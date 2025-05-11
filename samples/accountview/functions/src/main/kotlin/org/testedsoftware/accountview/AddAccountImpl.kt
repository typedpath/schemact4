
package org.testedsoftware.accountview
// created by template functionSampleImpl

import schemact.aws.CognitoClientDetails
import schemact.aws.VerifyCognito.verifyCognitoJwt



class AddAccountImpl {
    // created from template  functionSampleImpl at 2025-05-10T21:43:21.138087600       
    fun addAccount(userTableName: String, privateBucketName: String, Authorization: String, cognitoDetails: CognitoClientDetails, account: Account) : UserInfo {
        // check authorization
        val cognitoData = verifyCognitoJwt(Authorization, cognitoDetails)
        println(cognitoData)
        val userId = cognitoData.sub
        val email = cognitoData.email?:"unknown"


        return DynamoDbUtil.createOrUpdate(userTableName=userTableName,
            userId=userId, email=email, UserInfo::class.java,
            defaultData = {UserInfo()},
            update =  {data ->
                data.accounts.add(account)
                data})
    }
    
}
