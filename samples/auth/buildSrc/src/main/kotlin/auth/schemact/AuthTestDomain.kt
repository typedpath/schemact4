package auth.schemact

import schemact.domain.*

val placeHolderFunction = Function("placeHolderF",
    description = "accepts an svg and creates an open graph friendly paramicon index page, returns index page url",
    paramType = Entity(name="param", description="Params" ) {
        string(name="svg", description="svg for rendering", maxLength = 4000)
        string(name="editParams", description="ui params for editing paramicon", maxLength = 500)
        containsOne("bucketName", description="bucketName", type=StaticWebsite.BucketName())
    },
    returnType = StringType(200)
)

val functionsModule = Module(name= "functions",
    version = "1.0.22-SNAPSHOT",
    functions = mutableListOf(placeHolderFunction))

val rootDomain =  Domain(name = "testedsoftware.org",
    wildcardCertificateRef = "arn:aws:acm:us-east-1:950651224730:certificate/78fab14f-b918-42cd-bb4f-2bea3153d252",
    cdnZoneReference = "Z2FDTNDATAQYW2",
    deployments = listOf(
    Deployment(subdomain = "devauthsample", codeBranch ="dev"),
))

val authsample = Schemact(
name = "authsample",
domains = listOf(rootDomain),
    //modules = mutableListOf(functionsModule),
auth =  Auth()
    ) {

}



