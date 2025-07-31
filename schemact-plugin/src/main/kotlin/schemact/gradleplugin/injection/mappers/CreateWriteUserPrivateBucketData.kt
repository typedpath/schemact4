package schemact.gradleplugin.injection.mappers


import schemact.aws.awsHelperRoot
import schemact.gradleplugin.injection.MapperFunction


//val dollarChar = '$'
// TODO package and resource location should be inferred from function
val classLocation = listOf("schemact", "aws", "CreateWriteUserPrivateBucketData")
val src = getResourceAsText(root = awsHelperRoot, "/${classLocation.joinToString("/")}.kt")
val createWriteUserPrivateBucketData = MapperFunction( function =  schemact.domain.InfrastructureInjectables.createWriteUserPrivateBucketData,
    classLocation = classLocation,
    src=src,
    dependencyLocations = listOf(listOf("schemact", "aws", "DynamoDbUtil"))
)
