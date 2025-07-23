package schemact.domain


import schemact.domain.InfrastructureInjectables.ReadUserPrivateBucketData
import schemact.domain.InfrastructureInjectables.UpdateUserInfo
import schemact.domain.InfrastructureInjectables.VerifiedCognitoUser
import schemact.domain.InfrastructureInjectables.WriteUserPrivateBucketData

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

    object  AuthorizationHeaderType : StringType(maxLength = 2000, name = "AuthorizationHeader") {
                init {
                    isFromHeader = true
                }
            }

    //    data class CognitoResult(val sub: String, val email: String?, val username: String?)
    val VerifiedCognitoUser = Entity(name = "VerifiedCognitoUser", description = "user details from cognito or whatever") {
        string(name = "sub", description="sub", 50, optional = true)
        string(name = "email", description="email", 200, optional = true)
        string(name = "username", description="username", 50, optional = true)
    }

    val  verifyUserSession = Function (name="verifyUserSession",
               description = "verifies user session", paramType = Entity(name="params", description="params") {
               containsOne("cognitoClientDetails",  description = "cognitoClientDetails", CognitoClientDetails.entity)
               containsOne("AuthorizationHeaderType", type = InfrastructureInjectables.AuthorizationHeaderType)
        },
        returnType = VerifiedCognitoUser)


// should be a function, for time being new Function WriteUserPrivateBucketDataFunction
// somehow make it injectable - maybe add function field to ParamEntity or define native functions, types


        val WriteUserPrivateBucketData = Entity(name = "WriteUserPrivateBucketData", description = "Writes to private bucket") {
        isConstructedPreInjection = true
            // should be paramType and returnType
        containsOne("PrivateBucketNameType", type = InfrastructureInjectables.PrivateBucketNameType)
        containsOne("AuthorizationHeaderType", type = InfrastructureInjectables.AuthorizationHeaderType)
        containsOne(name="verifiedCognitoUser", description = "Verified Cognito User", type = VerifiedCognitoUser)
    }

    val createWriteUserPrivateBucketData = Function (name="createWriteUserPrivateBucketData",
    description = "verifies user session", paramType = Entity(name="params", description="params") {
        containsOne("verifiedCognitoUser",  description = "VerifiedCognitoUser", VerifiedCognitoUser)
        containsOne("privateBucketName", type = InfrastructureInjectables.PrivateBucketNameType)
    },
    returnType = WriteUserPrivateBucketData)


    val ReadUserPrivateBucketData = Entity(name = "ReadUserPrivateBucketData", description = "Read from a private bucket") {
        isConstructedPreInjection = true
        containsOne("PrivateBucketNameType", type = InfrastructureInjectables.PrivateBucketNameType)
        containsOne("AuthorizationHeaderType", type = InfrastructureInjectables.AuthorizationHeaderType)
        containsOne(name="cognitoDetails", description = "Cognito Details", type = InfrastructureInjectables.CognitoClientDetails.entity)
    }

    fun UpdateUserInfo(userInfoType: Entity) = Entity(name = "Update${userInfoType.name}", description = "Updates User Data (${userInfoType.name})") {
        isConstructedPreInjection = true
        containsOne("DynamoDBTablenameType", type = InfrastructureInjectables.DynamoDBTablenameType)
        containsOne("AuthorizationHeaderType", type = InfrastructureInjectables.AuthorizationHeaderType)
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
fun Entity.updateUserDataArg(userInfoType: Entity): Connection =
    containsOne(name="Update${userInfoType.name}", type=UpdateUserInfo(userInfoType))
