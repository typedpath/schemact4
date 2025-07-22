package schemact.gradleplugin

import schemact.domain.Cardinality
import schemact.domain.Connection
import schemact.domain.ConnectionType
import schemact.domain.Entity
import schemact.domain.Function


abstract class Resolver {

    // return null if unresolved
    // otherwise return dependencies
    abstract fun resolve(value: Value) : List<Value.Requirement>?
}

fun requirement(from: Entity, to: Entity, name: String )  = Value.Requirement(name=name, match = {v->v.connectionFrom.entity2 == to},
    { Value( connectionFrom= Connection(name=name, entity1 = from, entity2 = to,
        cardinality= Cardinality.OneToOne, type= ConnectionType.Contains)) },
)

data class TransformFunction(val function: Function, val src: String)

fun fromTransformFunction(transformFunction: TransformFunction) = object : Resolver() {
    override fun resolve(value: Value): List<Value.Requirement>? {
        // should emit requirements not actual values
        // == test + default

        if (value.connectionFrom.entity2 == transformFunction.function.returnType && value.requirements == null) {

            value.renderer = object : Renderer() {
                override fun renderKotlin(value: Value, dependencies: Map<String, Value>) : String{
                    //return "val ${value.varName} = ${transformFunction.name}(${dependencies.map { (key, value) -> "$key=${value.varName}" }.joinToString(", ")})"
                    return "val ${value.varName} = ${transformFunction.function.name}(${dependencies.map{"${it.key}=${it.value.varName}"}.joinToString(", ")})"
                }

                override fun requiredImports(): List<String> {
                    TODO("Not yet implemented")
                }

                //  override fun transformFunction() : TransformFunction? = transformFunction

            }

            value.requirements = transformFunction.function.paramType.connections.map {
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

fun fromEntity(entity: Entity/*, transformFunction: TransformFunction*/) = object : Resolver () {
    override fun resolve(value: Value): List<Value.Requirement>? {
        // should emit requirements not actual values
        // == test + default

        if (value.connectionFrom.entity2 == entity && value.requirements == null) {

            value.renderer = object : Renderer() {
                override fun renderKotlin(value: Value, dependencies: Map<String, Value>) : String{
                    //return "val ${value.varName} = ${transformFunction.name}(${dependencies.map { (key, value) -> "$key=${value.varName}" }.joinToString(", ")})"
                    return "val ${value.varName} = work out entity renderer"
                }

                override fun requiredImports(): List<String> {
                    TODO("Not yet implemented")
                }

              //  override fun transformFunction() : TransformFunction? = transformFunction

            }

            value.requirements = value.connectionFrom.entity2.connections.map {
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