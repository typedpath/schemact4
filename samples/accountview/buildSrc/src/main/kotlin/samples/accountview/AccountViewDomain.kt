package samples.accountview

import schemact.domain.*
import schemact.domain.Language.Typescript

val functionModuleVersion="1.0.101-SNAPSHOT"

val auth =  Auth()

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
    userKeyedDatabase = UserKeyedDatabase(userInfoType = userInfoLatest, previousUserInfoTypes = listOf(userInfo0, userInfo1, userInfo2)),
    defaultLocalClientDeployment = defaultDeployment,
    privateBucket = PrivateBucket(),
    auth =  auth
    ) {
    mainPage = staticWebsite("mainPage", "the main page") {
        client(onLoginFunction, Typescript)
        client(uploadFileFunction, Typescript)
        client(addAccountFunction, Typescript)
        client(uploadTransactionGroupFunction, Typescript)
        client(getTransactionGroup, Typescript)
        client(getTransactionGroup2, Typescript)
        client(categorizeTransactions, Typescript)
        client(saveCategoriesFunction, Typescript)
        client(saveAutoCatFilters, Typescript)
    }
}



