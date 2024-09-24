package schemact.gradleplugin.golang.templates

import org.gradle.configurationcache.extensions.capitalized
import schemact.domain.*
import schemact.domain.Function

object  Util {
    fun propertyTypeName(entity: Entity) =
        if (entity is PrimitiveType) "${(entity as PrimitiveType).goName}"
        else entity.name.capitalized()

    fun propertyTypeName(function: Function, entity: Entity) =
        if (entity is PrimitiveType) "${(entity as PrimitiveType).goName}"
        else "${function.name.capitalized()}_${entity.name}"


    fun propertyTypeName(module:Module, function: Function, entity: Entity) =
        if (entity is PrimitiveType) "${(entity as PrimitiveType).goName}"
        else "${module.name}.${function.name.capitalized()}_${entity.name}"

    fun cardinalityToken(connection: Connection) = if(connection.cardinality==Cardinality.OneToMany) "[]" else ""

}