package schemact.gradleplugin.aws.functiontemplates.injectionsupport

object InjectablesTemplate {
    fun InjectablesTemplate(packageName: String, userInfoClassName: String?) = """
    package $packageName
    // created from InjectablesTemplate

//    import org.testedsoftware.accountview.UserInfo

    // functions can be arguments
// gen from private bucket
    typealias WriteToUserToDataPrivateBucket = (key: String, value: Any) -> Unit
    typealias ReadUserDataPrivateBucket = (key: String) -> String
    // gen from UserKeyedDatabase
    ${if (userInfoClassName!=null) 
        "typealias UpdateUserInfo = (update: ((data: ${userInfoClassName}) -> UserInfo)?, deserialize: (str: String, version: String) -> ${userInfoClassName}) -> UserInfo" else ""
}
    // functions can be arguments
"""
}
//typealias UpdateUserInfo=   (update: ((data: UserInfo) -> UserInfo ) ?) -> UserInfo