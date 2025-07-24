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

    val allNonRestResolvers = listOf(PrivateBucketNameResolver, DynamoDBTablenameResolver, CognitoClientDetailsResolver, WriteUserPrivateBucketDataResolver, VerifiedUserResolver)
    val RestParamResolver = restParameterResolver(exclusions = allNonRestResolvers.toSet())
    val LambdaResolvers = allNonRestResolvers.plus(RestParamResolver)

    private fun string2ObjectKotlin( expression: String) = "ObjectMapper().readValue($expression)"

    fun SystemPropertyResolver(propertyName: String, propertyType: Entity) =
        object : Resolver () {
            override fun resolve(value: Value): List<Value.Requirement>? {

                if (value.connectionFrom.entity2 == propertyType) {
                    value.renderer = object : Renderer() {
                        override fun renderKotlin(value: Value, dependencies: Map<String, Value>) : String{
                            return if (propertyType.isValueType)  """val ${value.varName} = System.getenv("${propertyName}")"""
                            else """val ${value.varName} = ${string2ObjectKotlin("""System.getenv("${propertyName}""")}"""
                        }
                        override fun requiredImports(): List<String> =  emptyList()
                       // override fun supportFunctions(): String = ""
                    }
                    value.requirements = emptyList()
                    return value.requirements
                } else return null
            }

        }


    // need reference to other resolvers to exclude
    fun restParameterResolver(exclusions: Set<Resolver>) =
        object : Resolver () {
            override fun resolve(value: Value, /*expansionLevel: Int,*/ ): List<Value.Requirement>? {

                if (!exclusions.any{it.resolve(value)!=null}) {
                    value.renderer = object : Renderer() {
                        override fun renderKotlin(value: Value, dependencies: Map<String, Value>) : String{
                            val propertyType = value.connectionFrom.entity2
                            val propertyName = value.connectionFrom.name
                            return if (propertyType.isValueType)  """val ${value.varName} = input.queryStringParameters.get("${propertyName}")"""
                            else """val ${value.varName} = ${string2ObjectKotlin("""input.queryStringParameters.get(("${propertyName}""")}"""
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

