package schemact.gradleplugin.aws.cdk

import software.amazon.awscdk.App
import software.amazon.awscdk.Stack
import software.amazon.awscdk.StackProps
import software.amazon.awscdk.services.cognito.UserPool
import software.amazon.awscdk.services.cognito.UserPoolClient
import software.amazon.awscdk.services.cognito.SignInAliases
import software.amazon.awscdk.services.cognito.AutoVerifiedAttrs
import software.constructs.Construct

object CDKCognitoStack {
    fun cognitoStack(scope: Construct, id: String) {
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
    }
}

