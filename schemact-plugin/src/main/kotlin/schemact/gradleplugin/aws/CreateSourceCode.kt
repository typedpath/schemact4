package schemact.gradleplugin.aws

import org.gradle.configurationcache.extensions.capitalized
import schemact.domain.*
import schemact.domain.Function
import schemact.gradleplugin.RestPolicy
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
        val packageTree = CodeLocations.packageTree(domain, schemact)

        if (module.type == Module.Type.SpringBootApplication) {
            println("TODO handler for module type ${module.type}")
        }

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
                mainKotlinSourceDir = mainKotlinSourceDir,
                packageTree = packageTree,
                staticWebSites = functionToStaticWebsite.get(it) ?: emptyList(),
                staticWebSiteToSourceRoot = staticWebSiteToSourceRoot
            )
        }
        module.functionClients.forEach {
            if (it.language!=Language.Kotlin) throw RuntimeException("module ${module.name} has unsupported language ${it.language}")
            createKotlinClientFunctionCode(functionClient = it, schemact=schemact, module = module,
                genDir = genDir, packageTree=packageTree)
        }
    }

    private fun createKotlinClientFunctionCode(
        functionClient: FunctionClient,
        schemact: Schemact,
        module: Module,
        genDir: File,
        packageTree: List<String>
        ) {
          val serviceModule = schemact.findModule(functionClient.function)
          val packageClientTree = packageTree.plus(serviceModule.name).plus("client")
          val packageName = packageClientTree.joinToString (".")
          val clientClassName = "${functionClient.function.name.capitalized()}Call"
          val fileName = "${clientClassName.capitalized()}.kt"
          val directory = File("$genDir/${packageClientTree.joinToString("/")}")
          directory.mkdirs()
          File(directory, fileName).printWriter().use { writer ->
              writer.write(kotlinRestClient(serviceModule, functionClient.function, packageName, clientClassName))
          }
    }

    private fun createFunctionCode(
        function: Function,
        module: Module,
        genDir: File,
        packageTree: List<String>,
        mainKotlinSourceDir: File,
        staticWebSites: List<StaticWebsite>,
        staticWebSiteToSourceRoot: Map<StaticWebsite, File>
    ) {
        println("creating code for function ${function.name} in ${genDir.absolutePath}")

        genDir.mkdirs()
        val packageName = packageTree.joinToString(".")

        // TODO non string args
        val implClassName = CodeLocations.implClassName(function.name)
        val handlerClassName = CodeLocations.handlerClassName(function.name)
        val restPolicy = RestPolicy(function.paramType)

        generateServiceCode(
            function,
            module,
            packageTree,
            genDir,
            packageName,
            implClassName,
            handlerClassName,
            restPolicy,
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
                module=module,
                function = function,
                argsFromParams = restPolicy.argsFromParams, argsFromBody = restPolicy.argsFromBody
            )
        }

    }

    private fun generateClientCode(
        sourceRoot: File, packageName: String, module:Module, function: Function, argsFromParams: List<Connection>,
        argsFromBody: List<Connection>
    ) {
        val file = File(sourceRoot, "functions/${function.name}.ts")
        file.parentFile.mkdirs()
        file.writeText(
            functionTypescriptClientTemplate(
                packageName = packageName, function = function,
                module=module,
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
        restPolicy: RestPolicy,
        mainKotlinSourceDir: File
    ) {
        // find all the entities in the arguments
        // assume all definitions are nested
        // generate source
        val allTopLevelConnections = restPolicy.argsFromBody.toMutableList()
        allTopLevelConnections.addAll(restPolicy.argsFromParams)
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

        if (module.type==Module.Type.StandaloneFunction) {
            val handlerSource =
                apiGatewayEventHandler(
                    packageName = packageName, function = function,
                    implClassName = implClassName,
                    handlerClassName = handlerClassName,
                    argsFromEnvironment = restPolicy.argsFromEnvironment,
                    argsFromParams = restPolicy.argsFromParams,
                    argsFromBody = restPolicy.argsFromBody
                )


            val handlerSourceSubpath = "${packageTree.joinToString("/")}/${handlerClassName}.kt"
            val handlerSourceFile = File(genDir, handlerSourceSubpath)
            handlerSourceFile.parentFile.mkdirs()
            handlerSourceFile.writeText(handlerSource)
        }

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


