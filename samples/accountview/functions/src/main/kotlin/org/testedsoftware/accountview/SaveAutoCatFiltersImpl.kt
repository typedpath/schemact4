
package org.testedsoftware.accountview
// created by template functionSampleImpl
import schemact.aws.CognitoClientDetails
import schemact.aws.InjectablesFactory
import schemact.aws.UpdateUserInfo


class SaveAutoCatFiltersImpl { 
    // created from template  functionSampleImpl at 2025-06-17T13:20:47.776842700
    // TODO auto gen injectable creation
    fun saveAutoCatFilters(userTableName: String, Authorization: String, cognitoDetails: CognitoClientDetails, autoCatFilters: List<AutoCatFilter>) : UserInfo {
        val injectables = InjectablesFactory.create(Authorization=Authorization, cognitoDetails=cognitoDetails, userTableName=userTableName )
        return saveAutoCatFilters(injectables.updateUserInfo, autoCatFilters)
    }

    fun saveAutoCatFilters(updateUserInfo: UpdateUserInfo, autoCatFilters: List<AutoCatFilter>) : UserInfo =
        updateUserInfo({
            userInfo ->
                userInfo.autoCatFilters = autoCatFilters.toMutableList()
                userInfo
        })
    }
