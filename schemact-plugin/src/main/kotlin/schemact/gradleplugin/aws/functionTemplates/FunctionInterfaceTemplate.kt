package schemact.gradleplugin.aws.functionTemplates

import schemact.domain.ConnectionType
import schemact.domain.Entity
import schemact.domain.Function
import schemact.domain.PrimitiveType
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

fun topLevelFieldsAsArgs(functionId: String, entity: Entity) : String {
    val unsupportedFields =  entity.connections.filter {
        it.type!= ConnectionType.Contains || it.entity2 !is PrimitiveType
    }

    if (unsupportedFields.size>0) {
        throw RuntimeException("these fields are not primitive and contained ${unsupportedFields.map
         {"$functionId.${it.name}"}.joinToString(", ")}")
    }

    return entity.connections.map { "${it.name}: ${ (it.entity2 as PrimitiveType).kotlinName}" }.joinToString (", ")

}


interface Sample {
    data class Params(val svg: String, val s3Bucket: String)
    data class Return (val url: String)

    fun execute(params: Params) :  Return
}


