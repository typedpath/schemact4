package golambda.schemact

import schemact.domain.*
import schemact.domain.Function
import schemact.domain.Module
import schemact.domain.string

val rootDomain =  Domain(name = "testedsoftware.org",
    wildcardCertificateRef = "arn:aws:acm:us-east-1:950651224730:certificate/78fab14f-b918-42cd-bb4f-2bea3153d252",
    cdnZoneReference = "Z2FDTNDATAQYW2",
    deployments = listOf(
    Deployment(subdomain = "golambda", codeBranch = "dev")
))

val helloWorldFunction = Function("helloWorld",
    description = "just a test",
    paramType = Entity(name="param", description="Params" ) {
        string(name="name", description="the name", maxLength = 100)    },
    returnType = StringType(300)
)

val functionsModule = Module(name= "gofunctions",
    version = "1.0.03-SNAPSHOT",
    type = Module.Type.GoStandaloneFunction,
    functions = mutableListOf(helloWorldFunction))

val golambda = Schemact(
name = "golambda",
domains = listOf(rootDomain),
entities = mutableListOf(),
modules = mutableListOf(functionsModule)
) {
}



