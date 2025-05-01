package schemact.gradleplugin.aws.cdk

import software.amazon.awscdk.services.dynamodb.CfnTable
import software.amazon.awscdk.services.dynamodb.Table
import software.amazon.awscdk.services.iam.CfnRole
import software.constructs.Construct

object CDKFunctionRoleTemplate {
    fun createFunctionRole(scope: Construct, websiteDomainName: String,  usersTable: Table?): CfnRole =
        CfnRole.Builder.create(scope, "functionRole")
            .assumeRolePolicyDocument(
                mapOf(
                    "Statement" to listOf(
                        mapOf(
                            "Action" to listOf("sts:AssumeRole"),
                            "Effect" to "Allow",
                            "Principal" to mapOf(
                                "Service" to listOf("edgelambda.amazonaws.com", "lambda.amazonaws.com")
                            )
                        )
                    ),
                    "Version" to "2012-10-17"
                )
            )
            .managedPolicyArns(
                listOf("arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole")
            )
            .policies(
                if (usersTable!=null) listOf(s3Policy(websiteDomainName),tablePolicy(usersTable))
                else listOf(s3Policy(websiteDomainName))
            )
            .build()

    fun s3Policy(websiteDomainName: String) =  CfnRole.PolicyProperty.builder()
        .policyDocument(
            mapOf(
                "Statement" to listOf(
                    mapOf(
                        "Action" to listOf("s3:PutObject", "s3:*"),
                        "Effect" to "Allow",
                        "Resource" to listOf("arn:aws:s3:::$websiteDomainName/*")
                    )
                ),
                "Version" to "2012-10-17"
            )
        )
        .policyName("s3Policy")
        .build()

    fun tablePolicy(usersTable: Table) =  CfnRole.PolicyProperty.builder()
        .policyDocument(
            mapOf(
                "Statement" to listOf(
                    mapOf(
                        "Action" to listOf(
                            "dynamodb:PutItem",
                            "dynamodb:UpdateItem",
                            "dynamodb:GetItem",
                            "dynamodb:Query"
                        ),
                        "Effect" to "Allow",
                        "Resource" to listOf(usersTable.tableArn)
                    )
                ),
                "Version" to "2012-10-17"
            )
        )
        .policyName("dynamoDBPolicy")
        .build()

}


