package com.typedpath.stack.util

import java.io.File

fun buildWebsite(sourceDirectory: File, command: Array<String>) {
    var builder = ProcessBuilder(*command)
    builder = builder.directory(sourceDirectory)
    builder.redirectErrorStream(true)// redirect error stream to output stream
    builder.redirectOutput(ProcessBuilder.Redirect.INHERIT)
    val process = builder.start()
    process.waitFor()
    val exitValue = process.exitValue()
    println("exitValue: $exitValue")
    //TODO switch stdout and sterr back
}

//build the deployment
fun deployCode(bucketName: String,
               bucketFolder: String?=null,
               sourceDirectory: File,
               buildCommand: Array<String>?=null,
               buildDirectory: String

) {
    if (!sourceDirectory.exists()) {
        throw Exception("sourceDir doesnt exist ${sourceDirectory.canonicalPath}")
    }
    // delete dist directory ?
    // ng build

    if (buildCommand!=null) buildWebsite(sourceDirectory, buildCommand)
    val distributionDirectory = File(sourceDirectory, buildDirectory)

    println("distributionDirectory = ${distributionDirectory.canonicalPath} isDirectory=${distributionDirectory.isDirectory}")
    copyToS3(bucketName, bucketFolder?:"", distributionDirectory)

}
