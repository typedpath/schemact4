package schemact.domain

import schemact.domain.InfrastructureInjectables.AwsPackage

object RestInjectables {
val MultiPartBodyReader  = Entity(name = "MultiPartBodyReader", description = "multi part body reader",
        prefferedPackage= AwsPackage,
    ) {
        nativeDefinition = Entity.NativeDefinition(kotlin=
"""import schemact.react.File
interface MultiPartBodyReader {
    fun get(paramName: String) : String
    fun getAsFile(paramName: String): File
}
""")
    }
val ContentTypeHeaderReader = Entity(name = "ContentTypeHeaderReader", description = "content type reader")

    object  AuthorizationHeaderType : StringType(maxLength = 2000, name = "AuthorizationHeader") {
        init {
            isFromHeader = true
        }
    }
}