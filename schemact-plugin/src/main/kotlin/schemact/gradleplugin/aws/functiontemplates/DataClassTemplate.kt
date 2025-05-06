package schemact.gradleplugin.aws.functiontemplates

import schemact.domain.Cardinality
import schemact.domain.Connection
import schemact.domain.Entity
import schemact.domain.PrimitiveType

fun dataClass(`package`: String?=null, entity: Entity) = """
package ${`package`}

import com.fasterxml.jackson.annotation.JsonProperty

${dataClassSanPackage(entity, "")}    
""".trimIndent()

fun dataClassSanPackage(entity: Entity, indent: String) : String {
val complexTypes = entity.connections.map{it.entity2}.filter {it !is PrimitiveType}
return """
// create from template DataClassTemplate    
${indent}data class ${entity.name}(${asArgs(entity)}) ${if (complexTypes.isNotEmpty()) {"""{ ${
 complexTypes.joinToString { dataClassSanPackage(it, "$indent   ") }   
}${indent}}    
"""} else ""} 
"""
}

private fun asArgs(entity: Entity) : String {
    return entity.connections.map { """@JsonProperty("${it.name}") var ${it.name}:  ${argTypeDef(it)}""" }.joinToString(", ")
}

private fun argTypeDef(connection : Connection) : String{
    val entity = connection.entity2
    var type = "${if (entity is PrimitiveType) (entity as PrimitiveType).kotlinName else entity.name}"
    if (connection.cardinality== Cardinality.OneToMany) {
        type = "MutableList<$type>"
    }
    return type
}

//entity.connections.map { "${it.name}: ${ (it.entity2 as PrimitiveType).kotlinName}" }.joinToString (", ")



/* sample: data class TopLevel(var a: String, var b: Level2) {
    data class Level2(var c: Int) {
    }
}*/