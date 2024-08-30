package schemact.gradleplugin.aws.functiontemplates

import schemact.domain.Connection
import schemact.domain.Function
import schemact.domain.StringType
import java.time.LocalDateTime


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

//https://docs.aws.amazon.com/lambda/latest/dg/urls-invocation.html
// https://docs.aws.amazon.com/apigateway/latest/developerguide/http-api-develop-integrations-lambda.html#http-api-develop-integrations-lambda.proxy-format

class ${handlerClassName} : RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
    override fun handleRequest(
        input: APIGatewayV2HTTPEvent?,
        context: Context?
    ): APIGatewayV2HTTPResponse {
       // created from template  apiGatewayEventHandler at ${LocalDateTime.now()}       
        ${argsFromEnvironment.map {
    """    
    val ${it.name} = System.getenv("${it.name}")"""
}.joinToString(System.lineSeparator())}       
        ${if (argsFromBody.size!=1 || argsFromBody[0].entity2 !is StringType) """throw RuntimeException("only 1 string arg supported in body")"""
        else """
    val ${argsFromBody[0].name} = input!!.body"""}     
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