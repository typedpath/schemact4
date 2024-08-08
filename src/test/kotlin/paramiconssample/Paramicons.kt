package paramiconssample

import domain.*
import domain.Function
import domain.string

val rootDomain =  Domain(name = "testedsoftware.org", wildcardCertificateRef = "sampleWildcertRef", deployments = listOf(
    Deployment("paramicons"),
    Deployment("devparamicons")
))

val thumbnailerFunction = Function("svgThumbnailer",
    description = "accepts an svg and creates an open graph friendly paramicon index page, returns index page url",
    environment = mapOf("bucketName" to "TODO injected forward reference"),
    paramType = Entity(name="param", description="Params" ) {
        string(name="svg", description="svg for rendering", maxLength = 4000)
    },
    returnType = StringType(200)
)

val metaDataEntity = Entity(name="metadata", description="describe params") {
//TODO
}

val paramsEntity = Entity(name="params", description="paramiconparams") {
    // no id column - that comes from global policy
    referencesOne("metaData", "Meta Data", to= metaDataEntity)
    string("highlightedProperty", "property that is highlighted", 30)
    bool("loading", "is loading animation on?")
    int("unit", "TODO")
    int("pageHeight", "TODO")
    int("pageWidth", "TODO")
    containsMany("fillColours" , "Fill Colours", StringType(maxLength = 10))
    float("angleDegrees", "orientation !!")
    //TODO - subclasses !

}

val paramiconsInfrastructure = Schemact(
domains = listOf(rootDomain),
functions = mutableListOf(thumbnailerFunction),
entities = mutableListOf(metaDataEntity, paramsEntity)
) {
    staticWebsite("homepage", "the main page")
}



