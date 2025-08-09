
package org.testedsoftware.accountview

// created by template functionSampleImplNew

class SaveCategoriesImpl { 
    // created from template  functionSampleImplNew at 2025-08-09T22:01:47.099958800       
    fun saveCategories(UpdateUserInfo: schemact.aws.UpdateUserData<UserInfo>, categories: List<String>) =
        AccountViewUserInfoUpdate.accountViewUserInfoUpdate(UpdateUserInfo) {
              data -> data.categories = categories.toMutableList()
        }
    }

