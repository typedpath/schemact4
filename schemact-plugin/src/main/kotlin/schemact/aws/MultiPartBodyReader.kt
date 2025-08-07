package schemact.aws

import schemact.react.File

interface MultiPartBodyReader {
    fun get(paramName: String) : String
    fun getAsFile(paramName: String): File
}
