package samples.accountview

import schemact.domain.Entity
import schemact.domain.Function
import schemact.domain.InfrastructureInjectables
import schemact.domain.ReactJsInjectables
import schemact.domain.int
import schemact.domain.string

val onLoginFunction = schemact.domain.Function(
    "onLogin",
    description = "updates the auth table on login",
    paramType = Entity(name = "param", description = "Params") {
        containsOne(
            "userTableName",
            description = "bucketName",
            type = InfrastructureInjectables.DynamoDBTablenameType
        )
        containsOne(
            "bucketName",
            description = "bucketName",
            type = InfrastructureInjectables.BucketNameType
        )
        containsOne(
            "Authorization",
            description = "Authorization header",
            type = InfrastructureInjectables.AuthorizationHeaderType
        )
        containsOne(
            "cognitoDetails", description = "Cognito Details",
            type = InfrastructureInjectables.CognitoClientDetails.entity
        )
    },
    returnType = userInfo1,
    auth = auth
)

val smallUploadFile = ReactJsInjectables.File(name ="File",
    description="Random Uploaded File", maxBytes=10000000)

val uploadFileFunction = schemact.domain.Function(
    "uploadFile",
    description = "uploads a file",
    paramType = Entity(name = "param", description = "Params") {
        containsOne(
            "userTableName",
            description = "bucketName",
            type = InfrastructureInjectables.DynamoDBTablenameType
        )
        containsOne(
            "privateBucketName",
            description = "bucketName",
            type = InfrastructureInjectables.PrivateBucketNameType
        )
        containsOne(
            "Authorization",
            description = "Authorization header",
            type = InfrastructureInjectables.AuthorizationHeaderType
        )
        containsOne(
            "cognitoDetails", description = "Cognito Details",
            type = InfrastructureInjectables.CognitoClientDetails.entity
        )
        containsOne(name = "file", "upload file", smallUploadFile)
// for debug / development
        containsOne(
            "input", description = "native input details",
            type = InfrastructureInjectables.APIGatewayV2HTTPEventEntity
        )
    },
    returnType = userInfo1,
    auth = auth
)

val uploadTransactionGroupFunction = schemact.domain.Function(
    "uploadTransactionGroup",
    description = "uploads a transactionGroup",
    paramType = Entity(name = "param", description = "Params") {
        containsOne(
            "userTableName",
            description = "bucketName",
            type = InfrastructureInjectables.DynamoDBTablenameType
        )
        containsOne(
            "privateBucketName",
            description = "bucketName",
            type = InfrastructureInjectables.PrivateBucketNameType
        )
        containsOne(
            "Authorization",
            description = "Authorization header",
            type = InfrastructureInjectables.AuthorizationHeaderType
        )
        containsOne(
            "cognitoDetails", description = "Cognito Details",
            type = InfrastructureInjectables.CognitoClientDetails.entity
        )
        containsOne(name = "file", description = "upload file", type = smallUploadFile)
        string("fromInclusiveDate", "From Inclusive Date", maxLength = 10)
        string("toInclusiveDate", "To Inclusive Date", maxLength = 10)
        string("accountNumber", "AccountNumber", maxLength = 20)

    },
    returnType = userInfo1,
    auth = auth
)

val getTransactionGroup = schemact.domain.Function(
    "getTransactionGroup",
    description = "gets a transactionGroup",
    paramType = Entity(name = "param", description = "Params") {
        containsOne(
            "userTableName",
            description = "bucketName",
            type = InfrastructureInjectables.DynamoDBTablenameType
        )
        containsOne(
            "privateBucketName",
            description = "bucketName",
            type = InfrastructureInjectables.PrivateBucketNameType
        )
        containsOne(
            "Authorization",
            description = "Authorization header",
            type = InfrastructureInjectables.AuthorizationHeaderType
        )
        containsOne(
            "cognitoDetails",
            description = "Cognito Details",
            type = InfrastructureInjectables.CognitoClientDetails.entity
        )
        string("fromInclusiveDate", "From Inclusive Date", maxLength = 10)
        string("toInclusiveDate", "To Inclusive Date", maxLength = 10)
        string("accountNumber", "AccountNumber", maxLength = 20)
    },
    returnType = transactionGroup,
    auth = auth
)

val categorizeTransactions = schemact.domain.Function(
    "categorizeTransactions",
    description = "gets a transactionGroup",
    paramType = Entity(name = "param", description = "Params") {
        containsOne(
            "userTableName",
            description = "bucketName",
            type = InfrastructureInjectables.DynamoDBTablenameType
        )
        containsOne(
            "privateBucketName",
            description = "bucketName",
            type = InfrastructureInjectables.PrivateBucketNameType
        )
        containsOne(
            "Authorization",
            description = "Authorization header",
            type = InfrastructureInjectables.AuthorizationHeaderType
        )
        containsOne(
            "cognitoDetails",
            description = "Cognito Details",
            type = InfrastructureInjectables.CognitoClientDetails.entity
        )
        string("fromInclusiveDate", "From Inclusive Date", maxLength = 10)
        string("toInclusiveDate", "To Inclusive Date", maxLength = 10)
        string("accountNumber", "AccountNumber", maxLength = 20)
        containsMany(
            "transactionUpdates",
            "transaction updates",
            type = Entity(name = "TransactionUpdate", description = "Transaction Update") {
                int("index", "index in file")
                containsOne("transaction", description = "Transaction", type = transaction)
            })
    },
    returnType = transactionGroup,
    auth = auth
)

val addAccountFunction = Function(
    "addAccount",
    description = "adds an account",
    paramType = Entity(name = "param", description = "Params") {
        containsOne(
            "userTableName",
            description = "table name",
            type = InfrastructureInjectables.DynamoDBTablenameType
        )
        containsOne(
            "privateBucketName",
            description = "bucketName",
            type = InfrastructureInjectables.PrivateBucketNameType
        )
        containsOne(
            "Authorization",
            description = "Authorization header",
            type = InfrastructureInjectables.AuthorizationHeaderType
        )
        containsOne(
            "cognitoDetails",
            description = "Cognito Details",
            type = InfrastructureInjectables.CognitoClientDetails.entity
        )
        containsOne("account", description = "Account", type = account)

    },
    returnType = userInfo1,
    auth = auth
)
