package schemact.gradleplugin.aws.functiontemplates

import java.time.LocalDateTime
import schemact.domain.*
import schemact.domain.Function


//TODO deal with optional arguments
fun apiGatewayEventHandler(packageName: String, function: Function,
                           implClassName: String,
                           handlerClassName: String,
                           argsFromEnvironment: List<Connection> /*connections.filter { it.entity2.isFromInfrastructure}*/,
                           argsFromParams: List<Connection>,
                           argsFromBody: List<Connection>
                           ) = """
package $packageName

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.annotation.JsonProperty

//https://docs.aws.amazon.com/lambda/latest/dg/urls-invocation.html
// https://docs.aws.amazon.com/apigateway/latest/developerguide/http-api-develop-integrations-lambda.html#http-api-develop-integrations-lambda.proxy-format


class ${handlerClassName} : RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    data class Body (${argsFromBody.joinToString(",") { asDataClassField(it) }})

    override fun handleRequest(
        input: APIGatewayV2HTTPEvent?,
        context: Context?
    ): APIGatewayV2HTTPResponse {
       // created from template  apiGatewayEventHandler at ${LocalDateTime.now()}       
        ${argsFromEnvironment.map {
    """    
    val ${it.name} = System.getenv("${it.name}")"""
}.joinToString(System.lineSeparator())}       
      val body = ObjectMapper().readValue(input!!.body, Body::class.java)
      ${argsFromBody.joinToString(System.lineSeparator()) {"""
      val ${it.name}=body.${it.name}"""   }} 
       ${argsFromParams.map { 
"""
    val ${it.name} = input.queryStringParameters.get("${it.name}")!!"""       
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

private fun asDataClassField(connection: Connection) = """@JsonProperty("${connection.name}") val ${connection.name}:${kotlinTypeName(connection.entity2)} """

private fun kotlinTypeName(entity: Entity) = if (entity is PrimitiveType) entity.kotlinName else entity.name