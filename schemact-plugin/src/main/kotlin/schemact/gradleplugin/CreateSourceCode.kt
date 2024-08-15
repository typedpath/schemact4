package schemact.gradleplugin

import schemact.domain.Function
import java.io.File

fun createSourceCode (directory: File, functions: List<Function> ) {
    functions.forEach {
        println("creating function ${it.name} in ${directory.absolutePath}")
    }
}

