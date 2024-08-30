package schemact.gradleplugin.aws.util

import com.amazonaws.AmazonServiceException
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.transfer.TransferManagerBuilder
import schemact.gradleplugin.aws.util.ZipUtil.zipResourceDirectory
import java.io.*
import java.nio.file.Paths

object S3Util {
    fun uploadBlobToS3(
        region: Regions,
        bucketName: String,
        keyName: String,
        inputStream: InputStream,
        credentialsProvider: AWSCredentialsProvider
    ): String {
        val metadata = ObjectMetadata()
        metadata.setContentType("application/blob")
        metadata.addUserMetadata("x-amz-meta-title", keyName)
        val s3Client = AmazonS3ClientBuilder.standard()
            .withCredentials(credentialsProvider)
            .withRegion(region)
            .build()
        s3Client.putObject(bucketName, keyName, inputStream, metadata)
        return "s3://$bucketName/$keyName"
    }

    fun uploadCodeToS3(
        strFile: String,
        region: Regions,
        bucketName: String,
        bucketKey: String,
        credentialsProvider: AWSCredentialsProvider
    ): String {
        val path = Paths.get(strFile)
        val theFile = path.toFile()
        println("loading code from ${theFile.absolutePath}")
        val inputStream = FileInputStream(path.toFile())
        println("found ${inputStream.available()} bytes in ${path.toAbsolutePath()}")
        return uploadBlobToS3(region, bucketName, bucketKey, inputStream, credentialsProvider)
    }

    fun zipResourceDirectoryToS3(
        region: Regions, bucketName: String, keyName: String,
        credentialsProvider: AWSCredentialsProvider, resourceDirectory: String
    ): String {
        // create a zip of the source and upload
        val baos = ByteArrayOutputStream()
        zipResourceDirectory(resourceDirectory, baos)
        val inputStream = ByteArrayInputStream(baos.toByteArray())
        return uploadBlobToS3(region, bucketName, keyName, inputStream, credentialsProvider)
    }

    fun copyToS3(
        bucketName: String,
        bucketFolder: String,
        distributionDirectory: String,
        region: Regions = Regions.US_EAST_1
    ) =
        copyToS3(bucketName, bucketFolder, File(distributionDirectory), region)

    fun copyToS3(
        bucketName: String,
        bucketFolder: String,
        distributionDirectory: File,
        region: Regions = Regions.US_EAST_1
    ) {
        val xfer_mgr = TransferManagerBuilder.standard()
            .withS3Client(AmazonS3ClientBuilder.standard().withRegion(region).build())
            .build()
        try {
            val xfer = xfer_mgr.uploadDirectory(
                bucketName,
                bucketFolder, distributionDirectory, true
            )
            // loop with Transfer.isDone()
            XferMgrProgress.showTransferProgress(xfer)
            // or block with Transfer.waitForCompletion()
            XferMgrProgress.waitForCompletion(xfer)
        } catch (e: AmazonServiceException) {
            System.err.println(e.errorMessage)
            System.exit(1)
        } finally {
            xfer_mgr.shutdownNow()
        }
    }

    fun main(args: Array<String>) {
        if (args.size != 3) {
            println("expected 7 args got ${args.size}")
            println("usage S3UtilKt bucketName bucketFolder sourceDirectory")
            System.exit(-1)
        }
        val bucketName: String = args[0]
        val bucketFolder: String = args[1]
        val distributionDirectory: String = args[2]
        println("copyToS3( $bucketName, $bucketFolder, $distributionDirectory )")
        copyToS3(bucketName, bucketFolder, distributionDirectory, Regions.EU_WEST_2)
        System.exit(0)
    }
}




