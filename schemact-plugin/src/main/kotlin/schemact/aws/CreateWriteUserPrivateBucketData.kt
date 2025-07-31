package schemact.aws
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import com.fasterxml.jackson.databind.ObjectMapper

object CreateWriteUserPrivateBucketData {
           fun createWriteUserPrivateBucketData (privateBucketName: String, verifiedCognitoUser: VerifiedCognitoUser): WriteUserPrivateBucketData {
                   val writeToUserToDataPrivateBucket: WriteUserPrivateBucketData =
                 { key, value ->
                    val s3 = AmazonS3ClientBuilder.standard().withRegion("us-east-1").build()
                    val inputStream = (ObjectMapper()).writeValueAsString(value).byteInputStream()
                    val om = ObjectMetadata()
                    om.contentType = "application/json; charset=utf-8"
                    val privatisedKey = "${verifiedCognitoUser.sub}$key"
                    val writeObjectRequest =
                        PutObjectRequest(privateBucketName, privatisedKey, inputStream, om)
                    val s3Object = s3.putObject(writeObjectRequest)
                }
                return writeToUserToDataPrivateBucket
    }
  }            