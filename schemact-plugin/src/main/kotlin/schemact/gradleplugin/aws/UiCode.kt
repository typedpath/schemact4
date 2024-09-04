package schemact.gradleplugin.aws
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import schemact.domain.Deployment
import schemact.domain.Domain
import schemact.gradleplugin.aws.util.S3Util.copyToS3
import java.io.File
import java.io.IOException
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.io.path.absolute
import kotlin.io.path.writeText

object UiCode {
    fun deployUiCode(
        domain: Domain,
        deployment: Deployment,
        uiCodeLocation: String,
        region: Regions = Regions.US_EAST_1
    ) {
        val websiteDomainName = "${deployment.subdomain}.${domain.name}"
        val s3Builder = AmazonS3Client.builder()
        s3Builder.region = region.getName()
        copyToS3(
            bucketName = websiteDomainName,
            distributionDirectory = uiCodeLocation,
            bucketFolder = "",
            region = region
        )
    }

   //TODO fix this
    fun buildUiCode(
        rootProjectDir: File,
        tempDir: Path,
        uiBuildCommand: String
    ) {
        println("creating tempDir: ${tempDir} ${tempDir.absolute()}")
        tempDir.toFile().mkdirs()
        val tempFilePath =  kotlin.io.path.createTempFile(/*directory = tempDir, prefix = "",*/ suffix = ".sh");
        println ("tempFilePath: $tempFilePath")
        val tempFile = tempFilePath.toFile()
        println("tempFile ${tempFile} ${tempFile.absolutePath}")

        tempFile.writeText(uiBuildCommand)

        val command = "cmd ${tempFile.absolutePath}"
        println("running file ${command}")
        val result = command.runCommand(rootProjectDir)
        println(result)
    }

    fun String.runCommand(workingDir: File): String? {
        try {
            val parts = this.split("\\s".toRegex())
            val proc = ProcessBuilder(*parts.toTypedArray())
                .directory(workingDir)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()

            proc.waitFor(60, TimeUnit.MINUTES)
            return proc.inputStream.bufferedReader().readText()
        } catch(e: IOException) {
            e.printStackTrace()
            return null
        }
    }

}
