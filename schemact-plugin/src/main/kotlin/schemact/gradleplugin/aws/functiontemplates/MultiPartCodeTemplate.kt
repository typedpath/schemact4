fun multiPartCode(packageName: String) ="""
package $packageName

import org.apache.commons.fileupload.MultipartStream
import java.io.ByteArrayInputStream
import java.util.*

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
                    throw Exception("cant find ; in ${'$'}it")
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
        return "contentType: ${'$'}contentType, contentDispositionType: ${'$'}contentDispositionType contentDispositionValues: ${'$'}{
            contentDispositionValues.map { "${'$'}{it.key}=${'$'}{it.value}" }.joinToString(",")
        }"
    }

    companion object {
        fun read(body: String, contentType: String): Map<String, MultiPart> {
            val bodyBytes = Base64.getDecoder().decode(body)
            val boundary = contentType.split("boundary=")[1]
            val inputStream = ByteArrayInputStream(bodyBytes)
            val multipart = MultipartStream(inputStream, boundary.toByteArray(), 1024, null)

            var fileContent: ByteArray? = null
            var partCount = 0

            var nextPart: Boolean = multipart.skipPreamble()

            val result = mutableMapOf<String, MultiPart>()
            while (nextPart) {
                val headers = multipart.readHeaders()
                val output = java.io.ByteArrayOutputStream()
                multipart.readBodyData(output)
                fileContent = output.toByteArray()
                val part = MultiPart(headers, fileContent)
                result.put(part.name(), part)
                nextPart = multipart.readBoundary()
                partCount++
            }
//    multipart.discardBodyData()
            return result
        }

    }
}
"""