package schemact.domain


open class Entity(val name: String, val description: String, val isValueType: Boolean= false,
                  val parent: Entity? = null,
                  //comes from the infrastructure build
                  var isFromInfrastructure: Boolean = false,
                  // if injected, constructed from other injected stuff
                  var isConstructedPreInjection: Boolean = false,
                  var nativeDefinition: NativeDefinition?=null,
                  //comes from an HTTP header
                  var isFromHeader: Boolean = false,
                  // comes from the native handler
                  var isNativePassthrough: Boolean = false,
                  var connections: MutableList<Connection> = mutableListOf(),
                  var prefferedPackage: String? = null,
                  var version: String = DefaultVersion,
                  init: Entity.() -> Unit = {}) {
    init { init() }
    companion object {
        const val DefaultVersion="0"
    }

    data class NativeDefinition(val kotlin: String)


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

    fun fieldsFromInfrastructure() :List<Connection> = connections.filter {it.type==ConnectionType.Contains && it.entity2.isFromInfrastructure}



    fun fieldsFromHeader() :List<Connection> = connections.filter {it.type==ConnectionType.Contains && it.entity2.isFromHeader}

}

