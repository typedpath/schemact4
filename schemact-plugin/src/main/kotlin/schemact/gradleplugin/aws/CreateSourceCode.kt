package schemact.gradleplugin.aws

import schemact.domain.*
import schemact.domain.Function
import schemact.gradleplugin.aws.functiontemplates.*
import schemact.gradleplugin.aws.functiontemplates.FunctionTypescriptClientTemplate.functionTypescriptClientTemplate
import java.io.File

object CreateSourceCode {
    fun createSourceCode(
        genDir: File,
        mainKotlinSourceDir: File,
        domain: Domain,
        schemact: Schemact,
        module: Module,
        functionToStaticWebsite: Map<Function, List<StaticWebsite>>,
        staticWebSiteToSourceRoot: Map<StaticWebsite, File>
    ) {
        module.functions.forEach {
            println("creating service code for function ${it.name} + client code for these websites: " +
                    "${
                        functionToStaticWebsite.flatMap { it.value }.joinToString(",") { it.name }
                    }"
            )
            println("created website code in: ${staticWebSiteToSourceRoot.entries.joinToString(",") { "${it.key.name}=>${it.value}" }}")
            createFunctionCode(
                function = it,
                module = module,
                genDir = genDir,
                domain = domain,
                schemact = schemact,
                mainKotlinSourceDir = mainKotlinSourceDir,
                staticWebSites = functionToStaticWebsite.get(it) ?: emptyList(),
                staticWebSiteToSourceRoot = staticWebSiteToSourceRoot
            )
        }


    }

    private fun createFunctionCode(
        function: Function,
        module: Module,
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

        fun argIsTooBigForParam(paramType: Entity) = paramType is StringType && paramType.maxLength > 1000
        // assign small args to params
        val argsFromParams = paramType.connections.filter {
            val subParam = it.entity2
            !subParam.isFromInfrastructure && subParam is PrimitiveType && !argIsTooBigForParam(subParam)
        }
        // assign big args to body
        val argsFromBody = paramType.connections.filter {
            val subParam = it.entity2
            subParam !is PrimitiveType ||argIsTooBigForParam(subParam)
        }

        generateServiceCode(
            function,
            module,
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
            if (sourceRootLocation == null) {
                throw RuntimeException("cant find sourceRoot from website ${it.name}")
            }
            generateClientCode(
                sourceRoot = sourceRootLocation,
                packageName = packageName,
                function = function,
                argsFromParams = argsFromParams, argsFromBody = argsFromBody
            )
        }

    }

    private fun generateClientCode(
        sourceRoot: File, packageName: String, function: Function, argsFromParams: List<Connection>,
        argsFromBody: List<Connection>
    ) {
        val file = File(sourceRoot, "functions/${function.name}.ts")
        file.parentFile.mkdirs()
        file.writeText(
            functionTypescriptClientTemplate(
                packageName = packageName, function = function,
                argsFromParams = argsFromParams, argsFromBody = argsFromBody
            )
        )
    }

    private fun generateServiceCode(
        function: Function,
        module: Module,
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
        // find all the entities in the arguments
        // assume all definitions are nested
        // generate source
        val allTopLevelConnections = argsFromBody.toMutableList()
        allTopLevelConnections.addAll(argsFromParams)
        //assume argFrom environment are defined elsewhere
        val complexTopLevelTypes = allTopLevelConnections.map { it.entity2 }.filter { it !is PrimitiveType }.toSet()
        println("generateServiceCode complexTopLevelTypes for function ${function.name}: ${complexTopLevelTypes.joinToString(","){it.name}}")
        complexTopLevelTypes.forEach {
            val dataClassName = it.name
            val dataClassSubPath = "${packageTree.joinToString("/")}/${dataClassName}.kt"
            val dataClassFile = File(genDir, dataClassSubPath)
            dataClassFile.parentFile.mkdirs()
            with (dataClassFile) {
                writeText(dataClass(`package`=packageName, entity = it))
            }
        }

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

        val handlerSource = if (module.type==Module.Type.StandaloneFunction) {
             apiGatewayEventHandler(
                packageName = packageName, function = function,
                implClassName = implClassName,
                handlerClassName = handlerClassName,
                argsFromEnvironment = argsFromEnvironment,
                argsFromParams = argsFromParams,
                argsFromBody = argsFromBody
            )
        } else {
            throw RuntimeException("not supporting genSource module type ${module.type}")
        }


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
}


