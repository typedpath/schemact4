package schemact.gradleplugin.injection.mappers


import TemplateConstants.dollarChar
import schemact.gradleplugin.injection.MapperFunction


//val dollarChar = '$'
// TODO put src impl in a submodule (injectedtransforms, from which source is copied)
val createWriteUserPrivateBucketData = MapperFunction( function =  schemact.domain.InfrastructureInjectables.createWriteUserPrivateBucketData,
    src=
    """//TODO generate args fro function
           fun ${schemact.domain.InfrastructureInjectables.createWriteUserPrivateBucketData.name}() (privateBucketName: String, verifiedCognitoUser: VerifiedCognitoUser): WriteUserPrivateBucketData {
                   val writeToUserToDataPrivateBucket: WriteUserPrivateBucketData? =
                 { key, value ->
                    val s3 = AmazonS3ClientBuilder.standard().withRegion("us-east-1").build()
                    val inputStream = (ObjectMapper()).writeValueAsString(value).byteInputStream()
                    val om = ObjectMetadata()
                    om.contentType = "application/json; charset=utf-8"
                    val privatisedKey = "${dollarChar}{privateBucketName.sub}${dollarChar}key"
                    val writeObjectRequest =
                        PutObjectRequest(privateBucketName, privatisedKey, inputStream, om)
                    val s3Object = s3.putObject(writeObjectRequest)
                }
    }
      """.trimIndent() )
