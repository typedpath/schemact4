package schemact.gradleplugin.injection

import org.junit.jupiter.api.Test
import schemact.domain.Entity
import schemact.domain.Function
import schemact.domain.int
import schemact.domain.string
import schemact.domain.writeUserPrivateBucketDataArg
import schemact.gradleplugin.injection.APIGatewayV2HTTPEventHandlerInjectedTemplate.templateLambdaEventHandlerFiles

class ParameterResolverTest {

val userInfoLatest = Entity(name = "param", description = "Params") {
    string("firstName", "firstNamee", maxLength = 10)
    string("middleName", "middleName", maxLength = 10)
    string("lastName", "lastName", maxLength = 20)
}

val function  = Function(
    "getTransactionGroup2",
    description = "gets a transactionGroup",
    paramType = Entity(name = "param", description = "Params") {
        writeUserPrivateBucketDataArg()
        string("fromInclusiveDate", "From Inclusive Date", maxLength = 10)
        /*readUserPrivateBucketDataArg()
        updateUserDataArg(userInfoLatest)
        string("fromInclusiveDate", "From Inclusive Date", maxLength = 10)
        string("toInclusiveDate", "To Inclusive Date", maxLength = 10)
        string("accountNumber", "AccountNumber", maxLength = 20)*/
    },
    returnType = Entity(name = "status", description = "Status") {
        int("code", "code")
        string("description", "Status Description", maxLength = 20)
    }
//        auth = auth
)


@Test
fun test() {
    val domainPath= listOf("com", "company")
    val srcMap = templateLambdaEventHandlerFiles(function = function, domainPath = domainPath, implClassName = "testImplClassName", handlerClassName = "testHandlerClassName")
    srcMap.forEach {  (input, output) -> println("""srcFile: $input
*****************************
$output
    """.trimMargin()) }

    srcMap.forEach {  (input, output) -> println("""srcFile: $input""".trimMargin()) }

    // sort this tree in dependency order
    //

    // inputs are parameter definition,
    // establish connection
    // each param has a source
    //  headerparam1
         // headers
               //  AWS REST input
    // multipartparam1
    //    multipart part
    //        AWS REST input
    // S3PrivateWriter
    //      userid
    //         cognitoInfo
    //           cognito (config) details (environment)
    //      private S3 bucket name (environment)
    // S3privateReader
    //      userid
    //         cognitoInfo
    //             cognito (config) details (environment)
    //      private S3 bucket name (environment)
    // UserInfoWriter
    //    dynamo db tablename
    //    userid
    //         cognitoInfo
    //           cognito (config) details (environment)

    // e.g.  // S3PrivateWriter->userid->cognitoInfo->cognito (config) details (environment)
    //    s3 PrivateWriter comes from resolver - add with unresolved dependencies
    //       e.g. if "value" slot for userid doesnt exist, create it
    //            if value for slot for cognitoInfo doesnt exist, create it



}
}