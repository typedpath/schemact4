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

val helloWorldExtraFunction = Function("helloWorldExtra",
    description = "just a test",
    paramType = Entity(name="param", description="Params" ) {
        containsOne(name="protagonist", type = Entity(name = "name", "a persons name") {
            string("firstName", maxLength = 100)
            string("middleNames", maxLength = 500, optional = true)
            string("lastName", maxLength = 100)
        })
    },
    returnType = Entity(name = "GreetingPolicy", "Greeting Policy") {
        string(name ="hello", maxLength = 500)
        string(name= "goodbye", maxLength = 500)
    }
)


val functionsModule = Module(name= "gofunctions",
    version = "1.0.14-SNAPSHOT",
    type = Module.Type.GoStandaloneFunction,
    functions = mutableListOf(helloWorldFunction, helloWorldExtraFunction))

val externalTestModule = Module(name= "externaltest",
    version = "1.0.04-SNAPSHOT",
    type = Module.Type.StandaloneFunction,
    ) {
       client(helloWorldFunction, Language.Kotlin)
       client(helloWorldExtraFunction, Language.Kotlin)
}

val golambda = Schemact(
name = "golambda",
domains = listOf(rootDomain),
entities = mutableListOf(),
modules = mutableListOf(functionsModule)
) {
}



