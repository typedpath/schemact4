
package org.testedsoftware.accountview
// created by template functionSampleImpl
import schemact.aws.CognitoClientDetails
import schemact.aws.VerifyCognito.verifyCognitoJwt


class SaveCategoriesImpl { 
    // created from template  functionSampleImpl at 2025-06-11T16:13:29.666682200       
    fun saveCategories(userTableName: String, Authorization: String, cognitoDetails: CognitoClientDetails, categories: List<String>) : UserInfo {
        val cognitoData = verifyCognitoJwt(Authorization, cognitoDetails)

        val updateUserInfo:   (update: ((data: UserInfo) -> UserInfo ) ?) -> UserInfo =
            {
                    update ->
                UserInfoUpdaterOld.update(userTableName=userTableName, userId=cognitoData.sub,
                    email = cognitoData.email?:"noemail", update=update)
            }
       return  saveCategories(updateUserInfo, categories)
    }

    fun saveCategories(updateUserInfo:   (update: ((data: UserInfo) -> UserInfo ) ?)->UserInfo, categories: List<String>) : UserInfo =
        updateUserInfo({
            data ->
               data.categories=categories.toMutableList()
               data
        })

}
