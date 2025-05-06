
package org.testedsoftware.accountview

import schemact.aws.CognitoClientDetails 
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.ObjectMetadata
import com.fasterxml.jackson.databind.ObjectMapper
import schemact.react.File
import java.io.ByteArrayInputStream
import java.util.Base64
import org.apache.commons.fileupload.MultipartStream


class UploadFileImpl {



    // created from template  functionSampleImpl at 2025-05-02T13:46:54.624886700       
    fun uploadFile(userTableName: String, bucketName: String, Authorization: String, cognitoDetails: CognitoClientDetails, file: File, input: APIGatewayV2HTTPEvent) : String {
         println("input ${ObjectMapper().writeValueAsString(input)}")

        val s3: AmazonS3 =
            AmazonS3ClientBuilder.standard()
                .withRegion(cognitoDetails.region)
                .build()

/*        val contentType = input.headers?.get("content-type")
            ?: throw Exception("Missing Content-Type")

        val parts = MultiPart.read(input.body, input.headers?.get("content-type")
            ?: throw Exception("Missing Content-Type"))

        val filePart = parts.get("file")

        val filename = filePart?.contentDispositionValues!!["filename"]?:throw Exception("No file provided")
        val contentTypeValue = filePart?.contentType
        val file = File(filename = filePart?.contentDispositionValues!!["filename"]?:throw Exception("No file provided"), content = filePart.body, contentType =filePart?.contentType!!)
*/

        // Upload to S3 with environment-specific prefix
        val key = "uploads/${file.filename}"
        val metadata = ObjectMetadata().apply {
            this.contentType = file.contentType
            this.contentLength = file.content.size.toLong()
        }
        val s3Bucket = bucketName
        s3.putObject(s3Bucket, key, ByteArrayInputStream(file.content), metadata)

        // Generate file URL
        val fileUrl = "https://$s3Bucket.s3.amazonaws.com/$key"

        // Return success response
        return fileUrl
    }
    
}
