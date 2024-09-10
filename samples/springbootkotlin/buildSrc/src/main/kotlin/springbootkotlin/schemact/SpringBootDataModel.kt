package springbootkotlin.schemact

import schemact.domain.*

val metaDataEntity = Entity(name="metadata", description="describe params") {
    string("id", "identifier", 10)
    string("description", "Description", 100)
    // properties - TODO implicit in entity definition
    //defaultParams
    /*
    how to do these ?
    properties:  Property[];
    // this is a circular ref
    defaultParams: MultiShapeParams;
    // these is in the client
    render: (ps: MultiShapeParams) => JSX.Element;
    sampleParams: MultiShapeParams[];
    how to do this ?
    animationParams: ((params: MultiShapeParams) => AnimationParams) | null;
 */
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

val polarRadialCon2Params = Entity(name="polarRadialCon2Params", description = "polarRadialCon2Params",
    parent = paramsEntity
) {
    int("rotationFrequency", "TODO")
    int("radialFrequency", "TODO")
    int("spokeAngle0", "TODO")
    int("spokeAngle1", "TODO")
    int("spokeUnit", "TODO")
    bool("radialColourGradient", "TODO")

}