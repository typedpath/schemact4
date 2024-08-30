package schemact.gradleplugin.aws

object AwsDependencies {
    val awsLambdaDependencies = listOf(
        "com.amazonaws:aws-lambda-java-core:1.2.1",
        "com.amazonaws:aws-lambda-java-events:3.11.0",
        "com.amazonaws:aws-java-sdk-s3:1.11.574"
    )
}
