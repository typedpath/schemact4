package schemact.gradleplugin.injection

import schemact.domain.Entity
import schemact.domain.InfrastructureInjectables
import schemact.gradleplugin.injection.mappers.createWriteUserPrivateBucketData
import schemact.gradleplugin.injection.mappers.verifyUserSession

object AwsResolvers {


    val PrivateBucketNameResolver = SystemPropertyResolver(InfrastructureInjectables.PrivateBucketNameType.name,InfrastructureInjectables.PrivateBucketNameType)
    val DynamoDBTablenameResolver = AwsResolvers.SystemPropertyResolver(InfrastructureInjectables.DynamoDBTablenameType.name, InfrastructureInjectables.DynamoDBTablenameType)
    val CognitoClientDetailsResolver = AwsResolvers.SystemPropertyResolver(InfrastructureInjectables.CognitoClientDetails.entity.name, InfrastructureInjectables.CognitoClientDetails.entity)

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

