package schemact.aws
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent
import org.apache.commons.fileupload.MultipartStream
import schemact.react.File
import java.io.ByteArrayInputStream
import java.util.Base64


object CreateMultiPartBodyReader {

    fun createMultiPartBodyReader(input: APIGatewayV2HTTPEvent): MultiPartBodyReader {

        val contentType = input.headers?.get("content-type")
            ?: throw Exception("Missing Content-Type")
        println("createMultiPartBodyReader contentType: $contentType")
        val bodyBytes = Base64.getDecoder().decode(input.body)
        val boundary = contentType.split("boundary=")[1]
        val inputStream = ByteArrayInputStream(bodyBytes)
        val multipart = MultipartStream(inputStream, boundary.toByteArray(), 1024, null)

        var fileContent: ByteArray? = null
        var partCount = 0

        var nextPart: Boolean = multipart.skipPreamble()

        val parts = mutableMapOf<String, MultiPart>()
        while (nextPart) {
            val headers = multipart.readHeaders()
            val output = java.io.ByteArrayOutputStream()
            multipart.readBodyData(output)
            fileContent = output.toByteArray()
            val part = MultiPart(headers, fileContent)
            parts.put(part.name(), part)
            nextPart = multipart.readBoundary()
            partCount++
        }
        val result: MultiPartBodyReader = object : MultiPartBodyReader {
            override fun get(paramName: String): String {
                return String(parts.get(paramName)!!.body)
            }

            override fun getAsFile(paramName: String): File {
                val filePart = parts.get(paramName)!!
                val file = schemact.react.File(filename = filePart?.contentDispositionValues!!["filename"]?:throw Exception("No file provided"), content = filePart.body, contentType =filePart?.contentType!!)
                return file
            }
        }
        return result
    }


    class MultiPart(headers: String, val body: ByteArray) {
        val ContentDispositionHeader = "Content-Disposition"
        val ContentTypeHeader = "Content-Type"
        private var contentDispositionType: String? = null
        var contentDispositionValues: Map<String, String> = mutableMapOf()
        var contentType: String? = null

        init {
            headers.split("\r\n").forEach {
                if (it.startsWith(ContentDispositionHeader)) {
                    val semicolonIndex = it.indexOf(';')
                    if (semicolonIndex == -1) {
                        throw Exception("cant find ; in $it")
                    }
                    contentDispositionType =
                        it.substring(ContentDispositionHeader.length + 1, semicolonIndex + 1).trim()
                    val contentDispositionValuesPart = it.substring(semicolonIndex + 1).trim()
                    contentDispositionValues = contentDispositionValuesPart.split(";").map {
                        it.split("=")
                    }.map {
                        it[0].trim() to it[1].replace("\"", "").trim()
                    }.toMap()
                }
                if (it.startsWith(ContentTypeHeader)) {
                    contentType = it.substring(ContentTypeHeader.length + 1).trim()
                }
            }
        }

        fun name(): String =
            contentDispositionValues.get("name") ?: "unknown"

        override fun toString(): String {
            return "contentType: $contentType, contentDispositionType: $contentDispositionType contentDispositionValues: ${
                contentDispositionValues.map { "${it.key}=${it.value}" }.joinToString(",")
            }"
        }

    }
}