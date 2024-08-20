package schemact.gradleplugin

import apiGatewayEventHandler
import org.gradle.configurationcache.extensions.capitalized
import schemact.domain.*
import schemact.domain.Function
import schemact.gradleplugin.aws.functionTemplates.functionInterface
import schemact.gradleplugin.aws.functionTemplates.functionSampleImpl
import java.io.File

fun createSourceCode (genDir: File, mainKotlinSourceDir: File, domain: Domain, schemact: Schemact, functions: List<Function> ) {
    functions.forEach {
        println("creating function ${it.name} in ${genDir.absolutePath}")
        genDir.mkdirs()
        val destination = File(genDir, "Test.kt")
        destination.writeText("""fun hello() {println("Hello World")}""")
        val packageTree = domain.name.split(".").reversed().toMutableList()
        packageTree.add(schemact.name)
        val packageName = packageTree.joinToString(".")
        val interfaceClassName = "${it.name.capitalized()}Int"

        val interfaceSourceSubpath = "${packageTree.joinToString ("/")}/${interfaceClassName}.kt"
        val interfaceSourceFile = File(genDir, interfaceSourceSubpath)
        interfaceSourceFile.parentFile.mkdirs()
        interfaceSourceFile.writeText(functionInterface(`package`=packageName,
            functionId = it.name, function=it, interfaceName = interfaceClassName))


        // TODO non string args
        val implClassName = "${it.name.capitalized()}Impl"
        val handlerClassName = "${it.name.capitalized()}Handler"
        val paramType = it.paramType
        // assign args from infrastructure
        val argsFromEnvironment = paramType.fieldsFromInfrastructure()
        // assign small args to params
        val argsFromParams = paramType.connections.filter {
            val subParam = it.entity2
                !subParam.isFromInfrastructure && !( subParam !is StringType || subParam is StringType && subParam.maxLength>1000   )
        }
        // assign big args to body
        val argsFromBody =  paramType.connections.filter {
            val subParam = it.entity2
            subParam is StringType && subParam.maxLength>1000
        }


        val handlerSource = apiGatewayEventHandler(packageName=packageName,function=it,
                                   implClassName= implClassName,
                     handlerClassName = handlerClassName,
                                   argsFromEnvironment= argsFromEnvironment,
                                   argsFromParams=argsFromParams,
                                   argsFromBody = argsFromBody)


        val handlerSourceSubpath = "${packageTree.joinToString ("/")}/${handlerClassName}.kt"
        val handlerSourceFile = File(genDir, handlerSourceSubpath)
        handlerSourceFile.parentFile.mkdirs()
        handlerSourceFile.writeText(handlerSource)

        val implSourceFile = File(mainKotlinSourceDir, "${packageTree.joinToString ("/") }/${implClassName}.kt")
        println("implSourceFile: ${implSourceFile}")
        // create a sample HandleImpl
        if (!implSourceFile.exists()) {
            implSourceFile.parentFile.mkdirs()
            implSourceFile.writeText(functionSampleImpl(packageName, implClassName, it))
        }


    }



}

