package org.testedsoftware.accountview

object AccountViewUserInfoUpdate {
    fun accountViewUserInfoUpdate(UpdateUserInfo: schemact.aws.UpdateUserData<UserInfo>, update: (data: UserInfo) -> Unit)=  UpdateUserInfo ({
        update(it)
        it
    },
        { str, version -> UserInfoDeserializer.deserialize(str, version) },
        { UserInfo() })
}