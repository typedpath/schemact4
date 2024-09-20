package schemact.gradleplugin.golang.templates

import org.gradle.configurationcache.extensions.capitalized
import schemact.domain.Entity
import schemact.domain.Function
import schemact.domain.Module
import schemact.domain.PrimitiveType
import schemact.gradleplugin.golang.templates.Util.propertyTypeName

fun goInterface(module: Module) = """
package ${module.name}

${module.functions.map { 
    val function = it
    """
    
${it.paramType.connections.filter { it.entity2 !is PrimitiveType}.map {
    structure(function, it.entity2)
}.joinToString(System.lineSeparator())}   
 
${if (it.returnType !is PrimitiveType) structure(function, it.returnType) else ""} 
 
type ${it.name.capitalized()} func(${it.paramType.connections.map {"${it.name}  ${propertyTypeName(function, it.entity2)}" }.joinToString(", ")}) (${propertyTypeName(function, it.returnType)}, error)   
""".trimIndent()
}.joinToString (System.lineSeparator())}
""".trimIndent()

private fun structure(function : Function, entity: Entity) : String {
    if (entity.isFromInfrastructure) {
        throw RuntimeException("TODO goInterface.structure isFromInfrastructure ")
    }
return """
type ${propertyTypeName(function, entity)} struct {
${entity.connections.map{"""    ${it.name.capitalized()} ${subStructure(it.entity2, "    ")} `json:"${it.name}"` """ }.joinToString(System.lineSeparator())}
}     
""".trimIndent()
}

private fun subStructure(entity: Entity, indent: String) : String {
    return if (entity is PrimitiveType) return """${entity.goName}""" else """
${indent}${entity.connections.map {"""${indent}${it.name.capitalized()} ${subStructure(it.entity2, "$indent    ")} `json:"${it.name}"`"""}
    .joinToString (System.lineSeparator())}
${indent}
 """.trimIndent()
}


