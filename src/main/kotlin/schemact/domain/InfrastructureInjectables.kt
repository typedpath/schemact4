package schemact.domain

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

}