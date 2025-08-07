package schemact.domain


import schemact.domain.InfrastructureInjectables.ReadUserPrivateBucketData
import schemact.domain.InfrastructureInjectables.UpdateUserData

import schemact.domain.InfrastructureInjectables.WriteUserPrivateBucketData
import schemact.domain.RestInjectables.MultiPartBodyReader

object InfrastructureInjectables {
// see here https://ogp.me/

    object APIGatewayV2HTTPEventEntity : Entity (
        name="APIGatewayV2HTTPEvent",
        prefferedPackage = "com.amazonaws.services.lambda.runtime.events",
        description = "structure from lambda calls from APIGateway or cloudfront",
        isNativePassthrough = true
    )

    const val AwsPackage = "schemact.aws"


    object CognitoClientDetails {
        lateinit var jwksUrl  : Connection
        lateinit var userPoolId  : Connection
        lateinit var clientId  : Connection
        lateinit var region  : Connection
        val entity =
            Entity(name = "CognitoClientDetails", description = "details to connect to cognito") {
                isFromInfrastructure = true
                jwksUrl = string(name = "jwksUrl", description="jwksUrl TODO", maxLength = 500)
                userPoolId = string(name = "userPoolId", description="userPoolId TODO", 500, optional = true)
                clientId = string(name = "clientId", "ClientId TODO", maxLength = 500)
                region = string(name = "region", "Region TODO", maxLength = 500)
                prefferedPackage= AwsPackage
                }
            }

    object BucketNameType : StringType(maxLength = 100, name="BucketName") {
        init {
            isFromInfrastructure = true
        }
    }

    object PrivateBucketNameType : StringType(maxLength = 100, name="PrivateBucketName") {
        init {
            isFromInfrastructure = true
        }
    }

    object DynamoDBTablenameType : StringType(maxLength = 300, name="DynamoDBTablename") {
        init {
            isFromInfrastructure = true
        }
    }



    //    data class CognitoResult(val sub: String, val email: String?, val username: String?)
    val VerifiedCognitoUser = Entity(name = "VerifiedCognitoUser", description = "user details from cognito or whatever",
        prefferedPackage= AwsPackage) {
        string(name = "sub", description="sub", 50, optional = true)
        string(name = "email", description="email", 200, optional = true)
        string(name = "username", description="username", 50, optional = true)
    }

    val  verifyUserSession = Function (name="verifyUserSession",
               description = "verifies user session", paramType = Entity(name="params", description="params") {
               containsOne("cognitoClientDetails",  description = "cognitoClientDetails", CognitoClientDetails.entity)
               containsOne("token", type = RestInjectables.AuthorizationHeaderType)
        },
        returnType = VerifiedCognitoUser)


// should be a function, for time being new Function WriteUserPrivateBucketDataFunction
// somehow make it injectable - maybe add function field to ParamEntity or define native functions, types


        val WriteUserPrivateBucketData = Entity(name = "WriteUserPrivateBucketData", description = "Writes to private bucket",
            prefferedPackage= AwsPackage
            ) {
        isConstructedPreInjection = true
        nativeDefinition = Entity.NativeDefinition(kotlin="typealias WriteUserPrivateBucketData = (key: String, value: Any) -> Unit")
            // should be paramType and returnType
        //TODO remove these
        containsOne("PrivateBucketNameType", type = InfrastructureInjectables.PrivateBucketNameType)
        containsOne("AuthorizationHeaderType", type = RestInjectables.AuthorizationHeaderType)
        containsOne(name="verifiedCognitoUser", description = "Verified Cognito User", type = VerifiedCognitoUser)
    }

    val UpdateUserData  = Entity(name = "UpdateUserData", description = "updates user data, first generic argument is the type updated",
        prefferedPackage= AwsPackage,
    ) {
        isConstructedPreInjection = true
        nativeDefinition = Entity.NativeDefinition(kotlin="typealias UpdateUserData<T> =(update: ((data: T) -> T)?, deserialize: (str: String, version: String) -> T, defaultData: ()->T) -> T")
    }

