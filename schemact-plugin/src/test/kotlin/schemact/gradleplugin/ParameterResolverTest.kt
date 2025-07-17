package schemact.gradleplugin

import org.junit.jupiter.api.Test
import schemact.domain.Connection
import schemact.domain.Entity
import schemact.domain.Function
import schemact.domain.asString
import schemact.domain.int
import schemact.domain.readUserPrivateBucketDataArg
import schemact.domain.string
import schemact.domain.updateUserDataArg
import schemact.domain.writeUserPrivateBucketDataArg
import software.amazon.awscdk.pipelines.CodePipelineSource.connection
import javax.swing.Renderer
import kotlin.collections.mutableListOf
import kotlin.math.E

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
            readUserPrivateBucketDataArg()
            updateUserDataArg(userInfoLatest)
            string("fromInclusiveDate", "From Inclusive Date", maxLength = 10)
            string("toInclusiveDate", "To Inclusive Date", maxLength = 10)
            string("accountNumber", "AccountNumber", maxLength = 20)
        },
        returnType = Entity(name="status", description="Status") {
            int("code", "code")
            string("description", "Status Description", maxLength = 20)
        }
//        auth = auth
    )

    abstract class Renderer {
        abstract fun renderKotlin( dependencyToValue: MutableMap<Connection, Value>)// = "val $name = ${transformFunction}(${dependencyNames.joinToString (",")})"
    }

    class Value (val connection: Connection, val dependencyToValue: MutableMap<Connection, Value> = mutableMapOf(),
                 val renderer: Renderer? = null) {
    }

    abstract class Resolver {
        // return null if unresolved
        // otherwise return dependencies
        abstract fun resolve(resolvedValues: List<Value>) : List<Value>?
    }

    // distinguish between expansion and resolution
    fun expandParams(function: Function, resolvers: List<Resolver>) : List<Value> {
        val unresolvedValues = function.paramType.connections.map { Value(connection = it) }.toMutableList()
        val resolvedValues = mutableListOf<Value>()
        var loopAlert = false
        // only top level map to REST params hene need to know iteration count
        var iterationIndex = 0
        while (unresolvedValues.isNotEmpty() && !loopAlert) {
            val valuesResolvedThisIteration = mutableListOf<Value>()
            for (unresolvedValue in unresolvedValues) {
                  // resolver
            }
            unresolvedValues.removeAll(valuesResolvedThisIteration)
            if (!unresolvedValues.isEmpty() && valuesResolvedThisIteration.size ==0) {
                loopAlert = true
            }
            iterationIndex++
        }
        if (loopAlert) {
            throw Exception("resolveParams: unresolvedConnections for function: ${function.name}: ${unresolvedValues.map{it.connection.asString()}.joinToString(",")}")
        }
        //now sort in dependency order ?
        return resolvedValues
    }

    fun template(function: Function, values: List<Value>/* resolve params  given: paramSource  */): String {

        val resolvers = listOf<Resolver>(/* TODO */)

        val values = expandParams(function, resolvers)


        // suppliers are the rest policy and infrastructure
        // isConstructedPreInjection, isFromInfrastructure determines if web suppler works


        // some sort of recursive dependancy search of params
        // check everything is resolvable
        // sort dependancies
        // render dependancies as code
        //     find dependency mapper for each dependancy
        //        if there is more than 1

           return """
               ${
                   values.map{ it.renderer?.renderKotlin(it.dependencyToValue) }.joinToString("\n")
               }
           """.trimIndent()
    }


@Test
fun test() {
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


}
}