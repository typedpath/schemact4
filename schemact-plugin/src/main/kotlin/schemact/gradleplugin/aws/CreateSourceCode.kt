package schemact.gradleplugin.aws

import CodeLocations
import apiGatewayEventHandler
import schemact.domain.*
import schemact.domain.Function
import schemact.gradleplugin.aws.functionTemplates.functionInterface
import schemact.gradleplugin.aws.functionTemplates.functionSampleImpl
import schemact.gradleplugin.aws.functionTemplates.functionTypescriptClientTemplate
import java.io.File

fun createSourceCode (genDir: File, mainKotlinSourceDir: File, domain: Domain, schemact: Schemact, functions: List<Function>,
                      functionToStaticWebsite : Map<Function, List<StaticWebsite>>,
                      staticWebSiteToSourceRoot: Map<StaticWebsite, File>) {
    functions.forEach {
        println("creating service code for function ${it.name} + client code for these websites: " +
                "${functionToStaticWebsite.flatMap { it.value }.joinToString(",") { it.name }}")
        println("created website code in: ${staticWebSiteToSourceRoot.entries.joinToString(",") { "${it.key.name}=>${it.value}" }}")
        createFunctionCode(function=it, genDir=genDir, domain=domain, schemact=schemact, mainKotlinSourceDir=mainKotlinSourceDir, staticWebSites=functionToStaticWebsite.get(it)?: emptyList(), staticWebSiteToSourceRoot= staticWebSiteToSourceRoot)
    }



}

private fun createFunctionCode(
    function: Function,
    genDir: File,
    domain: Domain,
    schemact: Schemact,
    mainKotlinSourceDir: File,
    staticWebSites: List<StaticWebsite>,
    staticWebSiteToSourceRoot: Map<StaticWebsite, File>
) {
    println("creating code for function ${function.name} in ${genDir.absolutePath}")

    genDir.mkdirs()
    val packageTree = CodeLocations.packageTree(domain, schemact)
    val packageName = packageTree.joinToString(".")

    
    // TODO non string args
    val implClassName = CodeLocations.implClassName(function.name)
    val handlerClassName = CodeLocations.handlerClassName(function.name)
    val paramType = function.paramType
    // assign args from infrastructure
    val argsFromEnvironment = paramType.fieldsFromInfrastructure()
    // assign small args to params
    val argsFromParams = paramType.connections.filter {
        val subParam = it.entity2
        !subParam.isFromInfrastructure && !(subParam !is StringType || (subParam is StringType && subParam.maxLength > 1000))
    }
    // assign big args to body
    val argsFromBody = paramType.connections.filter {
        val subParam = it.entity2
        subParam is StringType && subParam.maxLength > 1000
    }

    generateServiceCode(
        function,
        packageTree,
        genDir,
        packageName,
        implClassName,
        handlerClassName,
        argsFromEnvironment,
        argsFromParams,
        argsFromBody,
        mainKotlinSourceDir
    )

    staticWebSites.forEach {
        val sourceRootLocation = staticWebSiteToSourceRoot.get(it)
        if (sourceRootLocation==null) {
            throw RuntimeException("cant find sourceRoot from website ${it.name}")
        }
        generateClientCode(sourceRoot = sourceRootLocation,
               packageName=packageName,
            function = function,
               argsFromParams=argsFromParams, argsFromBody = argsFromBody) }

}

private fun generateClientCode(sourceRoot: File, packageName: String, function: Function, argsFromParams: List<Connection>,
                               argsFromBody: List<Connection>) {
    val file = File(sourceRoot, "functions/${function.name}.ts")
    file.parentFile.mkdirs()
    file.writeText(functionTypescriptClientTemplate(packageName = packageName, function=function,
        argsFromParams=argsFromParams, argsFromBody = argsFromBody))
}

private fun generateServiceCode(
    function: Function,
    packageTree: List<String>,
    genDir: File,
    packageName: String,
    implClassName: String,
    handlerClassName: String,
    argsFromEnvironment: List<Connection>,
    argsFromParams: List<Connection>,
    argsFromBody: List<Connection>,
    mainKotlinSourceDir: File
) {
    val interfaceClassName = CodeLocations.interfaceClassName(id = function.name)

    val interfaceSourceSubpath = "${packageTree.joinToString("/")}/${interfaceClassName}.kt"
    val interfaceSourceFile = File(genDir, interfaceSourceSubpath)
    interfaceSourceFile.parentFile.mkdirs()
    interfaceSourceFile.writeText(
        functionInterface(
            `package` = packageName,
            functionId = function.name, function = function, interfaceName = interfaceClassName
        )
    )

    val handlerSource = apiGatewayEventHandler(
        packageName = packageName, function = function,
        implClassName = implClassName,
        handlerClassName = handlerClassName,
        argsFromEnvironment = argsFromEnvironment,
        argsFromParams = argsFromParams,
        argsFromBody = argsFromBody
    )


    val handlerSourceSubpath = "${packageTree.joinToString("/")}/${handlerClassName}.kt"
    val handlerSourceFile = File(genDir, handlerSourceSubpath)
    handlerSourceFile.parentFile.mkdirs()
    handlerSourceFile.writeText(handlerSource)

    val implSourceFile =
        File(mainKotlinSourceDir, "${packageTree.joinToString("/")}/${implClassName}.kt")
    println("implSourceFile: ${implSourceFile}")
    // create a sample HandleImpl
    if (!implSourceFile.exists()) {
        implSourceFile.parentFile.mkdirs()
        implSourceFile.writeText(functionSampleImpl(packageName, implClassName, function))
    }
}


