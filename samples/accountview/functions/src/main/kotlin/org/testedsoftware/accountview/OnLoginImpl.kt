
package org.testedsoftware.accountview

import java.time.LocalDateTime
import kotlin.compareTo

// created by template functionSampleImplNew

class OnLoginImpl {
    // created from template  functionSampleImplNew at 2025-08-09T14:47:21.050488200       
    fun onLogin(UpdateUserInfo: schemact.aws.UpdateUserData<UserInfo>): UserInfo =

        AccountViewUserInfoUpdate.accountViewUserInfoUpdate(UpdateUserInfo) { data ->
            data.loginEvents.add(LocalDateTime.now().toString())
            if (data.loginEvents.size > 20) data.loginEvents =
                data.loginEvents.subList(data.loginEvents.size - 20, data.loginEvents.size)
        }

}

