
package org.testedsoftware.accountview
// created by template functionSampleImplNew

class AddAccountImpl { 
    // created from template  functionSampleImplNew at 2025-08-10T17:28:54.170776100       
    fun addAccount(UpdateUserInfo: schemact.aws.UpdateUserData<UserInfo>, account: Account) : UserInfo =
        AccountViewUserInfoUpdate.accountViewUserInfoUpdate(UpdateUserInfo) {
            data -> data.accounts.add(account)
        }
}
