package schemact.gradleplugin.aws.functiontemplates

import schemact.domain.Function
import schemact.domain.PrimitiveType
import java.time.LocalDateTime

fun functionSampleImpl(`package`: String, implClassName: String, function: Function) = """
package ${`package`}
class ${implClassName} { 
    // created from template  functionSampleImpl at ${LocalDateTime.now()}       
    fun ${function.name}(${topLevelFieldsAsArgs(function.name, function.paramType)}) : ${if (function.returnType is PrimitiveType) (function.returnType as PrimitiveType).kotlinName else "Return" } {
         throw RuntimeException("Not Implemented")    
    }
}
"""


