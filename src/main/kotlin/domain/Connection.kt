package domain

//TODO use a class maxTo, minTo
enum class Cardinality { OneToOne,OneToMany }
enum class ConnectionType { Contains,Reference }
class Connection(val name: String, description: String?,
                 var entity1: Entity,
                 var entity2: Entity,
                 var cardinality: Cardinality,
                 var type: ConnectionType,
                 init: Connection.() -> Unit={})  {
init { init() }
}

fun Connection?.asString() : String = if (this==null) "null" else "${this.entity1.name} =${this.name}> ${this.entity2.name}"
