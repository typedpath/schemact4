package paramiconssample

import domain.*
import domain.Function
import domain.string

val rootDomain =  Domain(name = "testedsoftware.org", wildcardCertificateRef = "sampleWildcertRef", deployments = listOf(
    Deployment("paramicons"),
    Deployment("devparamicons")
))


val paramicons = Schemact(
domains = listOf(rootDomain),
entities = mutableListOf(metaDataEntity, paramsEntity),
userKeyedDatabase = UserKeyedDatabase(entities=mutableListOf(metaDataEntity, paramsEntity))
) {
    val sw = staticWebsite("homepage", "the main page")
    val thumbnailerFunction = Function("svgThumbnailer",
        description = "accepts an svg and creates an open graph friendly paramicon index page, returns index page url",
        environment = mutableMapOf("bucketName" to sw.bucketNameReference()),
        paramType = Entity(name="param", description="Params" ) {
            string(name="svg", description="svg for rendering", maxLength = 4000)
        },
        returnType = StringType(200)
    )
    function(thumbnailerFunction)
}



