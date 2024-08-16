import org.gradle.configurationcache.extensions.capitalized
import schemact.domain.Function

fun apiGatewayEventHandler(packageName: String, functionId: String, function: Function) = """
package $packageName

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse

//https://docs.aws.amazon.com/lambda/latest/dg/urls-invocation.html
// https://docs.aws.amazon.com/apigateway/latest/developerguide/http-api-develop-integrations-lambda.html#http-api-develop-integrations-lambda.proxy-format

class ${functionId.capitalized()}Handler : RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
    override fun handleRequest(
        input: APIGatewayV2HTTPEvent?,
        context: Context?
    ): APIGatewayV2HTTPResponse {
        val s3bucket = System.getenv("s3bucket")
        val svg = input!!.body
        val urlRoot = "https://{'$'}s3bucket"
        System.out.println("urlRoot ='{'$'}urlRoot'")
        val rawQueryString = input.rawQueryString
        System.out.println("rawQueryString ='{'$'}rawQueryString'")

        val indexPage = thumbnailToS3(bucketName = s3bucket, urlRoot = urlRoot, rawQueryString = rawQueryString,
            svg = svg, width = 256, height=256)
        System.out.println("indexPage: {'$'}indexPage")

        return APIGatewayV2HTTPResponse.builder()
            //.withHeaders (mutableMapOf("header1" to "value1"))
            .withBody(indexPage)
            .withStatusCode(200)
            .build()
    }
}
"""