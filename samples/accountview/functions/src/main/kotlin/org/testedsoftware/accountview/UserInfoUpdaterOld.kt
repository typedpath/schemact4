package org.testedsoftware.accountview

import schemact.aws.CognitoClientDetails
import schemact.aws.VerifyCognito.verifyCognitoJwt

//TODO autogen
object UserInfoUpdaterOld {

    fun updateSecure(Authorization: String, cognitoDetails: CognitoClientDetails,
                     userTableName: String,
                     update: ((data: UserInfo) -> UserInfo)? ) : UserInfo {
        // check authorization
        val cognitoData = verifyCognitoJwt(Authorization, cognitoDetails)
        println(cognitoData)
        val userId = cognitoData.sub
        val email = cognitoData.email ?: "unknown"
        return update(
            userTableName = userTableName, userId = userId, email = email,
            update = update
        )
    }

    fun update(userTableName: String, userId: String, email: String,
                version: String=UserInfoVersion.latest,
                update: ((data: UserInfo) -> UserInfo ) ?)=
        DynamoDbOld.createOrUpdate(userTableName=userTableName,
            userId=userId, email=email, UserInfo::class.java,
            defaultData = {UserInfo()},
            update =  update,
            preSaveFilter = UserInfoListener::preSaveFilter,
            deserialize = UserInfoDeserializer::deserialize,
            version = version
        )

}