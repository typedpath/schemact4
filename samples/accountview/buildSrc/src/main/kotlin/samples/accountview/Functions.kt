package samples.accountview

import schemact.domain.Entity
import schemact.domain.Function
import schemact.domain.InfrastructureInjectables
import schemact.domain.Module
import schemact.domain.ReactJsInjectables
import schemact.domain.StringType
import schemact.domain.int
import schemact.domain.string

val userInfoLatest = userInfo3
val transactionGroupLatest = transactionGroup3
val accountLatest = account3

val onLoginFunction = Function(
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
    returnType = userInfoLatest,
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
    returnType = userInfoLatest,
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
    returnType = userInfoLatest,
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
    returnType = transactionGroupLatest,
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
    returnType = transactionGroupLatest,
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
        containsOne("account", description = "Account", type = accountLatest)

    },
    returnType = userInfoLatest,
    auth = auth
)

val saveCategoriesFunction = Function(
    "saveCategories",
    description = "saves categories",
    paramType = Entity(name = "param", description = "Params") {
        containsOne(
            "userTableName",
            description = "table name",
            type = InfrastructureInjectables.DynamoDBTablenameType
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
        containsMany("categories", description = "cats", type = StringType(maxLength = 50))
    },
    returnType = userInfoLatest,
    auth = auth
)

val saveAutoCatFilters = Function(
    "saveAutoCatFilters",
    description = "saves autoCatFilters",
    paramType = Entity(name = "param", description = "Params") {
        containsOne(
            "userTableName",
            description = "table name",
            type = InfrastructureInjectables.DynamoDBTablenameType
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
        containsMany("autoCatFilters", description = "autoCatFilters", type =autoCatFilter)
    },
    returnType = userInfoLatest,
    auth = auth
)



val functionsModule = Module(name= "functions",
    version = functionModuleVersion,
    functions = mutableListOf(onLoginFunction, uploadFileFunction,
        addAccountFunction, uploadTransactionGroupFunction, getTransactionGroup,
        categorizeTransactions, saveCategoriesFunction, saveAutoCatFilters))
