package schemact.gradleplugin.injection.mappers


import schemact.aws.awsHelperRoot
import schemact.domain.InfrastructureInjectables
import schemact.gradleplugin.injection.MapperFunction

// TODO package and resource location should be inferred from function
val createReaderClassLocation = listOf("schemact", "aws", "CreateReadUserPrivateBucketData")
val createReadUserPrivateBucketData = MapperFunction( function =  InfrastructureInjectables.createReadUserPrivateBucketData,
    classLocation = createReaderClassLocation,
    src=getResourceAsText(root = awsHelperRoot, "/${createReaderClassLocation.joinToString("/")}.kt")
)
