package org.testedsoftware.accountview

import com.fasterxml.jackson.databind.ObjectMapper

object UserInfoDeserializer {
    fun deserialize(str: String, version: String) : UserInfo {
        return if (version.equals("2")) {
            ObjectMapper().readValue<UserInfo>(str, UserInfo::class.java) as UserInfo
        } else if (version.equals("1")) {
            val userInfo1 = ObjectMapper().readValue<UserInfo1>(str, UserInfo1::class.java)
            UserInfo(loginEvents=userInfo1.loginEvents, uploads= mutableListOf(), accounts=userInfo1.accounts,
                       categories =  userInfo1.categories, autoCatFilters = mutableListOf<UserInfo.AutoCatFilter>())
        }else if (version.equals("0")) {
            val userInfo0 = ObjectMapper().readValue<UserInfo0>(str, UserInfo0::class.java)
            UserInfo(loginEvents=userInfo0.loginEvents, uploads= mutableListOf(), accounts=userInfo0.accounts, categories =  mutableListOf())
        } else {
            throw Exception("invalid version: $version")
        }
    }
}