package schemact.gradleplugin.aws.cdk

import schemact.domain.InfrastructureInjectables.CognitoClientDetails
import schemact.domain.Instance

import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.cognito.UserPool
import software.amazon.awscdk.services.cognito.UserPoolClient
import software.amazon.awscdk.services.cognito.SignInAliases
import software.amazon.awscdk.services.cognito.AutoVerifiedAttrs
import software.constructs.Construct

object CDKCognitoStack {
    fun cognitoStack(stack: Stack, scope: Construct, id: String) : Instance{
        // Create a Cognito User Pool
        val userPool = UserPool.Builder.create(scope, id)
            .userPoolName(id)
            .selfSignUpEnabled(true) // Allow users to sign up
            .autoVerify(
                AutoVerifiedAttrs.builder()
                    .email(true) // Auto-verify email
                    .phone(false) // No auto-verification for phone
                    .build()
            )
            .signInAliases(
                SignInAliases.builder()
                    .email(true) // Allow sign-in with email
                    .build()
            )
            .build()

        // Create a User Pool Client
        val userPoolClient = UserPoolClient.Builder.create(scope, "MyUserPoolClient")
            .userPool(userPool)
            .generateSecret(false) // No secret needed for web apps
            .build()

        // Output the User Pool ID and Client ID
        software.amazon.awscdk.CfnOutput.Builder.create(scope, "UserPoolIdOutput")
            .value(userPool.userPoolId)
            .build()

        software.amazon.awscdk.CfnOutput.Builder.create(scope, "UserPoolClientIdOutput")
            .value(userPoolClient.userPoolClientId)
            .build()

        // Values for environment variables
        val userPoolId = /*props?.cognito?.userPoolId ?:*/ userPool.userPoolId // e.g., "us-east-1_XXXXXX"
        val clientId = /*props?.cognito?.clientId ?:*/ userPoolClient.userPoolClientId // e.g., "XXXXXXXXXXXX"
        val region = /*props?.region ?: */ stack.region // e.g., "us-east-1"
        val jwksUrl = "https://cognito-idp.$region.amazonaws.com/$userPoolId"//.well-known/jwks.json"

        val cognitoDetailsInstance = Instance(CognitoClientDetails.entity)
        cognitoDetailsInstance.set(CognitoClientDetails.clientId, clientId)
        cognitoDetailsInstance.set(CognitoClientDetails.userPoolId, userPoolId)
        cognitoDetailsInstance.set(CognitoClientDetails.jwksUrl, jwksUrl)
        cognitoDetailsInstance.set(CognitoClientDetails.region, region)
        return cognitoDetailsInstance
    }
}

