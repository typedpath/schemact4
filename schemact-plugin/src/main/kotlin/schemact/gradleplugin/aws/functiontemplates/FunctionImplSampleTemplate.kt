package schemact.gradleplugin.aws.functiontemplates

import schemact.domain.Function
import schemact.domain.PrimitiveType
import java.time.LocalDateTime

fun functionSampleImpl(`package`: String, implClassName: String, function: Function) = """
package ${`package`}
// created by template functionSampleImpl
${function.paramType.connections.map { it.entity2 }.filter { it.prefferedPackage!=null }
    .map { "import ${it.prefferedPackage}.${it.name} " }.joinToString (System.lineSeparator()) }


class ${implClassName} { 
    // created from template  functionSampleImpl at ${LocalDateTime.now()}       
    fun ${function.name}(${topLevelFieldsAsArgs(function.name, function.paramType)}) : ${if (function.returnType is PrimitiveType) (function.returnType as PrimitiveType).kotlinName else function.returnType.name } {
         throw RuntimeException("Not Implemented")    
    }
    
}
"""


