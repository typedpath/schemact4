package org.testedsoftware.accountview

// functions can be arguments
// gen from private bucket
typealias WriteToUserToDataPrivateBucket = (key: String, value: Any) -> Unit
typealias ReadUserDataPrivateBucket = (key: String) -> String
// gen from UserKeyedDatabase
typealias UpdateUserInfoOld=   (update: ((data: UserInfo) -> UserInfo ) ?) -> UserInfo

