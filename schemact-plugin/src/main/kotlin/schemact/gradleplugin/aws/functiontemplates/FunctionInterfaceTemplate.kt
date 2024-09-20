package schemact.gradleplugin.aws.functiontemplates

import schemact.domain.*
import schemact.domain.Function
import java.time.LocalDateTime

fun functionInterface(`package`: String, functionId: String, interfaceName: String, function: Function) = """
package ${`package`}
interface ${interfaceName} {
    // created from template  functionInterface at ${LocalDateTime.now()}       
    fun ${function.name}(${topLevelFieldsAsArgs(functionId, function.paramType)}) : ${if (function.returnType is PrimitiveType) (function.returnType as PrimitiveType).kotlinName else "Return" }
}
"""

fun dataClassCode(functionId: String,entity: Entity, className: String) : String =
     if (entity is PrimitiveType) {
         ""
    } else {
        """data class $className(${topLevelFieldsAsArgs(functionId, entity)})""".trimIndent()
    }

fun topLevelFieldsAsArgs(functionId: String, entity: Entity) : String =
    topLevelFieldsAsArgs(functionId, entity.connections)


fun topLevelFieldsAsArgs(functionId: String, connections: List<Connection>) : String {
    val unsupportedFields =  connections.filter {
        it.type!= ConnectionType.Contains //|| it.entity2 !is PrimitiveType
    }

    if (unsupportedFields.size>0) {
        throw RuntimeException("these fields are not contained ${unsupportedFields.map
        {"$functionId.${it.name}"}.joinToString(", ")}")
    }

    return connections.map {"${it.name}: ${
        if ( it.entity2 is PrimitiveType) { (it.entity2 as PrimitiveType).kotlinName } else { it.entity2.name }
    }"}.joinToString (", ")
}

interface Sample {
    data class Params(val svg: String, val s3Bucket: String)
    data class Return (val url: String)

    fun execute(params: Params) :  Return
}


