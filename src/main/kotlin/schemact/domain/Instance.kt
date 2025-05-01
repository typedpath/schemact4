package schemact.domain

class Instance(val entity: Entity) {
    val values =  mutableMapOf<Connection, Any?>()
    // make safe at some point
    fun set(property: Connection, value: Any?) {
        // check property exits
        if (!entity.connections.contains(property)) {
            values[property] = value
        }
        values [property] = value
    }
    fun writeAsJsonString() : String {
        return "{ " +
                entity.connections.map {
            """"${it.name}": ${writeAsJsonString(values[it])}"""
        }.joinToString (", ") +
                "}"
    }

    fun writeAsJsonString(value: Any? ) : String {
        return if (value == null) return "null"
        else if (value is Instance) { value.writeAsJsonString()}
        else if (value is String) { return '"' + value + '"' }
        else return "" + value
    }

    fun writeAsJsonStringTODO(value: Any? ) : String {
        return when (value) {
            null -> "null"
            (value is Instance) -> (value as Instance).writeAsJsonString()
            (value is String) -> ("" + '"' + value + '"')
            else -> "" + value
        }
    }

}