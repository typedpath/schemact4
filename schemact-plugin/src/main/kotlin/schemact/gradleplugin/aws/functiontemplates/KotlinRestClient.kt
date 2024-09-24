package schemact.gradleplugin.aws.functiontemplates

import schemact.domain.*
import schemact.domain.Function
import schemact.gradleplugin.RestPolicy
import schemact.gradleplugin.aws.functiontemplates.CodeLocations.dataClassName

fun kotlinRestClient(module: Module, function: Function, packageName: String, className: String) : String  {
    val restPolicy = RestPolicy(function.paramType)
    val visited = mutableSetOf<Entity>()
    return """
package ${packageName}
import io.ktor.client.HttpClient
import io.ktor.client.call.*
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import io.ktor.serialization.kotlinx.json.*


//import kotlinx.serialization.Serializable

object  ${className} {  
   
${restPolicy.externalArgs.filter { it.entity2 !is PrimitiveType }.map{
        dataClassSanPackageSerializable(function, it.entity2, "", visited)
    }.joinToString (System.lineSeparator())}   
${if (function.returnType !is PrimitiveType) dataClassSanPackageSerializable(function, function.returnType, "", visited) else ""}     
      
suspend fun ${ function.name }(${ restPolicy.externalArgs.map { "${it.name} : ${argName(function, it)}" }.joinToString (", ")}, domainRoot: String = "",client: HttpClient=HttpClient(CIO) {
    install(HttpTimeout) {
        requestTimeoutMillis = 20000
        connectTimeoutMillis = 20000
    }
    install(ContentNegotiation) {
        json()
    }
    }) : ${typeName(function, function.returnType)}  {
    val url = "${'$'}domainRoot/${module.name}/${function.name}"
    ${if (restPolicy.argsFromBody.isNotEmpty()) """
    @Serializable data class Body(${restPolicy.argsFromBody.map { "val ${it.name}: ${dataClassName(function, it)}" }.joinToString (", ")})    
    val body = Body(${restPolicy.argsFromBody.map { "${it.name}= ${it.name}" }.joinToString (", ")})"""
    else """
    val body = ""    
    """}    
       
    val response: HttpResponse = client.post() {
                url(url)
${restPolicy.argsFromParams.map{"""
                parameter("${it.name}", ${it.name})""".trimIndent()}.joinToString(System.lineSeparator())}                
                setBody(body)
                contentType(ContentType.Application.Json)
            }
   return response.body<${typeName(function, function.returnType)}>()
}
}
"""
}

fun argName(function: Function, connection: Connection) : String =
        dataClassName(function, connection)

fun typeName(function: Function, entity: Entity) : String {
    return if (entity is PrimitiveType) {entity.kotlinName}
    else {
        dataClassName(function, entity)
    }
}