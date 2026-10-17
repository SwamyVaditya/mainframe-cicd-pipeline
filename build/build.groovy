@Grab('com.ibm.dbb:dbb-core:2.0.2')
import com.ibm.dbb.repository.*
import com.ibm.dbb.dependency.*
import com.ibm.dbb.build.*

// Load build properties
Properties props = BuildProperties.load("${args[0]}/build.properties")
String hlq = props.getProperty("hlq")
String spaceAlloc = props.getProperty("trackSpace")

println("** [DBB] Starting Mainframe Build for SAM1")

// 1. Allocate PDS Datasets
def datasets = ["${hlq}.COBOL", "${hlq}.COPYLIB", "${hlq}.OBJ", "${hlq}.LOAD"]
datasets.each { ds ->
    println("** [DBB] Allocating PDS: ${ds}")
    try {
        new CreatePDS().dataset(ds).space(spaceAlloc).dsorg("PO").recfm("FB").lrecl(80).blksize(32760).create()
    } catch (Exception e) {
        println("** [DBB] Dataset ${ds} already exists or created with warning: ${e.message}")
    }
}

// 2. Copy Source Code to PDS
println("** [DBB] Copying source and copybooks to MVS PDS")
new CopyToPDS().file(new File("${args[0]}/cobol/SAM1.cbl")).dataset("${hlq}.COBOL(SAM1)").copy()
new CopyToPDS().file(new File("${args[0]}/copybook/SAMCOPY.cpy")).dataset("${hlq}.COPYLIB(SAMCOPY)").copy()

// 3. Compile Step (IGYCRCTL)
println("** [DBB] Compiling SAM1 with ${props.getProperty('compiler')}")
MVSExec compile = new MVSExec().file("SAM1.cbl").program(props.getProperty("compiler"))
compile.options("ADATA,RENT,LIB,APOST")
compile.dd(new MVSPropertiesDD("SYSIN", "${hlq}.COBOL(SAM1)"))
compile.dd(new MVSDataSetsDD("SYSLIB", ["${hlq}.COPYLIB", props.getProperty("coblib")]))
compile.dd(new MVSDataSetsDD("SYSLMOD", "${hlq}.OBJ"))
compile.dd(new MVSDataSetsDD("SYSUT1", "TRK",(15,15)).temporary(true))
compile.dd(new MVSDataSetsDD("SYSUT2", "TRK",(15,15)).temporary(true))
compile.dd(new MVSDataSetsDD("SYSUT3", "TRK",(15,15)).temporary(true))
compile.dd(new MVSDataSetsDD("SYSUT4", "TRK",(15,15)).temporary(true))
compile.dd(new MVSDataSetsDD("SYSUT5", "TRK",(15,15)).temporary(true))
compile.dd(new MVSDataSetsDD("SYSUT6", "TRK",(15,15)).temporary(true))
compile.dd(new MVSDataSetsDD("SYSUT7", "TRK",(15,15)).temporary(true))
compile.dd(new MVSDataSetsDD("SYSPRINT", "*"))
int rc = compile.execute()
if (rc > 4) {
    println("** [DBB ERROR] Compilation failed with return code ${rc}")
    System.exit(rc)
}

// 4. SonarQube Static Code Analysis Step
println("** [SonarQube] Executing Static Code Analysis on COBOL source...")
int sonarRc = MVSExec.executeShellCommand("sonar-scanner -Dsonar.projectBaseDir=${args[0]}")
if (sonarRc != 0) {
    println("** [SonarQube WARNING] Code analysis reported non-zero return code: ${sonarRc}")
    // Depending on quality gates, you can choose to fail the build here or warn
}

// 5. Link-Edit Step (IEWBLINK)
println("** [DBB] Link-editing SAM1 with ${props.getProperty('linker')}")
MVSExec link = new MVSExec().program(props.getProperty("linker"))
link.options("LIST,LET,XREF,RENT,AMODE=31,RMODE=ANY")
link.dd(new MVSPropertiesDD("SYSIN", "DD *").contents(" NAME SAM1(R)\n"))
link.dd(new MVSDataSetsDD("SYSLIN", "${hlq}.OBJ"))
link.dd(new MVSDataSetsDD("SYSLMOD", "${hlq}.LOAD"))
link.dd(new MVSDataSetsDD("SYSLIB", [props.getProperty("sysilb"), props.getProperty("syslib")]))
link.dd(new MVSDataSetsDD("SYSUT1", "TRK",(15,15)).temporary(true))
link.dd(new MVSDataSetsDD("SYSPRINT", "*"))
int linkRc = link.execute()
if (linkRc > 4) {
    println("** [DBB ERROR] Link-edit failed with return code ${linkRc}")
    System.exit(linkRc)
}

println("** [DBB] Build, Sonar Analysis, and Link-Edit completed successfully!")