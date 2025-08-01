package schemact.gradleplugin.injection

import schemact.domain.Entity
import schemact.domain.Function
import schemact.domain.InfrastructureInjectables.APIGatewayV2HTTPEventEntity
import schemact.gradleplugin.aws.functiontemplates.dataClass
import schemact.gradleplugin.aws.functiontemplates.inputParamName
import schemact.gradleplugin.injection.AwsResolvers.LambdaResolvers
import schemact.gradleplugin.injection.ParameterResolver.assumeSingleDependencyMatches
import schemact.gradleplugin.injection.ParameterResolver.checkForUnresolved
import schemact.gradleplugin.injection.ParameterResolver.expandParamRequirements
import schemact.gradleplugin.injection.ParameterResolver.orderLeastDependantToMost
import schemact.gradleplugin.injection.mappers.getResourceAsText
import java.time.LocalDateTime

object APIGatewayV2HTTPEventHandlerInjectedTemplate {
    // returns fileKeys mapped to src
    //     e.g. com/company/GetTransactionGroup2Handler.kt ->... the src
    //          com/company/mappers/verifiedCognitoUser.kt ->.. the src
    //  should probably return structure containing handler full class name

    fun templateLambdaEventHandlerFiles(function: Function, domainPath: List<String>,
                                        handlerClassName: String, implClassName: String ): Map<String, String> {

        val context = orderLeastDependantToMost(expandParamRequirements(function, LambdaResolvers))
        checkForUnresolved(context)
        return templateLambdaEventHandlerFiles(function= function, domainPath = domainPath,
            handlerClassName = handlerClassName, implClassName =  implClassName, context = context)
    }


        fun templateLambdaEventHandlerFiles(function: Function, domainPath: List<String>,
                                        handlerClassName: String, implClassName: String,  context: List<Value>): Map<String, String> {



        val rootFilePath = domainPath.joinToString("/")
        val mapperFunctions = LambdaResolvers.filterIsInstance<MapperResolver>().map{it.mapperFunction}
        val mapperFunctionsRendered: Map<String, String> = LambdaResolvers.filterIsInstance<MapperResolver>().flatMap {
            it.mapperFunction.dependenciesSrc.entries.map{Pair(it.key, it.value)}
                .plus("${it.mapperFunction.classLocation.joinToString("/")}.kt" to it.mapperFunction.src)
        }.associate { it.first to it.second }
        val mapperFunctionDataClassesRendered = mapperFunctions.flatMap {  it.function.paramType.connections.map{it.entity2}
            .plus(it.function.returnType) }.filter{!it.isValueType}
            .map {
                 val packageName=  it.prefferedPackage?:domainPath.joinToString(".")
                 val nativeDefinition = it.nativeDefinition
                 val src = if (nativeDefinition!=null) nativeDefinitionSource(nativeDefinition, packageName) else dataClass(`package`=packageName, entity=it, topLevelTypes = emptySet()/* TODO - fix this*/)
                 val fileName = "${packageName.replace(".", "/")}/${it.name}.kt"
                 Pair(fileName, src)
            }
        // TODO render the external data classes - e.g. with DataClassTemplate
        // val handlerClassName = "${function.name}Handler"
        return mapOf("${rootFilePath}/${handlerClassName}.kt" to templateLambdaHandler(context = context/*should accept handler classname*/,
            domainPath = domainPath, handlerClassName = handlerClassName, implClassName = implClassName, function = function  ))
            .plus(mapperFunctionsRendered)
            .plus(mapperFunctionDataClassesRendered)
    }

    private fun nativeDefinitionSource(def: Entity.NativeDefinition, packageName: String) =
"""
package $packageName 
${def.kotlin}   
""".trimIndent()

    private fun templateLambdaHandler(context: List<Value>, domainPath: List<String>, handlerClassName:String,
                                      function: Function,
                                      implClassName: String): String {
        val imports = listOf("com.amazonaws.services.lambda.runtime.Context",
            "com.amazonaws.services.lambda.runtime.RequestHandler",
            "${APIGatewayV2HTTPEventEntity.let { "${it.prefferedPackage}.${it.name}" }}",
            "com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse",
            "com.fasterxml.jackson.databind.ObjectMapper")


        return """package ${domainPath.joinToString(".")}
// created by APIGatewayV2HTTPEventHandlerTemplate_Injection
${imports.map{"import $it"}.joinToString(System.lineSeparator())}
class ${handlerClassName} : RequestHandler<${APIGatewayV2HTTPEventEntity.name}, APIGatewayV2HTTPResponse> {


    override fun handleRequest(
        ${inputParamName}: ${APIGatewayV2HTTPEventEntity.name}?,
        context: Context?
    ): APIGatewayV2HTTPResponse {
       // created from template  apiGatewayEventHandler at ${LocalDateTime.now()} 
         input!!
${
            context.map { value ->
                value.renderer?.renderKotlin(
                    value = value,
                    assumeSingleDependencyMatches(value, context)
                )
            }.joinToString("\n")
        }
        
            val result = ($implClassName()).${function.name}(${
            function.paramType.connections.map { "${it.name}=${it.name}" }.joinToString(", ")
        })
    System.out.println("result: ${'$'}result")

    return APIGatewayV2HTTPResponse.builder()
            .withBody(ObjectMapper().writeValueAsString(result))
            .withStatusCode(200)
            .build()
    }
}        
           """.trimIndent()
    }

}
