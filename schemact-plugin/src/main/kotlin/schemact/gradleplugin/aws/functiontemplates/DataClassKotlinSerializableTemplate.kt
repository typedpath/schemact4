package schemact.gradleplugin.aws.functiontemplates

import schemact.domain.Entity
import schemact.domain.Function
import schemact.domain.PrimitiveType
import schemact.gradleplugin.aws.functiontemplates.CodeLocations.dataClassName

fun dataClassSanPackageSerializable(function: Function?=null, entity: Entity, indent: String) : String {
val complexTypes = entity.connections.map{it.entity2}.filter {it !is PrimitiveType}
return """
${indent} @Serializable data class ${if (function!=null) dataClassName(function, entity) else entity.name }(${asArgs(entity)}) ${if (complexTypes.isNotEmpty()) {"""{ ${
 complexTypes.joinToString { dataClassSanPackageSerializable(entity = it, indent = "$indent   ") }   
}${indent}}    
"""} else ""} 
"""
}

private fun asArgs(entity: Entity) : String {
    return entity.connections.map { """ var ${it.name}: ${if (it.entity2 is PrimitiveType) (it.entity2 as PrimitiveType).kotlinName else it.entity2.name} """ }.joinToString(", ")
}

//entity.connections.map { "${it.name}: ${ (it.entity2 as PrimitiveType).kotlinName}" }.joinToString (", ")



/* sample: data class TopLevel(var a: String, var b: Level2) {
    data class Level2(var c: Int) {
    }
}*/