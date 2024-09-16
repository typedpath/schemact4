package schemact.domain

open class PrimitiveType(name: String, description: String,
                         val kotlinName: String,
                         var sqlType: String= "",
                         var typescriptName: String="",
                         var goName: String = ""
                         )
  : Entity( name=name, description = description, isValueType = true) {
  }

open class StringType(val maxLength: Int) : PrimitiveType(name="String(${maxLength})",
    kotlinName = "String",
    typescriptName = "string",
    sqlType = "TEXT",
    goName="string",
    description = "String maxlength " + maxLength
)

class FloatType() : PrimitiveType(name="Float",
    kotlinName = "Float",
    typescriptName = "number",
    sqlType = "NUMBER",
    goName = "float32",
    description = "Float"
)

class BooleanType() : PrimitiveType(name="Bool",
    kotlinName = "Boolean",
    typescriptName = "boolean",
    sqlType = "int???",
    goName = "bool",
    description = "Boolean"
)

class IntType() : PrimitiveType(name="Int",
    kotlinName = "Int",
    typescriptName = "number",
    sqlType = "int???",
    goName = "int32",
    description = "Integer"
)


fun Entity.string(name: String, description: String?=null, maxLength: Int, optional:Boolean=false) {
    connections.add(
        Connection(name=name, description=description, entity1 = this,
        cardinality = Cardinality.OneToOne,
        type= ConnectionType.Contains,
        entity2 = StringType(maxLength),
            optional = optional
        )
    )
}

fun Entity.float(name: String, description: String?=null, optional:Boolean=false) {
    connections.add(
        Connection(name=name, description=description, entity1 = this,
        cardinality = Cardinality.OneToOne,
        type= ConnectionType.Contains,
        entity2 = FloatType(),
        optional = optional
        )
    )
}

fun Entity.bool(name: String, description: String?=null, optional:Boolean=false) {
    connections.add(
        Connection(name=name, description=description, entity1 = this,
        cardinality = Cardinality.OneToOne,
        type= ConnectionType.Contains,
        entity2 = BooleanType(),
            optional = optional
        )
    )
}

fun Entity.int(name: String, description: String?=null, optional:Boolean=false) {
    connections.add(
        Connection(name=name, description=description, entity1 = this,
        cardinality = Cardinality.OneToOne,
        type= ConnectionType.Contains,
        entity2 = IntType()
        )
    )
}