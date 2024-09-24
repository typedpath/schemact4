package schemact.gradleplugin.golang.templates

import org.gradle.configurationcache.extensions.capitalized
import schemact.domain.*
import schemact.domain.Function
import schemact.gradleplugin.golang.templates.Util.cardinalityToken
import schemact.gradleplugin.golang.templates.Util.propertyTypeName

fun goInterface(module: Module) : String  {
    val visited = mutableSetOf<Entity>()
    return """
package ${module.name}

${module.functions.map {
        val function = it
        """
    
${it.paramType.connections.filter { it.entity2 !is PrimitiveType}.map {
            structure(function, it.entity2, visited)
        }.joinToString(System.lineSeparator())}   
 
${if (it.returnType !is PrimitiveType) structure(function, it.returnType, visited) else ""} 
 
type ${it.name.capitalized()} func(${it.paramType.connections.map {"${it.name}  ${cardinalityToken(it)}${propertyTypeName(function, it.entity2)}" }.joinToString(", ")}) (${propertyTypeName(function, it.returnType)}, error)   
""".trimIndent()
    }.joinToString (System.lineSeparator())}
""".trimIndent()
}

private fun structure(function : Function, entity: Entity, visited: MutableSet<Entity>) : String {
    if (visited.contains(entity)) {
        return ""
    }
    if (entity.isFromInfrastructure) {
        throw RuntimeException("TODO goInterface.structure isFromInfrastructure ")
    }
    visited.add(entity)
return """
type ${propertyTypeName(function, entity)} struct {
${entity.connections.map{"""    ${it.name.capitalized()} ${subStructure(it.entity2, "    ", visited = visited)} `json:"${it.name}"` """ }.joinToString(System.lineSeparator())}
}     
""".trimIndent()
}

private fun subStructure(entity: Entity, indent: String, visited: MutableSet<Entity> ) : String {
    return if (entity is PrimitiveType) return """${entity.goName}""" else if (!visited.contains(entity)){
        visited.add(entity)
        """
${indent}${
            entity.connections.map {
                """${indent}${it.name.capitalized()} ${
                    subStructure(
                        it.entity2,
                        "$indent    ", visited
                    )
                } `json:"${it.name}"`"""
            }
                .joinToString(System.lineSeparator())
        }
${indent}
 """.trimIndent()
    } else ""
}


