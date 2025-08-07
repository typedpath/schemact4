package schemact.gradleplugin.injection.mappers


import schemact.aws.awsHelperRoot
import schemact.domain.InfrastructureInjectables
import schemact.gradleplugin.injection.MapperFunction

// TODO package and resource location should be inferred from function
val createMultiPartBodyReaderClassLocation = listOf("schemact", "aws", "CreateMultiPartBodyReader")
val createMultiPartBodyReader = MapperFunction( function =  InfrastructureInjectables.createMultiPartBodyReader,
    classLocation = createMultiPartBodyReaderClassLocation,
    src=getResourceAsText(root = awsHelperRoot, "/${createMultiPartBodyReaderClassLocation.joinToString("/")}.kt")
)