    val createUserDataUpdater = Function (name="createUserDataUpdater",
        description = "creates user data updater", paramType = Entity(name="params", description="params") {
            containsOne("verifiedCognitoUser",  description = "VerifiedCognitoUser", VerifiedCognitoUser)
            containsOne("userTableName", type = DynamoDBTablenameType)
            //containsOne(name = "version", type = dataVersionType)
        },
        returnType = UpdateUserData)

    val createWriteUserPrivateBucketData = Function (name="createWriteUserPrivateBucketData",
    description = "supplies a write to private bucket space", paramType = Entity(name="params", description="params") {
        containsOne("verifiedCognitoUser",  description = "VerifiedCognitoUser", VerifiedCognitoUser)
        containsOne("privateBucketName", type = InfrastructureInjectables.PrivateBucketNameType)
    },
    returnType = WriteUserPrivateBucketData)


    val ReadUserPrivateBucketData = Entity(name = "ReadUserPrivateBucketData", description = "Read from a private bucket") {
        isConstructedPreInjection = true
        prefferedPackage= AwsPackage
        nativeDefinition = Entity.NativeDefinition(kotlin="typealias ReadUserPrivateBucketData = (key: String) -> String")
        //TODO remove these
    }

    val createReadUserPrivateBucketData = Function (name="createReadUserPrivateBucketData",
        description = "supplies a reader of private bucket space", paramType = Entity(name="params", description="params") {
            containsOne("verifiedCognitoUser",  description = "VerifiedCognitoUser", VerifiedCognitoUser)
            containsOne("privateBucketName", type = InfrastructureInjectables.PrivateBucketNameType)
        },
        returnType = ReadUserPrivateBucketData)

    val createMultiPartBodyReader = Function (name="createMultiPartBodyReader",
        description = "supplies a reader of private bucket space", paramType = Entity(name="params", description="params") {
            containsOne("input",  description = "APIGatewayV2HTTPEventEntity", APIGatewayV2HTTPEventEntity)
        },
        returnType = MultiPartBodyReader)


    // TODO remove
    fun UpdateUserInfo(userInfoType: Entity) = Entity(name = "Update${userInfoType.name}", description = "Updates User Data (${userInfoType.name})") {
        isConstructedPreInjection = true
        containsOne("DynamoDBTablenameType", type = InfrastructureInjectables.DynamoDBTablenameType)
        containsOne("AuthorizationHeaderType", type = RestInjectables.AuthorizationHeaderType)
        containsOne(name="cognitoDetails", description = "Cognito Details", type = InfrastructureInjectables.CognitoClientDetails.entity)
    }

// to inject relationships between other injectables are needed - in the injectable resolver ?
//     WritePrivateBucket depends on (AuthorizationHeaderType, PrivateBucketNameType)
 //   typealias WriteToUserToDataPrivateBucket = (key: String, value: Any) -> Unit
  //  typealias ReadUserDataPrivateBucket = (key: String) -> String
    // gen from UserKeyedDatabase
   // typealias UpdateUserInfo=   (update: ((data: UserInfo) -> UserInfo ) ?) -> UserInfo


}

fun Entity.writeUserPrivateBucketDataArg(): Connection = containsOne(name="WriteUserPrivateBucketData", type=WriteUserPrivateBucketData)
fun Entity.readUserPrivateBucketDataArg(): Connection = containsOne(name="ReadUserPrivateBucketData", type=ReadUserPrivateBucketData)
fun Entity.updateUserDataArg(userDataType: Entity): Connection {
    val result = containsOne(name = "Update${userDataType.name}", type =UpdateUserData)
    result.genericParams = listOf(userDataType)
    return result
}

val dataVersionType = StringType(100)

