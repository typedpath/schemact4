package samples.accountview

import schemact.domain.*
import schemact.domain.Language.Typescript


val auth =  Auth()
val onLoginFunction = Function("onLogin",
    description = "updates the auth table on login",
    paramType = Entity(name="param", description="Params" ) {
        containsOne("userTableName", description="bucketName", type=InfrastructureInjectables.DynamoDBTablenameType)
        containsOne("bucketName", description="bucketName", type=InfrastructureInjectables.BucketNameType)
        containsOne("Authorization", description="Authorization header", type=InfrastructureInjectables.AuthorizationHeaderType)
        containsOne("cognitoDetails", description="Cognito Details",
            type=InfrastructureInjectables.CognitoClientDetails.entity)
    },
    returnType = StringType(200),
    auth = auth
)

val functionsModule = Module(name= "functions",
    version = "1.0.26-SNAPSHOT",
    functions = mutableListOf(onLoginFunction))

val defaultDeployment = Deployment(subdomain = "accountview", codeBranch ="dev")

val rootDomain =  Domain(name = "testedsoftware.org",
    wildcardCertificateRef = "arn:aws:acm:us-east-1:950651224730:certificate/78fab14f-b918-42cd-bb4f-2bea3153d252",
    cdnZoneReference = "Z2FDTNDATAQYW2",
    deployments = listOf(defaultDeployment))

lateinit var  mainPage : StaticWebsite

val userInfo = Entity(name = "UserInfo", description="UserInfo") {
  containsMany(name = "loginEvents", type = StringType(maxLength=200))
}

val accountview = Schemact(
name = "accountview",
    domains = listOf(rootDomain),
    modules = mutableListOf(functionsModule),
    userKeyedDatabase = UserKeyedDatabase(userInfo),
    defaultLocalClientDeployment = defaultDeployment,
    auth =  auth
    ) {
    mainPage = staticWebsite("mainPage", "the main page") {
        client(onLoginFunction, Typescript)
    }
}



