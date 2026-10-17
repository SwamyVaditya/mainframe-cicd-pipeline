@Grab('com.ibm.dbb:dbb-core:2.0.2')
import com.ibm.dbb.build.*

Properties props = BuildProperties.load("${args[0]}/build.properties")
String hlq = props.getProperty("hlq")

println("** [DBB] Cleaning up MVS workspace datasets...")
def datasets = ["${hlq}.COBOL", "${hlq}.COPYLIB", "${hlq}.OBJ", "${hlq}.LOAD"]

datasets.each { ds ->
    println("** [DBB] Deleting dataset: ${ds}")
    String cmd = "BPXWDYN \"alloc dd(DD1) dataset('${ds}') shr reuse\""
    String delCmd = "BPXWDYN \"free dd(DD1) dataset('${ds}') delete\""
    try {
        MVSExec.executeShellCommand(delCmd)
    } catch (Exception e) {
        println("** [DBB Warning] Dataset ${ds} could not be purged: ${e.message}")
    }
}
println("** [DBB] Workspace cleanup completed.")