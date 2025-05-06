
package org.testedsoftware.accountview


import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.GetItemRequest
import com.amazonaws.services.dynamodbv2.model.PutItemRequest
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.testedsoftware.accountview.DynamoDbUtil.createOrUpdate
import schemact.aws.CognitoClientDetails
import java.time.Instant
import java.time.LocalDateTime
import schemact.aws.VerifyCognito.verifyCognitoJwt

class OnLoginImpl {

    // created from template  functionSampleImpl at 2025-04-27T19:19:06.718319100
    // created from template  functionSampleImpl at 2025-05-06T19:07:16.567696700
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




}
