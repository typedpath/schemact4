package schemact.gradleplugin.aws.functiontemplates

import java.time.LocalDateTime
import schemact.domain.*
import schemact.domain.Function
import schemact.gradleplugin.RestPolicy


//TODO deal with optional arguments
fun apiGatewayEventHandler(packageName: String, function: Function,
                           implClassName: String,
                           handlerClassName: String,
                           restPolicy: RestPolicy
                           ) = """
package $packageName

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.annotation.JsonProperty
${function.paramType.connections.map { it.entity2 }.filter { it.prefferedPackage!=null }
    .map { "import ${it.prefferedPackage}.${it.name} " }.joinToString (System.lineSeparator()) }


//https://docs.aws.amazon.com/lambda/latest/dg/urls-invocation.html
// https://docs.aws.amazon.com/apigateway/latest/developerguide/http-api-develop-integrations-lambda.html#http-api-develop-integrations-lambda.proxy-format


class ${handlerClassName} : RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    ${if (restPolicy.argsFromBody.size>0) "data class Body (${restPolicy.argsFromBody.joinToString(",") { asDataClassField(it) }})" else ""}

    override fun handleRequest(
        input: APIGatewayV2HTTPEvent?,
        context: Context?
    ): APIGatewayV2HTTPResponse {
       // created from template  apiGatewayEventHandler at ${LocalDateTime.now()}   
           
    val debug = System.getenv("debug")!=null
    if (debug) {
          println("input: ${'$'}input")
          ObjectMapper().writeValueAsString(input)
          println("context: ${'$'}context")
          ObjectMapper().writeValueAsString(context)      
          }

    input!!
           
        ${restPolicy.argsFromEnvironment.map {
    """    
    val ${it.name} = ${getFromSystemEnvCode(it)}"""
}.joinToString(System.lineSeparator())}       
      ${if (restPolicy.argsFromBody.size>0) "val body = ObjectMapper().readValue(input!!.body, Body::class.java)" else ""}
      ${restPolicy.argsFromBody.joinToString(System.lineSeparator()) {"""
      val ${it.name}=body.${it.name}"""   }} 
       ${restPolicy.argsFromParams.map { 
"""
    val ${it.name} = input.queryStringParameters.get("${it.name}")!!"""       
}.joinToString (System.lineSeparator())} 
     ${restPolicy.argsFromHeader.map {
    """
    val ${it.name} = input.headers.get("${it.name}".lowercase())!!"""
}.joinToString (System.lineSeparator())}
    
    
    val result = ($implClassName()).${function.name}(${function.paramType.connections.map{"${it.name}=${it.name}"}.joinToString(", ")})
    System.out.println("result: ${'$'}result")

    return APIGatewayV2HTTPResponse.builder()
            .withBody(result)
            .withStatusCode(200)
            .build()
    }
    

}
"""

private fun getFromSystemEnvCode(connection: Connection) : String {
    var result = """System.getenv("${connection.name}")"""
    if (!connection.entity2.isValueType) {
        result = "ObjectMapper().readValue($result, ${connection.entity2.name}::class.java)"
    }
    return result
}

private fun asDataClassField(connection: Connection) = """@JsonProperty("${connection.name}") val ${connection.name}:${kotlinTypeName(connection.entity2)} """

private fun kotlinTypeName(entity: Entity) = if (entity is PrimitiveType) entity.kotlinName else entity.name