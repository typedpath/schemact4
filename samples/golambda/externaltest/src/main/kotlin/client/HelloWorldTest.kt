package client

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.testedsoftware.golambda.gofunctions.client.HelloWorldCall

fun main(args: Array<String>) {
    runBlocking {
        val job = GlobalScope.launch {
            val start = System.currentTimeMillis()
            val response = HelloWorldCall.helloWorld(
                name = "hiiii wiiild xz",
                domainRoot = "https://golambda.testedsoftware.org"
            )
            val timeTaken = System.currentTimeMillis() - start
            println("timeTaken = $timeTaken,  result: ${response}")
        }
        job.join()
    }
}