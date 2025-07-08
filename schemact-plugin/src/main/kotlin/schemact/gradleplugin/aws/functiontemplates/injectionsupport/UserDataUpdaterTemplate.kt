package schemact.gradleplugin.aws.functiontemplates.injectionsupport

import schemact.domain.Entity


object UserDataUpdaterTemplate {

    fun UserDataUpdaterTemplate(entity: Entity, packageName: String) = """
package $packageName        
import schemact.aws.CognitoClientDetails

//created by UserDataUpdaterTemplate
object ${entity.name}Updater {

    fun updateSecure(Authorization: String, cognitoDetails: CognitoClientDetails,
                     userTableName: String,
                     deserialize: (str: String, version: String) -> UserInfo,
                     update: ((data: ${entity.name}) -> ${entity.name})? ) : UserInfo {
        // check authorization
        val cognitoData = VerifyCognito.verifyCognitoJwt(Authorization, cognitoDetails)
        println(cognitoData)
        val userId = cognitoData.sub
        val email = cognitoData.email ?: "unknown"
        return update(
            userTableName = userTableName, userId = userId, email = email,
            deserialize =  deserialize,
            update = update
        )
    }

    fun update(userTableName: String, userId: String, email: String,
                version: String = "${entity.version}",
                deserialize: (str: String, version: String) -> UserInfo,
                update: ((data: ${entity.name}) -> UserInfo ) ?)=
        DynamoDbUtil.createOrUpdate(userTableName=userTableName,
            userId=userId, email=email, ${entity.name}::class.java,
            defaultData = {${entity.name}()},
            update =  update,
            preSaveFilter = {d->d},
            deserialize = deserialize,
            version = version
        )

}
"""
}