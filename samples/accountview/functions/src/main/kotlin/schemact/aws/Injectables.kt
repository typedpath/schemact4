package schemact.aws

import org.testedsoftware.accountview.UserInfo

// functions can be arguments
// gen from private bucket
typealias WriteToUserToDataPrivateBucket = (key: String, value: Any) -> Unit
typealias ReadUserDataPrivateBucket = (key: String) -> String
// gen from UserKeyedDatabase
typealias UpdateUserInfo=   (update: ((data: UserInfo) -> UserInfo ) ?) -> UserInfo

