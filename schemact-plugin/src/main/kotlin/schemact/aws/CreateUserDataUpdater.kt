
package schemact.aws

object CreateUserDataUpdater {

    inline fun <reified T> createUserDataUpdater(verifiedCognitoUser: VerifiedCognitoUser, userTableName: String, version: String) : UpdateUserData<T> {
        return {
              update, deserialize, defaultData ->
                      DynamoDbUtil.createOrUpdate(userTableName=userTableName,
                               userId=verifiedCognitoUser.sub, email=verifiedCognitoUser.email, T::class.java,
                               defaultData = defaultData,
                               update =  update,
                               preSaveFilter = {d->d},
                               deserialize = deserialize,
                               version = version)
        }
    }
}
