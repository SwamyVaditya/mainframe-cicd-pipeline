@Grab('com.ibm.dbb:dbb-core:2.0.2')
import com.ibm.dbb.build.*
import groovy.json.JsonBuilder

Properties props = BuildProperties.load("${args[0]}/build.properties")
String hlq = props.getProperty("hlq")
String targetDir = props.getProperty("targetUssDir")

println("** [DBB] Packaging Load Module to USS directory: ${targetDir}")
File stagingDir = new File(targetDir)
if (!stagingDir.exists()) stagingDir.mkdirs()

// Extract binary from MVS Load Library to HFS/USS
new CopyToHFS().dataset("${hlq}.LOAD(SAM1)").file(new File("${targetDir}/SAM1")).binary(true).copy()

// Generate deployment manifest JSON
def manifest = [
    program: "SAM1",
    timestamp: new Date().format("yyyy-MM-dd'T'HH:mm:ssZ"),
    loadModule: "${hlq}.LOAD(SAM1)",
    ussPath: "${targetDir}/SAM1",
    status: "PACKAGED"
]

File manifestFile = new File("${targetDir}/deploy_manifest.json")
manifestFile.text = new JsonBuilder(manifest).toPrettyString()
println("** [DBB] Package Manifest generated successfully at ${manifestFile.absolutePath}")