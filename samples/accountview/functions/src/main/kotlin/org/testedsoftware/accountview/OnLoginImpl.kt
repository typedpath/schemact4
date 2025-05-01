
package org.testedsoftware.accountview


import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.GetItemRequest
import com.amazonaws.services.dynamodbv2.model.PutItemRequest
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest
import com.fasterxml.jackson.databind.ObjectMapper
import schemact.aws.CognitoClientDetails
import java.time.Instant
import java.time.LocalDateTime
import schemact.aws.VerifyCognito.verifyCognitoJwt

class OnLoginImpl {

    // created from template  functionSampleImpl at 2025-04-27T19:19:06.718319100
    fun onLogin(userTableName: String, bucketName: String, Authorization: String, cognitoDetails: CognitoClientDetails) : String {
    val cognitoData = verifyCognitoJwt(Authorization, cognitoDetails)
    println(cognitoData)
    val userId = cognitoData.sub
    val email = cognitoData.email?:"unknown"

    createOrUpdate(userTableName=userTableName, userId=userId, email=email, dataType = UserInfo::class.java,
            defaultData = {UserInfo(loginEvents=mutableListOf<String>())},
           update =  {data ->
               data.loginEvents.add(LocalDateTime.now().toString())
                            data}
           )


    return cognitoData.toString()
    }

    fun <T> createOrUpdate(userTableName: String, userId: String, email: String, dataType: Class<T>, defaultData: ()-> T, update: (data: T) -> T) {
        val dynamoDb = AmazonDynamoDBClientBuilder.standard().build()
        try {

            // Retrieve existing item from DynamoDB
            val getItemRequest = GetItemRequest()
                .withTableName(userTableName)
                .withKey(mapOf("user_id" to AttributeValue().withS(userId)))
                .withConsistentRead(true) // Ensure latest data
            val getItemResult = dynamoDb.getItem(getItemRequest)
            val existingItem = getItemResult.item

            if (existingItem != null && existingItem.isNotEmpty()) {
            var strData: String = existingItem.get("data")?.s!!

            var data = (ObjectMapper()).readValue(strData, dataType)
            data = update(data)
            strData = (ObjectMapper().writeValueAsString(data))
                // Item exists, update specific attributes
                val updateItemRequest = UpdateItemRequest()
                    .withTableName(userTableName)
                    .withKey(mapOf("user_id" to AttributeValue().withS(userId)))
                    .withUpdateExpression("SET email = :email, username = :username, #data = :data, #updated_at=:updated_at")
                    .withExpressionAttributeNames(mapOf("#data" to "data", "#updated_at" to "updated_at")) // Avoid reserved keyword
                    .withExpressionAttributeValues(mapOf(
                        ":email" to AttributeValue().withS(email),
                        ":username" to AttributeValue().withS(email),
                        ":data" to AttributeValue().withS(strData),
                        ":updated_at" to AttributeValue().withS(Instant.now().toString())

                    ))
                dynamoDb.updateItem(updateItemRequest)
                println("Updated user data in DynamoDB for user_id=$userId")
            } else {
                // Item does not exist, create new item
                val data = update(defaultData())
                val strData = ObjectMapper().writeValueAsString(data)
                val putItemRequest = PutItemRequest()
                    .withTableName(userTableName)
                    .withItem(
                        mapOf(
                            "user_id" to AttributeValue().withS(userId),
                            "email" to AttributeValue().withS(email),
                            "username" to AttributeValue().withS(email),
                            "data" to AttributeValue().withS(strData),
                            "created_at" to AttributeValue().withS(Instant.now().toString())
                        )
                    )
                dynamoDb.putItem(putItemRequest)
                println("Created new user data in DynamoDB for user_id=$userId")

            }
            return
                //context.logger.log("Stored user data in DynamoDB for user_id=$userId")
        } catch (e: Exception) {
            e.printStackTrace()
            println("oops DynamoDB write failed: ${e.message}")
            throw e
            //context.logger.log("DynamoDB write failed: ${e.message}")
            //return buildResponse(500, mapOf("error" to "Failed to store user data", "details" to e.message))
        }


    }



}
