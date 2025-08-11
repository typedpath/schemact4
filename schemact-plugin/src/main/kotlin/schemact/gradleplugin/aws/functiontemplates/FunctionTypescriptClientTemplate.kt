package schemact.gradleplugin.aws.functiontemplates

import schemact.domain.*
import schemact.domain.Function
import schemact.gradleplugin.RestPolicy
import software.amazon.awscdk.pipelines.CodePipelineSource.connection

object FunctionTypescriptClientTemplate {


    fun interfaceDef(entity: Entity) =
"""
export interface ${entity.name}  ${interfaceFieldsDef(entity, "    ")}
"""
    private fun interfaceFieldsDef(entity: Entity, indent: String) = " {${System.lineSeparator()} ${
        entity.connections.joinToString(System.lineSeparator()) { "$indent${it.name}: ${propertyTypeDef(it, indent)}" }}${System.lineSeparator()}${indent} } "

    private fun entityTypeDef(entity: Entity, indent: String) : String {
        return if (entity is PrimitiveType) {
            entity.typescriptName
        } else {
            interfaceFieldsDef(entity, "$indent    ")
        }
    }

    private fun propertyTypeDef(connection: Connection, indent: String): String =
          "${entityTypeDef(connection.entity2, indent)}${if (connection.cardinality==Cardinality.OneToMany)"[]" else ""}"



}


