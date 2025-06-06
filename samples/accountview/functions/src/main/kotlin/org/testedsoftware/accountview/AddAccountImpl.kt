
package org.testedsoftware.accountview
// created by template functionSampleImpl

import schemact.aws.CognitoClientDetails
import schemact.aws.VerifyCognito.verifyCognitoJwt



class AddAccountImpl {
    // created from template  functionSampleImpl at 2025-05-10T21:43:21.138087600       
    fun addAccount(userTableName: String, privateBucketName: String, Authorization: String,
                   cognitoDetails: CognitoClientDetails, account: Account) : UserInfo =
         UserInfoUpdater.updateSecure(Authorization=Authorization, userTableName=userTableName, cognitoDetails =  cognitoDetails,
            update = {data -> data.accounts.add(account)
                      data})
    
}
