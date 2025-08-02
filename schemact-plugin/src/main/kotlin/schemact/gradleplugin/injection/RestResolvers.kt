import schemact.domain.Cardinality
import schemact.domain.Connection
import schemact.domain.ConnectionType
import schemact.domain.Entity
import schemact.domain.PrimitiveType
import schemact.domain.StringType
import schemact.gradleplugin.injection.Renderer
import schemact.gradleplugin.injection.Resolver
import schemact.gradleplugin.injection.ResolverUtil.string2ObjectKotlin
import schemact.gradleplugin.injection.Value

object RestResolvers {
    // need reference to other resolvers to exclude
    fun restParameterResolver(exclusions: Set<Resolver>) =
        object : Resolver () {
            override fun resolve(value: Value, /*expansionLevel: Int,*/ ): List<Value.Requirement>? {

                if (!exclusions.any{it.resolve(value)!=null}) {
                    value.renderer = object : Renderer() {
                        override fun renderKotlin(value: Value, dependencies: Map<String, Value>) : String{
                            val propertyType = value.connectionFrom.entity2
                            val propertyName = value.connectionFrom.name
                            // TODO handle optionality !
                            return if (propertyType.isValueType)  """val ${value.varName} = input.queryStringParameters.get("${propertyName}")!!"""
                            else """val ${value.varName} = ${string2ObjectKotlin("""input.queryStringParameters.get("${propertyName}")!!""", value.connectionFrom.entity2)}"""
                        }
                        override fun requiredImports(): List<String> =  emptyList()
                        // override fun supportFunctions(): String = ""
                    }
                    value.requirements = emptyList()
                    return value.requirements
                } else return null
            }

        }

    val bodyType = Entity(name = "_Body", description="body type")

    fun argIsTooBigForParam(paramType: Entity): Boolean = paramType is StringType && paramType.maxLength > 1000

    fun isBodyElement(connection: Connection) =
    (!(connection.entity2 is PrimitiveType && connection.cardinality == Cardinality.OneToOne)|| argIsTooBigForParam(connection.entity2))
    // TODO use multipart

    val restBodyVarName="_body"
    fun restBodyElementResolver(exclusions: Set<Resolver>) =
        object : Resolver () {
            override fun resolve(value: Value, /*expansionLevel: Int,*/ ): List<Value.Requirement>? {
// also is it a body type ?
                if (!exclusions.any{it.resolve(value)!=null} && isBodyElement(value.connectionFrom)) {
                    value.renderer = object : Renderer() {
                        override fun renderKotlin(value: Value, dependencies: Map<String, Value>) : String{
                            val propertyType = value.connectionFrom.entity2
                            val propertyName = value.connectionFrom.name
                            // TODO handle optionality !
                            return """val ${value.varName} = ${restBodyVarName}.${propertyName}"""
                        }
                        override fun requiredImports(): List<String> =  emptyList()
                    }
                    value.requirements = listOf(Value.Requirement(name = restBodyVarName,
                          match = {v -> v.connectionFrom.entity2==bodyType},
                         creator =  {
                             Value(connectionFrom = Connection(name=restBodyVarName, entity1 = value.connectionFrom.entity2,
                             entity2 = bodyType,
                             cardinality= Cardinality.OneToOne, type= ConnectionType.Contains))
                         }
                    ))
                    return value.requirements
                } else return null
            }

        }

    val restBodyResolver =
        object : Resolver () {
            override fun resolve(value: Value, /*expansionLevel: Int,*/ ): List<Value.Requirement>? {
// also is it a body type ?
                if (value.connectionFrom.entity2 == bodyType) {
                    value.renderer = object : Renderer() {
                        override fun renderKotlin(value: Value, dependencies: Map<String, Value>) : String{
                            return """val ${value.varName} = ${string2ObjectKotlin("""input.body!!""", value.connectionFrom.entity2)}"""
                        }
                        override fun requiredImports(): List<String> =  emptyList()
                    }
                    value.requirements = emptyList()
                    return value.requirements
                } else return null
            }

        }



}