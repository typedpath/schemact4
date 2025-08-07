package org.testedsoftware.accountview

import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import com.fasterxml.jackson.databind.ObjectMapper
import schemact.aws.CognitoClientDetails
import schemact.aws.VerifyCognito.verifyCognitoJwt

object InjectablesFactoryOld {
    data class SecureUserInjectables(val updateUserInfo:   UpdateUserInfoOld,
                                     val writeToUserToDataPrivateBucket : WriteToUserToDataPrivateBucket?,
                                     val readUserDataPrivateBucket : ReadUserDataPrivateBucket?
        )

    fun create(Authorization: String, cognitoDetails: CognitoClientDetails, userTableName: String,
               privateBucketName: String?=null) : SecureUserInjectables {

        val cognitoData = verifyCognitoJwt(Authorization, cognitoDetails)

        val updateUserInfo:   UpdateUserInfoOld =
            {
                    update ->
                UserInfoUpdaterOld.update(userTableName=userTableName, userId=cognitoData.sub,
                    email = cognitoData.email?:"noemail", update=update)
            }

        val writeToUserToDataPrivateBucket : WriteToUserToDataPrivateBucket? =
            if (privateBucketName!=null) {
                    key, value ->
                val s3 = AmazonS3ClientBuilder.standard().withRegion("us-east-1").build()
                val inputStream = (ObjectMapper()).writeValueAsString(value).byteInputStream()
                val om = ObjectMetadata()
                om.contentType =  "application/json; charset=utf-8"
                val privatisedKey = "${cognitoData.sub}$key"
                val writeObjectRequest = PutObjectRequest(privateBucketName, privatisedKey, inputStream, om)
                val s3Object = s3.putObject(writeObjectRequest)
            } else null


        val readUserDataPrivateBucket : ReadUserDataPrivateBucket? =
            if (privateBucketName!=null) {
                    key ->
                val s3 = AmazonS3ClientBuilder.standard().withRegion("us-east-1").build()
                val privatisedKey = "${cognitoData.sub}$key"
                val getObjectRequest = GetObjectRequest(privateBucketName, privatisedKey)
                try {
                    val s3Object = s3.getObject(getObjectRequest)
                    val contentType = s3Object.objectMetadata.contentType
                    s3Object.objectContent.bufferedReader().use { it.readText() }
                } catch (ex: Exception) {
                    println("InjectablesFactory.readUserDataPrivateBucket failed to read object $privatisedKey in $privateBucketName")
                    println("InjectablesFactory.readUserDataPrivateBucket cognitoData.sub: ${cognitoData.sub} key=$key")
                    throw ex
                }
            }  else null
        return SecureUserInjectables(updateUserInfo,  writeToUserToDataPrivateBucket, readUserDataPrivateBucket)
    }
}