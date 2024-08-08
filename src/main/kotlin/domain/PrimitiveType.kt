package domain

open class PrimitiveType(name: String, description: String,
                         val kotlinName: String,
                         var sqlType: String= "",
                         var typescriptName: String=""
                         )
  : Entity( name=name, description = description, isValueType = true) {
  }

class StringType(val maxLength: Int) : PrimitiveType(name="String(${maxLength})",
    kotlinName = "String",
    typescriptName = "string",
    sqlType = "TEXT",
    description = "String maxlength " + maxLength
)

class FloatType() : PrimitiveType(name="Float",
    kotlinName = "Float",
    typescriptName = "number",
    sqlType = "NUMBER",
    description = "Float"
)

class BooleanType() : PrimitiveType(name="Bool",
    kotlinName = "Bool",
    typescriptName = "boolean",
    sqlType = "int???",
    description = "Boolean"
)

class IntType() : PrimitiveType(name="Int",
    kotlinName = "Int",
    typescriptName = "number",
    sqlType = "int???",
    description = "Integer"
)


fun Entity.string(name: String, description: String, maxLength: Int) {
    connections.add(Connection(name=name, description=description, entity1 = this,
        cardinality = Cardinality.OneToOne,
        type=ConnectionType.Contains,
        entity2 = StringType(maxLength)))
}

fun Entity.float(name: String, description: String) {
    connections.add(Connection(name=name, description=description, entity1 = this,
        cardinality = Cardinality.OneToOne,
        type=ConnectionType.Contains,
        entity2 = FloatType()))
}

fun Entity.bool(name: String, description: String) {
    connections.add(Connection(name=name, description=description, entity1 = this,
        cardinality = Cardinality.OneToOne,
        type=ConnectionType.Contains,
        entity2 = BooleanType()))
}

fun Entity.int(name: String, description: String) {
    connections.add(Connection(name=name, description=description, entity1 = this,
        cardinality = Cardinality.OneToOne,
        type=ConnectionType.Contains,
        entity2 = IntType()))
}