package schemact.gradleplugin.injection

import org.junit.jupiter.api.Test
import schemact.domain.Entity
import schemact.domain.Function
import schemact.domain.InfrastructureInjectables
import schemact.domain.IntType
import schemact.domain.StringType
import schemact.domain.bool
import schemact.domain.int
import schemact.domain.readUserPrivateBucketDataArg
import schemact.domain.string
import schemact.domain.updateUserDataArg
import schemact.domain.writeUserPrivateBucketDataArg
import schemact.gradleplugin.injection.APIGatewayV2HTTPEventHandlerInjectedTemplate.templateLambdaEventHandlerFiles

class ParameterResolverTest {


    @Test
fun getTransactonGroup2Test() {
      test(getTransactionGroup2)
    }

    @Test
    fun categorizeTransactionsTest() {
        test(categorizeTransactions)
    }


   fun test(function: Function) {
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