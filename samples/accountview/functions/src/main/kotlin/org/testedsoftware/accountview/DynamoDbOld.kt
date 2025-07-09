package org.testedsoftware.accountview

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.GetItemRequest
import com.amazonaws.services.dynamodbv2.model.PutItemRequest
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest
import com.fasterxml.jackson.databind.ObjectMapper
import java.time.Instant

object DynamoDbOld {
    val dynamoDb = AmazonDynamoDBClientBuilder.standard().build()

    fun <T> createOrUpdate(userTableName: String, userId: String, email: String, dataType: Class<T>, defaultData: ()-> T,
                           preSaveFilter: ((data: T) -> T),
                           update: ((data: T) -> T) ? = null,
                           deserialize: (str: String, version: String) -> T,
                           version: String
                           ) : T {
        var data: T? = null
        try {

            // Retrieve existing item from DynamoDB
            val existingData = getUserData(userTableName=userTableName, userId=userId,
                 dataType=dataType, deserialize=deserialize)

            if (existingData != null) {
                data = existingData
                if (update!=null) {
                    data = update(data)
                    data = preSaveFilter(data)
                    val strData = (ObjectMapper().writeValueAsString(data))
                    // Item exists, update specific attributes
                    val updateItemRequest = UpdateItemRequest()
                        .withTableName(userTableName)
                        .withKey(mapOf("user_id" to AttributeValue().withS(userId)))
                        .withUpdateExpression("SET email = :email, username = :username, #data = :data, #version=:version, #updated_at=:updated_at")
                        .withExpressionAttributeNames(
                            mapOf(
                                "#data" to "data",
                                "#version" to "version",
                                "#updated_at" to "updated_at"
                            )
                        ) // Avoid reserved keyword
                        .withExpressionAttributeValues(
                            mapOf(
                                ":email" to AttributeValue().withS(email),
                                ":username" to AttributeValue().withS(email),
                                ":data" to AttributeValue().withS(strData),
                                ":version" to AttributeValue().withS(version),
                                ":updated_at" to AttributeValue().withS(Instant.now().toString())

                            )
                        )
                    dynamoDb.updateItem(updateItemRequest)
                }
                println("Updated user data in DynamoDB for user_id=$userId")
            } else {
                // Item does not exist, create new item
                data = defaultData()
                if (update!=null) data = update(data)
                val strData = ObjectMapper().writeValueAsString(data)
                val putItemRequest = PutItemRequest()
                    .withTableName(userTableName)
                    .withItem(
                        mapOf(
                            "user_id" to AttributeValue().withS(userId),
                            "email" to AttributeValue().withS(email),
                            "username" to AttributeValue().withS(email),
                            "data" to AttributeValue().withS(strData),
                            "version" to AttributeValue().withS(version),
                            "created_at" to AttributeValue().withS(Instant.now().toString())
                        )
                    )
                dynamoDb.putItem(putItemRequest)
                println("Created new user data in DynamoDB for user_id=$userId")

            }
            return data
            //context.logger.log("Stored user data in DynamoDB for user_id=$userId")
        } catch (e: Exception) {
            e.printStackTrace()
            println("oops DynamoDB write failed: ${e.message}")
            throw e
            //context.logger.log("DynamoDB write failed: ${e.message}")
            //return buildResponse(500, mapOf("error" to "Failed to store user data", "details" to e.message))
        }


    }

    fun <T> getUserData(userTableName: String, userId: String,
                        dataType: Class<T>,
                        deserialize: (str: String, version: String) -> T) : T? {


        val getItemRequest = GetItemRequest()
            .withTableName(userTableName)
            .withKey(mapOf("user_id" to AttributeValue().withS(userId)))
            .withConsistentRead(true) // Ensure latest data
        val getItemResult = dynamoDb.getItem(getItemRequest)
        val existingItem = getItemResult.item

        return if (existingItem != null && existingItem.isNotEmpty()) {
            var version = existingItem.get("version")?.s?:"0"
            var strData: String = existingItem.get("data")?.s!!
             deserialize(strData, version)
        } else null
    }

}