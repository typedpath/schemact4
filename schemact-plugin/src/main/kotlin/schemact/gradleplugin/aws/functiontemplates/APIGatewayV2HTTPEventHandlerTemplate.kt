package schemact.gradleplugin.aws.functiontemplates

import java.time.LocalDateTime
import schemact.domain.*
import schemact.domain.Function
import schemact.domain.InfrastructureInjectables.APIGatewayV2HTTPEventEntity
import schemact.gradleplugin.RestPolicy

const val inputParamName="input"
val nativeParamType2Name= mapOf(inputParamName to APIGatewayV2HTTPEventEntity)
//TODO deal with optional arguments
fun apiGatewayEventHandler(packageName: String, function: Function,
                           implClassName: String,
                           handlerClassName: String,
                           restPolicy: RestPolicy
                           ) = """
package $packageName
// create by APIGatewayV2HTTPEventHandlerTemplate
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import ${APIGatewayV2HTTPEventEntity.let { "${it.prefferedPackage}.${it.name}" }}

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.annotation.JsonProperty
${if (restPolicy.argsFromMultiPart.size>0)"import $packageName.MultiPart" else ""}
${function.paramType.connections.map { it.entity2 }.filter { it.prefferedPackage!=null && !it.isNativePassthrough }
    .map { "import ${it.prefferedPackage}.${it.name} " }.joinToString (System.lineSeparator()) }


// https://docs.aws.amazon.com/lambda/latest/dg/urls-invocation.html
// https://docs.aws.amazon.com/apigateway/latest/developerguide/http-api-develop-integrations-lambda.html#http-api-develop-integrations-lambda.proxy-format


class ${handlerClassName} : RequestHandler<${APIGatewayV2HTTPEventEntity.name}, APIGatewayV2HTTPResponse> {

    ${if (restPolicy.argsFromBody.size>0) "data class Body (${restPolicy.argsFromBody.joinToString(",") { asDataClassField(it) }})" else ""}

    override fun handleRequest(
        ${inputParamName}: ${APIGatewayV2HTTPEventEntity.name}?,
        context: Context?
    ): APIGatewayV2HTTPResponse {
       // created from template  apiGatewayEventHandler at ${LocalDateTime.now()}   
           
    val debug = System.getenv("debug")!=null
    if (debug) {
          println("${inputParamName}: ${'$'}${inputParamName}")
          ObjectMapper().writeValueAsString(${inputParamName})
          println("context: ${'$'}context")
          ObjectMapper().writeValueAsString(context)      
          }

    ${inputParamName}!!
           
        ${restPolicy.argsFromEnvironment.map {
    """    
    val ${it.name} = ${getFromSystemEnvCode(it)}"""
}.joinToString(System.lineSeparator())}       
      ${if (restPolicy.argsFromMultiPart.size==0 && restPolicy.argsFromBody.size>0) "val body = ObjectMapper().readValue(input!!.body, Body::class.java)" else ""}
      ${restPolicy.argsFromBody.joinToString(System.lineSeparator()) {"""
      val ${it.name}=body.${it.name}"""   }} 
       ${restPolicy.argsFromParams.map { 
"""
    val ${it.name} = ${inputParamName}.queryStringParameters.get("${it.name}")!!"""       
}.joinToString (System.lineSeparator())} 
     ${restPolicy.argsFromHeader.map {
    """
    val ${it.name} = ${inputParamName}.headers.get("${it.name}".lowercase())!!"""
}.joinToString (System.lineSeparator())}
    
   ${if (restPolicy.argsFromMultiPart.size>0) """
              val contentType = input.headers?.get("content-type")
            ?: throw Exception("Missing Content-Type")
        val multiParts = MultiPart.read(input.body, contentType)  
            ${restPolicy.argsFromMultiPart.map { mulitiPartExtractionCode(it) }.joinToString(System.lineSeparator()) } 
   """ else "" } 
        //TODO 

    
    val result = ($implClassName()).${function.name}(${function.paramType.connections.map{"${it.name}=${it.name}"}.joinToString(", ")})
    System.out.println("result: ${'$'}result")

    return APIGatewayV2HTTPResponse.builder()
            .withBody(ObjectMapper().writeValueAsString(result))
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

private fun mulitiPartExtractionCode(connection: Connection) : String {
    val partName = "${connection.name}Part"
    return if (connection.entity2 is ReactJsInjectables.File) {
"""        val ${partName} = multiParts.get("${connection.name}")
        val ${connection.name} = File(filename = ${partName}?.contentDispositionValues!!["filename"]?:throw Exception("No file provided"), content = ${partName}.body, contentType =${partName}?.contentType!!)
"""    } else
"""
    val ${partName} = multiParts.get("${connection.name}")
    val ${connection.name} = String(partName.body)
"""
}


private fun asDataClassField(connection: Connection) = """@JsonProperty("${connection.name}") val ${connection.name}:${kotlinTypeName(connection.entity2)} """

private fun kotlinTypeName(entity: Entity) = if (entity is PrimitiveType) entity.kotlinName else entity.name