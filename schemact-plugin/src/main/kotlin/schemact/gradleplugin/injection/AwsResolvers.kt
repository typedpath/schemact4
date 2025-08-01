package schemact.gradleplugin.injection


import RestResolvers.restBodyElementResolver
import RestResolvers.restBodyResolver
import RestResolvers.restParameterResolver
import schemact.domain.Entity
import schemact.domain.InfrastructureInjectables
import schemact.gradleplugin.injection.ResolverUtil.string2ObjectKotlin
import schemact.gradleplugin.injection.mappers.createReadUserPrivateBucketData
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

    val allNonRestResolvers = listOf(PrivateBucketNameResolver, DynamoDBTablenameResolver, CognitoClientDetailsResolver,
        WriteUserPrivateBucketDataResolver, ReadUserPrivateBucketDataResolver, VerifiedUserResolver, UpdateUserDataResolver)

    val RestBodyParamResolver = restBodyElementResolver(exclusions = allNonRestResolvers
        .toSet().plus(AwsAuthHeaderResolver).plus(restBodyResolver))

    val RestParamResolvers = listOf(restParameterResolver(exclusions = allNonRestResolvers.toSet()))
        .plus(RestBodyParamResolver)
        .plus(restBodyResolver)
        .plus(AwsAuthHeaderResolver)


    val LambdaResolvers = allNonRestResolvers.plus(RestParamResolvers)
        .plus(AwsAuthHeaderResolver)


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

}

