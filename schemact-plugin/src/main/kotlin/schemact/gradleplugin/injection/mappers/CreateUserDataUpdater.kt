package schemact.gradleplugin.injection.mappers

import schemact.aws.awsHelperRoot
import schemact.domain.Cardinality
import schemact.domain.Connection
import schemact.domain.ConnectionType
import schemact.domain.InfrastructureInjectables.createUserDataUpdater
import schemact.domain.InfrastructureInjectables.verifyUserSession
import schemact.domain.dataVersionType
import schemact.gradleplugin.injection.MapperFunction
import schemact.gradleplugin.injection.MapperResolver
import schemact.gradleplugin.injection.Value

// TODO put infer package and location from function
val createUserDataUpdaterMapperFunction = MapperFunction(
      function = createUserDataUpdater, src=getResourceAsText(root= awsHelperRoot,"/schemact/aws/CreateUserDataUpdater.kt"),
      classLocation = listOf("schemact", "aws", "CreateUserDataUpdater"),
      dependencyLocations=  listOf(listOf("schemact", "aws", "DynamoDbUtil"))
)

// TODO clean up this mess !!
val createUserDataUpdaterResolver = object : MapperResolver(mapperFunction =createUserDataUpdaterMapperFunction) {
      // where to get the
      override fun fitDependenciesToFunction(connectionFrom: Connection, dependencies: Map<String, Value>) : Map<String, Value> {
            val connection = Connection(name=""""${connectionFrom.genericParams[0].version}"""", entity1=connectionFrom.entity2,
                  entity2= dataVersionType, cardinality= Cardinality.OneToOne, type= ConnectionType.Contains
            )
            return dependencies.plus("version" to Value(connectionFrom=connection))
      }

}
