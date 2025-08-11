package schemact.gradleplugin.injection

import schemact.domain.Connection
import schemact.domain.ConnectionType
import schemact.domain.Entity
import schemact.domain.Function
import schemact.domain.PrimitiveType
import schemact.gradleplugin.aws.functiontemplates.CodeLocations
import java.time.LocalDateTime

fun functionSampleImplNew(`package`: String, implClassName: String, function: Function) = """
package ${`package`}
// created by template functionSampleImplNew

class ${implClassName} { 
    // created from template  functionSampleImplNew at ${LocalDateTime.now()}       
    fun ${function.name}(${topLevelFieldsAsArgsFullyQualified(function.name, function.paramType.connections)}) : ${if (function.returnType is PrimitiveType) (function.returnType as PrimitiveType).kotlinName else function.returnType.name } {
         throw RuntimeException("Not Implemented")    
    }
    
}
"""

fun topLevelFieldsAsArgsFullyQualified(functionId: String, connections: List<Connection>) : String {
    val unsupportedFields =  connections.filter {
        it.type!= ConnectionType.Contains //|| it.entity2 !is PrimitiveType
    }

    if (unsupportedFields.size>0) {
        throw RuntimeException("these fields are not contained ${unsupportedFields.map
        {"$functionId.${it.name}"}.joinToString(", ")}")
    }

    fun kotlinParamTypeDef(connection: Connection) = "${connection.entity2.prefferedPackage?.let{"$it."}?:"" }${CodeLocations.kotlinTypeName(connection)}"

    return connections.map {"${it.name}: ${kotlinParamTypeDef(it)}"}.joinToString (", ")
}
