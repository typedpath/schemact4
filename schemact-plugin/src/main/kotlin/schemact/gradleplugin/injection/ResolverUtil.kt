package schemact.gradleplugin.injection

import schemact.domain.Entity

object ResolverUtil {
     fun string2ObjectKotlin( expression: String, entity: Entity) = "ObjectMapper().readValue($expression, ${entity.prefferedPackage?.let{"$it."}?:""}${entity.name}::class.java)"
}