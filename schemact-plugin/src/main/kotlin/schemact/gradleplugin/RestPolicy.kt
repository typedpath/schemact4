package schemact.gradleplugin

import schemact.domain.*

// TODO this has no REST related parts e.g. argsFromEnvironment, argsPassedthoughNatively - maybe rename ?
class RestPolicy(val paramType: Entity, val returnType: Entity,
                 /*expandAsEnvironmentDependencies: (Connection) -> List<Connection> = ::defaultEnvironmentDependencyExpansion*/ /*only matching by entity is supported*/ ) {
    val argsPassedthoughNatively : List<Connection> = paramType.connections.filter {
        val subParam = it.entity2
        subParam.isNativePassthrough
    }

    // dependency expansion

    val constructedFields = paramType.connections.filter {it.entity2.isConstructedPreInjection}

    val injectionPrecursorFields = constructedFields.flatMap {
          connection ->
              connection.entity2.connections.map {
              Connection(name = "${it.entity2.name}", entity1 = it.entity2, entity2 = it.entity2, cardinality = Cardinality.OneToOne, type= ConnectionType.Contains)
             }
    }.groupBy { it.name }.map {it.value[0]}


//    println("RestPolicy injectionPrecursorFields= ${injectionPrecursorFields}")

    val argsFromEnvironment : List<Connection> by lazy {
        val result = paramType.fieldsFromInfrastructure()//.flatMap{conn->expandAsEnvironmentDependencies(conn)}.groupBy { it.entity2 }
            .plus(injectionPrecursorFields.filter{it.entity2.isFromInfrastructure})
        // need to remove duplicate entity2
        //val result = groupedByEntity2.values.map{it[0]}

//        println("RestPolicy::argsFromEnvironment : ${result.map{ it.asString()}.joinToString(",${System.lineSeparator()}  ")}")

        result
    }

    //println("defaultEnvironmentDependencyExpansion ${connection.asString()}")

    val externalArgs : List<Connection> = paramType.connections.minus(argsFromEnvironment).minus(argsPassedthoughNatively).minus(constructedFields)
    val argsFromHeader : List<Connection> = paramType.fieldsFromHeader()
        .plus(argsFromEnvironment.filter{it.entity2.isFromHeader})
        .plus(injectionPrecursorFields.filter{it.entity2.isFromHeader})
        .minus(constructedFields)
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
            !useMultiPart && !subParam.isNativePassthrough && !subParam.isFromInfrastructure && !subParam.isConstructedPreInjection
                    && (subParam is PrimitiveType && it.cardinality == Cardinality.OneToOne)
                    && !argIsTooBigForParam(subParam)
        }

    // assign big args to body
    val argsFromBody: List<Connection> = paramType.connections.filter {
        val subParam = it.entity2
        !useMultiPart && !subParam.isNativePassthrough  && !subParam.isConstructedPreInjection && !subParam.isFromHeader  && !subParam.isFromInfrastructure &&
                (!(subParam is PrimitiveType && it.cardinality == Cardinality.OneToOne)|| argIsTooBigForParam(subParam))
    }.minus(constructedFields)

   val allTopLevelConnections: List<Connection> =
        argsFromBody.plus(argsFromParams).plus(argsFromEnvironment).plus(argsFromMultiPart)

    val complexTopLevelTypes = allTopLevelConnections.plus(injectionPrecursorFields).map { it.entity2 }.plus(returnType)
        .filter { it !is PrimitiveType || it.connections.size>0}.toMutableSet()


}