package samples.accountview

import schemact.domain.*
import schemact.domain.Language.Typescript


val auth =  Auth()

val file = Entity(name="File",
    "uploaded file") {
    string("filename", "file name", maxLength = 2000)
    string("location", "where in the s3", maxLength = 2000)
    string("contentType", "what i sit", maxLength = 2000)
    string("uploadTime", "uploadTime", maxLength = 2000)
}

val transactionGroup = Entity(name="TransactionGroup", description="Transaction Group" ) {
    string("fromInclusiveDate", "From Inclusive Date", maxLength = 2000)
    string("toInclusiveDate", "To Inclusive Date", maxLength = 2000)
    containsOne("rawTransactionFile", "Raw Transaction File", type = file)
    // containsOne("categorizedTransactionFile", "Categorized Transaction File", type = file)
}

val account = Entity(name="Account","accounts") {
    string("name", "file name", maxLength = 200)
    string("sortCode", "where in the s3", maxLength = 10)
    string("accountNumber", "what i sit", maxLength = 16)
    containsMany("transactionGroups", "Transaction Groups", type= transactionGroup)
}

val userInfo = Entity(name = "UserInfo", description="UserInfo") {
    containsMany(name = "loginEvents", type = StringType(maxLength=200))
    containsMany(name = "uploads", type = file)
    containsMany(name= "accounts", type= account)
}

val onLoginFunction = Function("onLogin",
    description = "updates the auth table on login",
    paramType = Entity(name="param", description="Params" ) {
        containsOne("userTableName", description="bucketName", type=InfrastructureInjectables.DynamoDBTablenameType)
        containsOne("bucketName", description="bucketName", type=InfrastructureInjectables.BucketNameType)
        containsOne("Authorization", description="Authorization header", type=InfrastructureInjectables.AuthorizationHeaderType)
        containsOne("cognitoDetails", description="Cognito Details",
            type=InfrastructureInjectables.CognitoClientDetails.entity)
    },
    returnType = userInfo,
    auth = auth
)

val uploadFileFunction = Function("uploadFile",
    description = "uploads a file",
    paramType = Entity(name="param", description="Params" ) {
        containsOne("userTableName", description="bucketName", type=InfrastructureInjectables.DynamoDBTablenameType)
        containsOne("privateBucketName", description="bucketName", type=InfrastructureInjectables.PrivateBucketNameType)
        containsOne("Authorization", description="Authorization header", type=InfrastructureInjectables.AuthorizationHeaderType)
        containsOne("cognitoDetails", description="Cognito Details",
            type=InfrastructureInjectables.CognitoClientDetails.entity)
        containsOne(name ="file", "upload file", ReactJsInjectables.File(maxBytes=10000000))
// for debug / development
        containsOne("input", description="native input details",
            type=InfrastructureInjectables.APIGatewayV2HTTPEventEntity)
    },
    returnType = userInfo,
    auth = auth
)

val addAccountFunction = Function("addAccount",
    description = "adds an account",
    paramType = Entity(name="param", description="Params" ) {
        containsOne("userTableName", description="table name", type=InfrastructureInjectables.DynamoDBTablenameType)
        containsOne("privateBucketName", description="bucketName", type=InfrastructureInjectables.PrivateBucketNameType)
        containsOne("Authorization", description="Authorization header", type=InfrastructureInjectables.AuthorizationHeaderType)
        containsOne("cognitoDetails", description="Cognito Details", type=InfrastructureInjectables.CognitoClientDetails.entity)
        containsOne("account", description="Account", type= account)

    },
    returnType = userInfo,
    auth = auth
)

val functionsModule = Module(name= "functions",
    version = "1.0.44-SNAPSHOT",
    functions = mutableListOf(onLoginFunction, uploadFileFunction, addAccountFunction))

val defaultDeployment = Deployment(subdomain = "accountview", codeBranch ="dev")

val rootDomain =  Domain(name = "testedsoftware.org",
    wildcardCertificateRef = "arn:aws:acm:us-east-1:950651224730:certificate/78fab14f-b918-42cd-bb4f-2bea3153d252",
    cdnZoneReference = "Z2FDTNDATAQYW2",
    deployments = listOf(defaultDeployment))

lateinit var  mainPage : StaticWebsite

val accountview = Schemact(
name = "accountview",
    domains = listOf(rootDomain),
    modules = mutableListOf(functionsModule),
    userKeyedDatabase = UserKeyedDatabase(userInfo),
    defaultLocalClientDeployment = defaultDeployment,
    privateBucket = PrivateBucket(),
    auth =  auth
    ) {
    mainPage = staticWebsite("mainPage", "the main page") {
        client(onLoginFunction, Typescript)
        client(uploadFileFunction, Typescript)
        client(addAccountFunction, Typescript)
    }
}



