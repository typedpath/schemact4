package schemact.gradleplugin

import schemact.domain.*

// TODO this has no REST related parts e.g. argsFromEnvironment, argsPassedthoughNatively - maybe rename ?
class RestPolicy(val paramType: Entity) {
    val argsPassedthoughNatively : List<Connection> = paramType.connections.filter {
        val subParam = it.entity2
        subParam.isNativePassthrough
    }
    val argsFromEnvironment : List<Connection> = paramType.fieldsFromInfrastructure()

    val externalArgs : List<Connection> = paramType.connections.minus(argsFromEnvironment).minus(argsPassedthoughNatively)
    val argsFromHeader : List<Connection> = paramType.fieldsFromHeader()
    val useMultiPart = externalArgs.minus(argsFromHeader).any { it.entity2 is BlobType || it.entity2.connections.any { it.entity2 is BlobType } }
    val argsFromMultiPart : List<Connection>
        get () {
           if (useMultiPart) {
               return externalArgs.minus(argsFromHeader)
           } else {
               return emptyList()
           }
        }



    fun argIsTooBigForParam(paramType: Entity): Boolean = paramType is StringType && paramType.maxLength > 1000
    // assign small args to params
    val argsFromParams: List<Connection> = paramType.connections.filter {
            val subParam = it.entity2
            !useMultiPart && !subParam.isNativePassthrough && !subParam.isFromInfrastructure && subParam is PrimitiveType && !argIsTooBigForParam(
                subParam
            )
        }
    // assign big args to body
    val argsFromBody: List<Connection> = paramType.connections.filter {
        val subParam = it.entity2
        !useMultiPart && !subParam.isNativePassthrough  && !subParam.isFromHeader  && !subParam.isFromInfrastructure && (subParam !is PrimitiveType || argIsTooBigForParam(subParam))
    }




}