package schemact.gradleplugin.injection

import schemact.domain.Cardinality
import schemact.domain.Connection
import schemact.domain.ConnectionType
import schemact.domain.Entity
import schemact.domain.Function
import schemact.gradleplugin.injection.mappers.getResourceAsText


abstract class Resolver {

    // return null if unresolved
    // otherwise return dependencies
    abstract fun resolve(value: Value) : List<Value.Requirement>?
}

fun requirement(from: Entity, to: Entity, name: String )  = Value.Requirement(name=name, match = {v->v.connectionFrom.entity2 == to},
    { Value( connectionFrom= Connection(name=name, entity1 = from, entity2 = to,
        cardinality= Cardinality.OneToOne, type= ConnectionType.Contains)) },
)

class MapperFunction(val function: Function,
                     val classLocation: List<String>,
                     val src: String,
                     val dependencyLocations: List<List<String>> = listOf(),
                          ) {
    val dependenciesSrc: Map<String, String> = dependencyLocations.map {
           "${it.joinToString ("/")}.kt" to getResourceAsText(javaClass, "/${it.joinToString("/")}.kt")
    }.toMap()
}

open class MapperResolver(val mapperFunction: MapperFunction) : Resolver() {

    // what is the point of coding without the occassional bodge !
    open fun fitDependenciesToFunction(connectionFrom: Connection, dependencies: Map<String, Value>) : Map<String, Value> = dependencies

    override fun resolve(value: Value): List<Value.Requirement>? {
        // should emit requirements not actual values
        // == test + default

        if (value.connectionFrom.entity2 == mapperFunction.function.returnType /*&& value.requirements == null*/) {
            val genericArgs = value.connectionFrom.genericParams
            value.renderer = object : Renderer() {
                override fun renderKotlin(value: Value, dependencies: Map<String, Value>) : String{
                    val genericsSpec = "${if (genericArgs.size>0) "<${genericArgs.map {it.name}.joinToString(",")}>" else "" }"
                    return "val ${value.varName} = ${mapperFunction.classLocation.joinToString (".")}.${mapperFunction.function.name}$genericsSpec(${fitDependenciesToFunction(value.connectionFrom, dependencies).map{"${it.key}=${it.value.varName}"}.joinToString(", ")})"
                }

            }
            // this is where to muck about with dependencies
            value.requirements = mapperFunction.function.paramType.connections.map {
                Value.Requirement(name=it.name, match = {
                        v->v.connectionFrom.entity2==it.entity2
                },
                    creator = {Value(connectionFrom = it)}
                )
            }

            return value.requirements
        } else return null
    }


}

fun fromMapperFunction(mapperFunction: MapperFunction) = MapperResolver(mapperFunction = mapperFunction)

