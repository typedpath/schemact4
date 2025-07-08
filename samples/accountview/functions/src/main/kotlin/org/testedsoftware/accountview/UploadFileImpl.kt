
package org.testedsoftware.accountview

import schemact.aws.CognitoClientDetails 
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.ObjectMetadata
import com.fasterxml.jackson.databind.ObjectMapper
import schemact.aws.VerifyCognito.verifyCognitoJwt
import schemact.react.File
import java.io.ByteArrayInputStream
import java.time.LocalDateTime


class UploadFileImpl {

    // created from template  functionSampleImpl at 2025-05-02T13:46:54.624886700       
    fun uploadFile(userTableName: String, privateBucketName: String, Authorization: String, cognitoDetails: CognitoClientDetails, file: File, input: APIGatewayV2HTTPEvent) : UserInfo {
         println("input ${ObjectMapper().writeValueAsString(input)}")

        // check authorization
        val cognitoData = verifyCognitoJwt(Authorization, cognitoDetails)
        println(cognitoData)
        val userId = cognitoData.sub
        val email = cognitoData.email?:"unknown"

        val s3: AmazonS3 =
            AmazonS3ClientBuilder.standard()
                .withRegion(cognitoDetails.region)
                .build()

        // Upload to S3 with environment-specific prefix
        val path = "/uploads/${file.filename}"
        val key = "$userId$path"
        val metadata = ObjectMetadata().apply {
            this.contentType = file.contentType
            this.contentLength = file.content.size.toLong()
        }
        val s3Bucket = privateBucketName
        s3.putObject(s3Bucket, key, ByteArrayInputStream(file.content), metadata)

        // Generate file URL
        val fileUrl = "https://$s3Bucket.s3.amazonaws.com/$key"

        return UserInfoUpdaterOld.update(userTableName=userTableName,
            userId=userId, email=email,
            update =  {data ->
                data.uploads.add(UserInfo.RandomFile(filename = file.filename, location = path, contentType = file.contentType, uploadTime=LocalDateTime.now().toString()))
                data})
    }
    
}
