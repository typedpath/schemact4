package schemact.domain

import com.amazonaws.transform.MapEntry

object ReactJsInjectables {
    class File(maxBytes: Long) : Entity("File", "File") {
        init {
            string(name = "filename", maxLength = 1000)
            containsOne("content", "bytes", BlobType("blob", maxBytes = maxBytes))
            string("contentType", "contentType", 200)
            // webkitRelativePath
            prefferedPackage="schemact.react"
        }
    }
}
/*
    readonly lastModified: number;
    /** [MDN Reference](https://developer.mozilla.org/docs/Web/API/File/name) */
    readonly name: string;
    /** [MDN Reference](https://developer.mozilla.org/docs/Web/API/File/webkitRelativePath) */
    readonly webkitRelativePath: string;
 */
