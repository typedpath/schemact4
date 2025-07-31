package schemact.gradleplugin.injection


import schemact.domain.Entity
import schemact.domain.InfrastructureInjectables
import schemact.gradleplugin.injection.mappers.createReadUserPrivateBucketData
import schemact.gradleplugin.injection.mappers.createUserDataUpdaterMapperFunction
import schemact.gradleplugin.injection.mappers.createUserDataUpdaterResolver
import schemact.gradleplugin.injection.mappers.createWriteUserPrivateBucketData
import schemact.gradleplugin.injection.mappers.verifyUserSession

object AwsResolvers {


    val PrivateBucketNameResolver = SystemPropertyResolver(InfrastructureInjectables.PrivateBucketNameType.name,InfrastructureInjectables.PrivateBucketNameType)
    val DynamoDBTablenameResolver = AwsResolvers.SystemPropertyResolver(InfrastructureInjectables.DynamoDBTablenameType.name, InfrastructureInjectables.DynamoDBTablenameType)
    val CognitoClientDetailsResolver = AwsResolvers.SystemPropertyResolver(InfrastructureInjectables.CognitoClientDetails.entity.name, InfrastructureInjectables.CognitoClientDetails.entity)

    val WriteUserPrivateBucketDataResolver =
        fromMapperFunction(mapperFunction = createWriteUserPrivateBucketData)

    val ReadUserPrivateBucketDataResolver =
        fromMapperFunction(mapperFunction = createReadUserPrivateBucketData)

    val VerifiedUserResolver =
        fromMapperFunction(mapperFunction = verifyUserSession)
    val UpdateUserDataResolver = createUserDataUpdaterResolver
       // fromMapperFunction(mapperFunction = createUserDataUpdaterMapperFunction)

    val allNonRestResolvers = listOf(PrivateBucketNameResolver, DynamoDBTablenameResolver, CognitoClientDetailsResolver,
        WriteUserPrivateBucketDataResolver, ReadUserPrivateBucketDataResolver, VerifiedUserResolver, UpdateUserDataResolver)
    val RestParamResolver = restParameterResolver(exclusions = allNonRestResolvers.toSet().plus(AwsAuthHeaderResolver))
    val LambdaResolvers = allNonRestResolvers.plus(RestParamResolver).plus(AwsAuthHeaderResolver)

    private fun string2ObjectKotlin( expression: String, entity: Entity) = "ObjectMapper().readValue($expression, ${entity.prefferedPackage?.let{"$it."}?:""}${entity.name}::class.java)"

    fun SystemPropertyResolver(propertyName: String, propertyType: Entity) =
        object : Resolver () {
            override fun resolve(value: Value): List<Value.Requirement>? {

                if (value.connectionFrom.entity2 == propertyType) {
                    value.renderer = object : Renderer() {
                        override fun renderKotlin(value: Value, dependencies: Map<String, Value>) : String{
                            return if (propertyType.isValueType)  """val ${value.varName} = System.getenv("${propertyName}")"""
                            else """val ${value.varName} = ${string2ObjectKotlin("""System.getenv("${propertyName}")""", value.connectionFrom.entity2)}"""
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
                            // TODO handle optionality !
                            return if (propertyType.isValueType)  """val ${value.varName} = input.queryStringParameters.get("${propertyName}")!!"""
                            else """val ${value.varName} = ${string2ObjectKotlin("""input.queryStringParameters.get("${propertyName}")!!""", value.connectionFrom.entity2)}"""
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

