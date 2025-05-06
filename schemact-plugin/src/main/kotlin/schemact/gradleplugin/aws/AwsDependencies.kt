package schemact.gradleplugin.aws

object AwsDependencies {
    val awsLambdaDependencies = listOf(
        "com.amazonaws:aws-lambda-java-core:1.2.1",
        "com.amazonaws:aws-lambda-java-events:3.11.0",
        "com.amazonaws:aws-java-sdk-s3:1.11.574",
        "com.amazonaws:aws-java-sdk-dynamodb:1.12.777"
        /*
        Recommended Dependency: Add software.amazon.awssdk:cognito-jwt-verifier
                          (or its predecessor com.amazonaws:aws-jwt-verify for compatibility with your older SDK dependencies).
         */
        //,"software.amazon.awssdk:cognito-jwt-verifier:2.21.0" // Latest compatible with AWS SDK v2
        ,"com.auth0:java-jwt:4.4.0"
        ,"com.auth0:jwks-rsa:0.22.1"
        ,"commons-fileupload:commons-fileupload:1.5"

    //,"com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2"
    )
}
