package schemact.gradleplugin

import org.gradle.configurationcache.extensions.capitalized
import schemact.domain.Domain
import schemact.domain.Function
import schemact.domain.Schemact
import schemact.gradleplugin.aws.functionTemplates.functionInterface
import java.io.File

fun createSourceCode (genDir: File, domain: Domain, schemact: Schemact, functions: List<Function> ) {
    functions.forEach {
        println("creating function ${it.name} in ${genDir.absolutePath}")
        genDir.mkdirs()
        val destination = File(genDir, "Test.kt")
        destination.writeText("""fun hello() {println("Hello World")}""")
        val packageTree = domain.name.split(".").reversed().toMutableList()
        packageTree.add(schemact.name)
        val packageName = packageTree.joinToString(".")
        val interfaceClassName = it.name.capitalized()+"Int"
        val sourceSubpath = "${packageTree.joinToString ("/")}/${interfaceClassName}.kt"
        val sourceFile = File(genDir, sourceSubpath)
        sourceFile.parentFile.mkdirs()
        sourceFile.writeText(functionInterface(`package`=packageName,
            functionId = it.name, function=it, interfaceName = interfaceClassName))
    }
}

