package schemact.gradleplugin.injection


import RestResolvers.restBodyElementResolver
import RestResolvers.restBodyResolver
import RestResolvers.restMultiBodyElementResolver
import RestResolvers.restParameterResolver
import schemact.domain.Entity
import schemact.domain.InfrastructureInjectables
import schemact.gradleplugin.injection.ResolverUtil.string2ObjectKotlin
import schemact.gradleplugin.injection.mappers.createMultiPartBodyReader
import schemact.gradleplugin.injection.mappers.createReadUserPrivateBucketData
import schemact.gradleplugin.injection.mappers.createUserDataUpdaterResolver
import schemact.gradleplugin.injection.mappers.createWriteUserPrivateBucketData
import schemact.gradleplugin.injection.mappers.verifyUserSession

object AwsResolvers {


    val PrivateBucketNameResolver = SystemPropertyResolver(InfrastructureInjectables.PrivateBucketNameType)
    val BucketNameResolver = SystemPropertyResolver(InfrastructureInjectables.BucketNameType)
    val DynamoDBTablenameResolver = AwsResolvers.SystemPropertyResolver( InfrastructureInjectables.DynamoDBTablenameType)
    val CognitoClientDetailsResolver = AwsResolvers.SystemPropertyResolver(InfrastructureInjectables.CognitoClientDetails.entity)

    val WriteUserPrivateBucketDataResolver =
        fromMapperFunction(mapperFunction = createWriteUserPrivateBucketData)

    val ReadUserPrivateBucketDataResolver =
        fromMapperFunction(mapperFunction = createReadUserPrivateBucketData)

    val VerifiedUserResolver =
        fromMapperFunction(mapperFunction = verifyUserSession)
    val UpdateUserDataResolver = createUserDataUpdaterResolver

    val MultiPartBodyReaderResolver  =
        fromMapperFunction(mapperFunction = createMultiPartBodyReader)

    val RawInputResolverResolver =
        object : Resolver () {
            override fun resolve(value: Value): List<Value.Requirement>? {

                if (value.connectionFrom.entity2 == InfrastructureInjectables.APIGatewayV2HTTPEventEntity) {
                    value.renderer = object : Renderer() {
                        override fun renderKotlin(value: Value, dependencies: Map<String, Value>) =
                            """val ${value.varName} = input"""
                    }
                    value.requirements = emptyList()
                    return value.requirements
                } else return null
            }
        }

    val allNonRestResolvers = listOf(PrivateBucketNameResolver, BucketNameResolver, DynamoDBTablenameResolver, CognitoClientDetailsResolver,
        WriteUserPrivateBucketDataResolver, ReadUserPrivateBucketDataResolver, VerifiedUserResolver, UpdateUserDataResolver,
        RawInputResolverResolver, MultiPartBodyReaderResolver)

    // TODO rest resolver exclusions should be auto added
    val restMultiBodyElementResolver = restMultiBodyElementResolver(exclusions = allNonRestResolvers
        .toSet().plus(AwsAuthHeaderResolver))


    val RestBodyParamResolver = restBodyElementResolver(exclusions = allNonRestResolvers
        .toSet().plus(AwsAuthHeaderResolver).plus(restBodyResolver).plus(restMultiBodyElementResolver))


    val RestParamResolvers = listOf(restParameterResolver(exclusions = allNonRestResolvers.toSet()))
        .plus(restMultiBodyElementResolver)
        .plus(RestBodyParamResolver)
        .plus(restBodyResolver)
        .plus(AwsAuthHeaderResolver)


    val LambdaResolvers = allNonRestResolvers.plus(RestParamResolvers)
        //.plus(AwsAuthHeaderResolver)


    fun SystemPropertyResolver(propertyType: Entity) =
        object : Resolver () {
            override fun resolve(value: Value): List<Value.Requirement>? {

                if (value.connectionFrom.entity2 == propertyType) {
                    value.renderer = object : Renderer() {
                        override fun renderKotlin(value: Value, dependencies: Map<String, Value>) : String{
                            return if (propertyType.isValueType)  """val ${value.varName} = System.getenv("${value.connectionFrom.name}")"""
                            else """val ${value.varName} = ${string2ObjectKotlin("""System.getenv("${value.connectionFrom.name}")""", value.connectionFrom.entity2)}"""
                        }
                    }
                    value.requirements = emptyList()
                    return value.requirements
                } else return null
            }
        }



}

