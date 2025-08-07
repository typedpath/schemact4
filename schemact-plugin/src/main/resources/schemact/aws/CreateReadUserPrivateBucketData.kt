package schemact.aws
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import com.fasterxml.jackson.databind.ObjectMapper
import schemact.aws.VerifiedCognitoUser
import schemact.aws.ReadUserPrivateBucketData

object CreateReadUserPrivateBucketData {
           fun createReadUserPrivateBucketData (privateBucketName: String, verifiedCognitoUser: VerifiedCognitoUser): ReadUserPrivateBucketData {
                   val readUserPrivateBucketData: ReadUserPrivateBucketData =
                 { key ->
                     val s3 = AmazonS3ClientBuilder.standard().withRegion("us-east-1").build()
                     val privatisedKey = "${verifiedCognitoUser.sub}$key"
                     val getObjectRequest = GetObjectRequest(privateBucketName, privatisedKey)
                     try {
                         val s3Object = s3.getObject(getObjectRequest)
                         val contentType = s3Object.objectMetadata.contentType
                         s3Object.objectContent.bufferedReader().use { it.readText() }
                     } catch (ex: Exception) {
                         println("InjectablesFactory.readUserDataPrivateBucket failed to read object $privatisedKey in $privateBucketName")
                         println("InjectablesFactory.readUserDataPrivateBucket cognitoData.sub: ${verifiedCognitoUser.sub} key=$key")
                         throw ex
                     }
                }
                return readUserPrivateBucketData
    }
  }            