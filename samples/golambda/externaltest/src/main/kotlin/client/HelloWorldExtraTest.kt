package client

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.testedsoftware.golambda.gofunctions.client.HelloWorldExtraCall

fun main(args: Array<String>) {
    runBlocking {
        val job = GlobalScope.launch {
            val start = System.currentTimeMillis()

            val response = HelloWorldExtraCall.helloWorldExtra(
                protagonist = HelloWorldExtraCall.HelloWorldExtra_name(firstName="Eric",
                    middleNames = "Authur", lastName = "Idle"),
                domainRoot = "https://golambda.testedsoftware.org"
            )
            val timeTaken = System.currentTimeMillis() - start
            println("timeTaken = $timeTaken,  result: ${response}")
        }
        job.join()
    }
}