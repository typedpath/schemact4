
package org.testedsoftware.accountview


import org.testedsoftware.accountview.DynamoDbUtil.createOrUpdate
import schemact.aws.CognitoClientDetails
import java.time.LocalDateTime
import schemact.aws.VerifyCognito.verifyCognitoJwt

class OnLoginImpl {

    // created from template  functionSampleImpl at 2025-04-27T19:19:06.718319100
    // created from template  functionSampleImpl at 2025-05-06T19:07:16.567696700
    fun onLogin(userTableName: String, bucketName: String, Authorization: String, cognitoDetails: CognitoClientDetails) : UserInfo {
    val cognitoData = verifyCognitoJwt(Authorization, cognitoDetails)
    println(cognitoData)
    val userId = cognitoData.sub
    val email = cognitoData.email?:"unknown"

    return createOrUpdate(userTableName=userTableName, userId=userId, email=email, dataType = UserInfo::class.java,
            defaultData = {UserInfo(loginEvents=mutableListOf<String>())},
           update =  {data ->
               data.loginEvents.add(LocalDateTime.now().toString())
                            data}
           )

    }




}
