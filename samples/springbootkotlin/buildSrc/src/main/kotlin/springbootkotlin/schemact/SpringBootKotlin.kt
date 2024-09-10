package springbootkotlin.schemact

import schemact.domain.*
import schemact.domain.Function
import schemact.domain.Language.Typescript
import schemact.domain.Module
import schemact.domain.string

val rootDomain =  Domain(name = "testedsoftware.org",
    wildcardCertificateRef = "arn:aws:acm:us-east-1:950651224730:certificate/78fab14f-b918-42cd-bb4f-2bea3153d252",
    cdnZoneReference = "Z2FDTNDATAQYW2",
    deployments = listOf(
    Deployment(subdomain = "devspringboot", codeBranch = "dev")
))

val helloWorldFunction = Function("helloWorld",
    description = "just a test",
    paramType = Entity(name="param", description="Params" ) {
        string(name="greetin", description="the greeting", maxLength = 100)    },
    returnType = StringType(200)
)

val springbootappModule = Module(name= "springbootapp",
    version = "1.0.01-SNAPSHOT",
    type = Module.Type.SpringBootApplication,
    functions = mutableListOf(helloWorldFunction))

val springbootkotlin = Schemact(
name = "springbootkotlin",
domains = listOf(rootDomain),
entities = mutableListOf(),
modules = mutableListOf(springbootappModule)
) {
}



