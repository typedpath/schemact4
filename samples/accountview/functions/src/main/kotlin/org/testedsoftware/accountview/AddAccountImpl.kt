
package org.testedsoftware.accountview

import schemact.aws.CognitoClientDetails

// created by template functionSampleImpl


class AddAccountImpl {
    // created from template  functionSampleImpl at 2025-05-10T21:43:21.138087600       
    fun addAccount(userTableName: String, privateBucketName: String, Authorization: String,
                   cognitoDetails: CognitoClientDetails, account: Account) : UserInfo =
         UserInfoUpdaterOld.updateSecure(Authorization=Authorization, userTableName=userTableName, cognitoDetails =  cognitoDetails,
            update = {data -> data.accounts.add(account)
                      data})
    
}
