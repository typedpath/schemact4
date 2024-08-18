package schemact.domain

open class Entity(val name: String, val description: String, val isValueType: Boolean= false,
                  val parent: Entity? = null,
                  var isFromInfrastructure: Boolean = false,
                  var connections: MutableList<Connection> = mutableListOf(),
                  init: Entity.() -> Unit = {}) {
    init { init() }
    fun containsMany(name: String, description: String=name, type: Entity, optional : Boolean = true) : Connection {
        val c = Connection( name=name, description = description,  entity1 = this, entity2 = type,
            type = ConnectionType.Contains,
            cardinality = Cardinality.OneToMany
        )
        connections.add(c)
        return c
    }

    fun containsOne(name: String, description: String=name, type: Entity, optional : Boolean = true) : Connection {
        val c = Connection( name=name, description = description,  entity1 = this, entity2 = type,
            type = ConnectionType.Contains,
            cardinality = Cardinality.OneToOne
        )
        connections.add(c)
        return c
    }

    fun referencesOne(name: String, description: String  = name, to: Entity, optional : Boolean = true) : Connection {
        val c = Connection(name=name, description = description,  entity1 = this, entity2 = to,
            type = ConnectionType.Reference,
            cardinality = Cardinality.OneToOne
        )
        connections.add(c)
        return c
    }
}

