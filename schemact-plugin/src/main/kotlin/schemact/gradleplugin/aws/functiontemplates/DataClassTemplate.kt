package schemact.gradleplugin.aws.functiontemplates

import schemact.domain.Entity
import schemact.domain.PrimitiveType

fun dataClass(`package`: String?=null, entity: Entity) = """
package ${`package`}

import com.fasterxml.jackson.annotation.JsonProperty

${dataClassSanPackage(entity, "")}    
""".trimIndent()

private fun dataClassSanPackage(entity: Entity, indent: String) : String {
val complexTypes = entity.connections.map{it.entity2}.filter {it !is PrimitiveType}
return """
${indent}data class ${entity.name}(${asArgs(entity)}) ${if (complexTypes.isNotEmpty()) {"""{ ${
 complexTypes.joinToString { dataClassSanPackage(it, "$indent   ") }   
}${indent}}    
"""} else ""} 
"""
}

private fun asArgs(entity: Entity) : String {
    return entity.connections.map { """@JsonProperty("${it.name}") var ${it.name}: ${if (it.entity2 is PrimitiveType) (it.entity2 as PrimitiveType).kotlinName else it.entity2.name} """ }.joinToString(", ")
}

//entity.connections.map { "${it.name}: ${ (it.entity2 as PrimitiveType).kotlinName}" }.joinToString (", ")



/* sample: data class TopLevel(var a: String, var b: Level2) {
    data class Level2(var c: Int) {
    }
}*/