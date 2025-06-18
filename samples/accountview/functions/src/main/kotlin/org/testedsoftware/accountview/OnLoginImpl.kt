
package org.testedsoftware.accountview


import org.testedsoftware.accountview.AutoCatFiltersExtra.defaultAutoCatFilters
import org.testedsoftware.accountview.DynamoDbUtil.createOrUpdate
import schemact.aws.CognitoClientDetails
import java.time.LocalDateTime
import schemact.aws.VerifyCognito.verifyCognitoJwt

class OnLoginImpl {

    // created from template  functionSampleImpl at 2025-04-27T19:19:06.718319100
    // created from template  functionSampleImpl at 2025-05-06T19:07:16.567696700
    fun onLogin(userTableName: String, bucketName: String, Authorization: String, cognitoDetails: CognitoClientDetails) : UserInfo =
        UserInfoUpdater.updateSecure(Authorization=Authorization, userTableName=userTableName, cognitoDetails =  cognitoDetails,
            update = {data ->
                data.loginEvents.add(LocalDateTime.now().toString())
                // TODO should be part of a structured update
                if(data.autoCatFilters.isEmpty())
                    data.autoCatFilters = defaultAutoCatFilters().toMutableList()
                data})

    }
