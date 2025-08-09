
package org.testedsoftware.accountview
// created by template functionSampleImplNew

class SaveAutoCatFiltersImpl {
    // created from template  functionSampleImplNew at 2025-08-09T22:25:22.153253100       
    fun saveAutoCatFilters(
        UpdateUserInfo: schemact.aws.UpdateUserData<UserInfo>,
        autoCatFilters: List<AutoCatFilter>
    ): UserInfo =
        AccountViewUserInfoUpdate.accountViewUserInfoUpdate(UpdateUserInfo) { data ->
            data.autoCatFilters = autoCatFilters.toMutableList()
        }
}