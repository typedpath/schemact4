package org.testedsoftware.accountview

import schemact.aws.CognitoClientDetails
import schemact.aws.VerifyCognito.verifyCognitoJwt

object UserInfoUpdater {

    fun updateSecure(Authorization: String, cognitoDetails: CognitoClientDetails,
                     userTableName: String,
                     update: ((data: UserInfo) -> UserInfo)? ) : UserInfo{
        // check authorization
        val cognitoData = verifyCognitoJwt(Authorization, cognitoDetails)
        println(cognitoData)
        val userId = cognitoData.sub
        val email = cognitoData.email?:"unknown"
        return UserInfoUpdater.update(userTableName=userTableName, userId=userId, email=email,
            update = update)
    }

    fun update(userTableName: String, userId: String, email: String,
               update: ((data: UserInfo) -> UserInfo ) ?)
              = DynamoDbUtil.createOrUpdate(userTableName=userTableName,
        userId=userId, email=email, UserInfo::class.java,
        defaultData = {UserInfo()},
        update =  update,
        deserialize = UserInfoDeserializer::deserialize,
        version = "1"
    )
}