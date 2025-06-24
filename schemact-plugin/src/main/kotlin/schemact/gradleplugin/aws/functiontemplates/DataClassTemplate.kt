package schemact.gradleplugin.aws.functiontemplates

import schemact.domain.Cardinality
import schemact.domain.Connection
import schemact.domain.Entity
import schemact.domain.PrimitiveType
import kotlin.reflect.jvm.internal.impl.utils.DFS.Visited

// TODO keep track of what has already been printed
fun dataClass(`package`: String?=null, entity: Entity, topLevelTypes: Set<Entity>) = """
package ${`package`}
// dataClass topLevelTypes: ${topLevelTypes.map { "${it.name}-${it.description}-${it.hashCode()}" }.joinToString(", ")}
import com.fasterxml.jackson.annotation.JsonProperty

${dataClassSanPackage(entity, "", visited=topLevelTypes.minus(entity).toMutableSet())}    
""".trimIndent()

fun dataClassSanPackage(entity: Entity, indent: String, visited: MutableSet<Entity> = mutableSetOf()) : String {
    if (visited.contains(entity)) {
        return ""
    }
    visited.add(entity)
    val complexTypes = entity.connections.map{it.entity2}.filter {it !is PrimitiveType}.filter{!visited.contains(it)}
return """
    ${"// dataClassSanPackage ${entity.name} visited: ${visited.map { it.name }.joinToString (",")}"}
// create from template DataClassTemplate
// version= ${entity.version}    
${indent}data class ${entity.name}(${asArgs(entity)}) ${if (complexTypes.isNotEmpty()) {"""{ ${
 complexTypes.joinToString(System.lineSeparator()) { dataClassSanPackage(it, "$indent   ", visited) }   
}${indent}}
"""} else ""} 
"""
}

private fun asArgs(entity: Entity) : String {
    return entity.connections.map { """@JsonProperty("${it.name}") var ${it.name}:  ${argTypeDef(it)}""" }.joinToString(", ")
}

private fun argTypeDef(connection : Connection) : String{
    val entity = connection.entity2
    var type = "${if (entity is PrimitiveType) entity.kotlinName else entity.name}"
    if (connection.cardinality== Cardinality.OneToMany) {
        type = "MutableList<$type> = mutableListOf()"
    }
    return type
}

//entity.connections.map { "${it.name}: ${ (it.entity2 as PrimitiveType).kotlinName}" }.joinToString (", ")



/* sample: data class TopLevel(var a: String, var b: Level2) {
    data class Level2(var c: Int) {
    }
}*/