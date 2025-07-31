
package org.testedsoftware.accountview
// created by template functionSampleImpl


class SaveAutoCatFiltersImpl { 
    // created from template  functionSampleImpl at 2025-06-17T13:20:47.776842700
    // TODO auto gen injectable creation
    fun saveAutoCatFilters(userTableName: String, Authorization: String, cognitoDetails: CognitoClientDetails, autoCatFilters: List<AutoCatFilter>) : UserInfo {
        val injectables = InjectablesFactoryOld.create(Authorization=Authorization, cognitoDetails=cognitoDetails, userTableName=userTableName )
        return saveAutoCatFilters(injectables.updateUserInfo, autoCatFilters)
    }

    fun saveAutoCatFilters(updateUserInfo: UpdateUserInfoOld, autoCatFilters: List<AutoCatFilter>) : UserInfo =
        updateUserInfo({
            userInfo ->
                userInfo.autoCatFilters = autoCatFilters.toMutableList()
                userInfo
        })
    }
