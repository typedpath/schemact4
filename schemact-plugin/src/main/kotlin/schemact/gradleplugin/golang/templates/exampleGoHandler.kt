package schemact.gradleplugin.golang.templates

import org.gradle.configurationcache.extensions.capitalized
import schemact.domain.Entity
import schemact.domain.Function
import schemact.domain.Module
import schemact.domain.PrimitiveType
import schemact.gradleplugin.golang.templates.Util.propertyTypeName

fun exampleGoHandler(function: Function, module: Module) : String{
// define struct for params,
    //val paramTypeName = "${function.name}Params"
    val returnTypeName = "${function.name}"
    return """
package ${module.name}        
//test

func ${function.name.capitalized()}Handler(${function.paramType.connections.map { "${it.name} ${propertyTypeName(function, it.entity2)}" }.joinToString(",")}) (${propertyTypeName(function, function.returnType)}, error) {
   // TODO
}            
    """.trimIndent()
}




