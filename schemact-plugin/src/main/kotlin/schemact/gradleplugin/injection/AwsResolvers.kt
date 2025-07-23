package schemact.gradleplugin.injection

import schemact.domain.Entity
import schemact.domain.InfrastructureInjectables
import schemact.domain.InfrastructureInjectables.WriteUserPrivateBucketData

object AwsResolvers {


    val PrivateBucketNameResolver = SystemPropertyResolver(InfrastructureInjectables.PrivateBucketNameType.name,InfrastructureInjectables.PrivateBucketNameType)
    val DynamoDBTablenameResolver = AwsResolvers.SystemPropertyResolver(InfrastructureInjectables.DynamoDBTablenameType.name, InfrastructureInjectables.DynamoDBTablenameType)
    val CognitoClientDetailsResolver = AwsResolvers.SystemPropertyResolver(InfrastructureInjectables.CognitoClientDetails.entity.name, InfrastructureInjectables.CognitoClientDetails.entity)
    /*val VerifiedUserResolver =  object : Resolver () {
        override fun resolve(value: Value): List<Value.Requirement>? {
            val transformName = "verifyCognitoUser"
            if (value.connectionFrom.entity2 == VerifiedCognitoUser) {
                val authHeaderRequirement = requirement(from = VerifiedCognitoUser, to= InfrastructureInjectables.AuthorizationHeaderType, name="token")
                val cognitoClientDetailsRequirement = requirement(from = VerifiedCognitoUser, to= InfrastructureInjectables.CognitoClientDetails.entity, name="cognitoClientDetails")

                value.renderer = object : Renderer() {
                    override fun renderKotlin(value: Value, dependencies: Map<String, Value>) : String =
                         "val ${value.varName} = $transformName(${dependencies.map { (key, value) -> "$key=${value.varName}" }.joinToString(", ")})"

                    override fun requiredImports(): List<String> =  emptyList()
                }
                value.requirements = listOf(authHeaderRequirement, cognitoClientDetailsRequirement)
                return value.requirements
            } else return null
        }

    }*/

    val WriteUserPrivateBucketDataResolver =
       //fromEntity(entity = WriteUserPrivateBucketData/*, transformName = "createWriteUserPrivateBucketData"*/)
        fromMapperFunction(mapperFunction = createWriteUserPrivateBucketData)
    val VerifiedUserResolver =
        fromMapperFunction(mapperFunction = verifyUserSession)

    fun SystemPropertyResolver(propertyName: String, propertyType: Entity) =
        object : Resolver () {
            override fun resolve(value: Value): List<Value.Requirement>? {

                if (value.connectionFrom.entity2 == propertyType) {
                    value.renderer = object : Renderer() {
                        override fun renderKotlin(value: Value, dependencies: Map<String, Value>) : String{
                            return if (propertyType.isValueType)  """val ${value.varName} = System.getenv("${propertyName}")"""
                            else """val ${value.varName} = ObjectMapper().readValue(System.getenv("${propertyName}"))"""
                        }
                        override fun requiredImports(): List<String> =  emptyList()
                       // override fun supportFunctions(): String = ""
                    }
                    value.requirements = emptyList()
                    return value.requirements
                } else return null
            }

        }



}

