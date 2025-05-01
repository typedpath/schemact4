package schemact.gradleplugin.aws.cdk

import software.amazon.awscdk.services.dynamodb.*
import software.constructs.Construct
import software.amazon.awscdk.CfnOutput

object UserTableStack {//(scope: Construct, id: String, props: StackProps? = null) : Stack(scope, id, props) {
// tableName org.testedsoftware.sample-users" is valid
// id e.g. SampleUsers
// TODO tablename needs to be injected into function code
    fun userTableStack(scope: Construct, id: String, tableName: String, deleteWithStack: Boolean) : Table{
        // Create DynamoDB table with user_id as partition key

        val removalPolicy = if (deleteWithStack) software.amazon.awscdk.RemovalPolicy.DESTROY else
            software.amazon.awscdk.RemovalPolicy.RETAIN

        val userTable = Table.Builder.create(scope, id)
            .tableName(tableName) // Table name (complies with DynamoDB naming rules)
            .partitionKey(Attribute.builder()
                .name("user_id")
                .type(AttributeType.STRING)
                .build())
            .billingMode(BillingMode.PAY_PER_REQUEST) // On-demand billing
            .removalPolicy(removalPolicy) // Deletes table on stack deletion (for testing)
            .build()

        // Add a Global Secondary Index (GSI) on email
        userTable.addGlobalSecondaryIndex(GlobalSecondaryIndexProps.builder()
            .indexName("EmailIndex")
            .partitionKey(Attribute.builder()
                .name("email")
                .type(AttributeType.STRING)
                .build())
            .projectionType(ProjectionType.ALL) // Projects all attributes (can be customized)
            .build())

        // Output the table ARN for reference
        CfnOutput.Builder.create(scope, "${id}TableArn")
            .value(userTable.tableArn)
            .description("ARN of the $id DynamoDB table")
            .build()
    return userTable
    }



}

