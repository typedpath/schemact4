package schemact.gradleplugin.aws.functiontemplates.injectionsupport
import TemplateConstants.dollarChar
import schemact.domain.Entity

//   TODO redo this
// each requirement should map to pre-requisites
// the search to try to resolve pre-requisites based on inputs recursively etc
// e.g. ReadUserDataPrivateBucket->cognitoClientDetails->verifyCognito->cognitoData, authheader
object InjectablesFactoryTemplate {
    fun InjectablesFactoryTemplate(packageName: String, userInfoClass: Entity?) =
        """    package $packageName
// created from InjectablesFactoryTemplate

    import com.amazonaws.services.s3.AmazonS3ClientBuilder
    import com.amazonaws.services.s3.model.GetObjectRequest
    import com.amazonaws.services.s3.model.ObjectMetadata
    import com.amazonaws.services.s3.model.PutObjectRequest
    import com.fasterxml.jackson.databind.ObjectMapper
    ${if (userInfoClass!=null) "import org.testedsoftware.accountview.${userInfoClass.name}Updater" else ""}
    import schemact.aws.CognitoClientDetails
    import schemact.aws.VerifyCognito.verifyCognitoJwt

    object InjectablesFactory {
        data class SecureUserInjectables(
            ${if (userInfoClass != null) "val update${userInfoClass.name}: Update${userInfoClass.name}" else ""},
            // TODO should not be optional or better redo algo !
            val writeToUserToDataPrivateBucket: WriteToUserToDataPrivateBucket?,
            val readUserDataPrivateBucket: ReadUserDataPrivateBucket?
        )

        fun createContext(contextIn: Map<String, Any>) : Map<String, Any?> {
             val Authorization: String? =  contextIn["AuthorizationHeader"] as  String?
             val cognitoClientDetails: CognitoClientDetails? = contextIn["CognitoClientDetails"] as CognitoClientDetails?
             val userTableName: String? = contextIn["DynamoDBTablename"] as String?
             val privateBucketName: String? = contextIn["PrivateBucketName"] as String?
            if (Authorization==null || cognitoClientDetails==null || 
                 userTableName==null ) {
                throw Exception("InjectablesFactoryTemplate Authorization, cognitoClientDetails and userTableName are mandatory")
            }
            val typedResult:  SecureUserInjectables = create(Authorization=Authorization, cognitoDetails=cognitoClientDetails, userTableName=userTableName, privateBucketName=privateBucketName)
             return mapOf("WriteToUserToDataPrivateBucket" to typedResult.writeToUserToDataPrivateBucket,
                          "ReadUserDataPrivateBucket" to typedResult.readUserDataPrivateBucket
                          ${if (userInfoClass!=null) """, "Update${userInfoClass.name}" to  typedResult.update${userInfoClass.name} """ else "" }
                          )
        }

        fun create(
            Authorization: String, cognitoDetails: CognitoClientDetails, userTableName: String,
            privateBucketName: String? = null
        ): SecureUserInjectables {

            val cognitoData = verifyCognitoJwt(Authorization, cognitoDetails)

            ${if (userInfoClass != null) """
            val update${userInfoClass.name}: Update${userInfoClass.name} = 
                { update, deserialize ->
                    ${userInfoClass.name}Updater.update(
                        userTableName = userTableName, userId = cognitoData.sub,
                        email = cognitoData.email ?: "noemail", update = update,
                        deserialize = deserialize
                    )
                }
""" else ""}
            val writeToUserToDataPrivateBucket: WriteToUserToDataPrivateBucket? =
                if (privateBucketName != null) { key, value ->
                    val s3 = AmazonS3ClientBuilder.standard().withRegion("us-east-1").build()
                    val inputStream = (ObjectMapper()).writeValueAsString(value).byteInputStream()
                    val om = ObjectMetadata()
                    om.contentType = "application/json; charset=utf-8"
                    val privatisedKey = "${dollarChar}{cognitoData.sub}${dollarChar}key"
                    val writeObjectRequest =
                        PutObjectRequest(privateBucketName, privatisedKey, inputStream, om)
                    val s3Object = s3.putObject(writeObjectRequest)
                } else null


            val readUserDataPrivateBucket: ReadUserDataPrivateBucket? =
                if (privateBucketName != null) { key ->
                    val s3 = AmazonS3ClientBuilder.standard().withRegion("us-east-1").build()
                    val privatisedKey = "${dollarChar}{cognitoData.sub}${dollarChar}key"
                    val getObjectRequest = GetObjectRequest(privateBucketName, privatisedKey)
                    try {
                        val s3Object = s3.getObject(getObjectRequest)
                        val contentType = s3Object.objectMetadata.contentType
                        s3Object.objectContent.bufferedReader().use { it.readText() }
                    } catch (ex: Exception) {
                        println("InjectablesFactory.readUserDataPrivateBucket failed to read object ${dollarChar}privatisedKey in ${dollarChar}privateBucketName")
                        println("InjectablesFactory.readUserDataPrivateBucket cognitoData.sub: ${dollarChar}{cognitoData.sub} key=${dollarChar}key")
                        throw ex
                    }
                } else null
            return SecureUserInjectables(
                ${if (userInfoClass!=null)  "update${userInfoClass.name}" else "null"},
                writeToUserToDataPrivateBucket,
                readUserDataPrivateBucket
            )
        }
    }
"""
}