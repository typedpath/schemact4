package schemact.gradleplugin

import schemact.domain.Connection
import schemact.domain.Entity
import schemact.domain.PrimitiveType
import schemact.domain.StringType

class RestPolicy(paramType: Entity) {

    val argsFromEnvironment : List<Connection> = paramType.fieldsFromInfrastructure()

    fun argIsTooBigForParam(paramType: Entity): Boolean = paramType is StringType && paramType.maxLength > 1000
    // assign small args to params
    val argsFromParams: List<Connection> = paramType.connections.filter {
        val subParam = it.entity2
        !subParam.isFromInfrastructure && subParam is PrimitiveType && !argIsTooBigForParam(subParam)
    }
    // assign big args to body
    val argsFromBody: List<Connection> = paramType.connections.filter {
        val subParam = it.entity2
        subParam !is PrimitiveType ||argIsTooBigForParam(subParam)
    }

    val externalArgs : List<Connection> = paramType.connections.minus(argsFromEnvironment)

}