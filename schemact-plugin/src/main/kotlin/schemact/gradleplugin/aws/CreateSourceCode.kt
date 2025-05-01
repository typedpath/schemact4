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

        val defaultLocalServerDomain = "${schemact.defaultLocalClientDeployment?.subdomain?:"specifydefaultLocalServerDomain"}.${domain.name}"

        val packageName = packageTree.joinToString(".")


        schemact.userKeyedDatabase?.let {
           writeDataClassFile(entity=it.userInfoType, defaultPackageName = packageName, defaultPackageTree = packageTree, genDir=genDir )
        }

        module.functions.forEach {
            println("creating service code for function ${it.name} + client code for these websites: " +
                        functionToStaticWebsite.flatMap { it.value }.joinToString(",") { it.name }
            )
            schemact.userKeyedDatabase?.userInfoType?.let {
                // generate the user keyed database
            }
            println("created website code in: ${staticWebSiteToSourceRoot.entries.joinToString(",") { "${it.key.name}=>${it.value}" }}")
            createFunctionCode(
                function = it,
                module = module,
                genDir = genDir,
                mainKotlinSourceDir = mainKotlinSourceDir,
                packageTree = packageTree,
                staticWebSites = functionToStaticWebsite.get(it) ?: emptyList(),
                staticWebSiteToSourceRoot = staticWebSiteToSourceRoot,
                defaultLocalServerDomain=defaultLocalServerDomain
            )
        }
        module.functionClients.forEach {
            if (it.language!=Language.Kotlin) throw RuntimeException("module ${module.name} has unsupported language ${it.language}")
            createKotlinClientFunctionCode(functionClient = it, schemact=schemact,
                genDir = genDir, packageTree=packageTree)
        }
    }

    private fun createKotlinClientFunctionCode(
        functionClient: FunctionClient,
        schemact: Schemact,
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
        staticWebSiteToSourceRoot: Map<StaticWebsite, File>,
        defaultLocalServerDomain: String?
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
            sourceRootLocation ?:throw RuntimeException("cant find sourceRoot from website ${it.name}")

            generateClientCode(
                sourceRoot = sourceRootLocation,
                packageName = packageName,
                module=module,
                function = function,
                restPolicy = restPolicy,
                defaultLocalServerDomain=defaultLocalServerDomain
            )
        }

    }

    private fun generateClientCode(
        sourceRoot: File, packageName: String, module:Module, function: Function, restPolicy: RestPolicy,
        defaultLocalServerDomain: String?
    ) {
        val file = File(sourceRoot, "functions/${function.name}.ts")
        file.parentFile.mkdirs()
        file.writeText(
            functionTypescriptClientTemplate(
                packageName = packageName, function = function,
                module=module,
                restPolicy = restPolicy,
                defaultLocalServerDomain=defaultLocalServerDomain
            )
        )
    }

    private fun writeDataClassFile(entity: Entity, defaultPackageTree: List<String>, defaultPackageName: String,  genDir: File) {
        val prefferedPackageName = entity.prefferedPackage
        val packageTree = if (prefferedPackageName==null ) defaultPackageTree else entity.prefferedPackage!!.split(".")
        val packageName = prefferedPackageName?:defaultPackageName
        val dataClassName = entity.name
        val dataClassSubPath = "${packageTree.joinToString("/")}/${dataClassName}.kt"
        val dataClassFile = File(genDir, dataClassSubPath)
        dataClassFile.parentFile.mkdirs()
        with (dataClassFile) {
            writeText(dataClass(`package`=packageName, entity = entity))
        }
    }

    private fun generateServiceCode(
        function: Function,
        module: Module,
        defaultPackageTree: List<String>,
        genDir: File,
        defaultPackageName: String,
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
        allTopLevelConnections.addAll(restPolicy.argsFromEnvironment)
        //assume argFrom environment are defined elsewhere
        val complexTopLevelTypes = allTopLevelConnections.map { it.entity2 }.filter { it !is PrimitiveType }.toMutableSet()
        println("generateServiceCode complexTopLevelTypes for function ${function.name}: ${complexTopLevelTypes.joinToString(","){it.name}}")
        complexTopLevelTypes.forEach {
            writeDataClassFile(entity = it, defaultPackageTree=defaultPackageTree, defaultPackageName = defaultPackageName,
                genDir= genDir)
        }
        val interfaceClassName = CodeLocations.interfaceClassName(id = function.name)

        val interfaceSourceSubpath = "${defaultPackageTree.joinToString("/")}/${interfaceClassName}.kt"
        val interfaceSourceFile = File(genDir, interfaceSourceSubpath)
        interfaceSourceFile.parentFile.mkdirs()
        interfaceSourceFile.writeText(
            functionInterface(
                `package` = defaultPackageName,
                functionId = function.name, function = function, interfaceName = interfaceClassName
            )
        )

        if (module.type==Module.Type.StandaloneFunction) {
            val handlerSource =
                apiGatewayEventHandler(
                    packageName = defaultPackageName, function = function,
                    implClassName = implClassName,
                    handlerClassName = handlerClassName,
                    restPolicy = restPolicy)

            val handlerSourceSubpath = "${defaultPackageTree.joinToString("/")}/${handlerClassName}.kt"
            val handlerSourceFile = File(genDir, handlerSourceSubpath)
            handlerSourceFile.parentFile.mkdirs()
            handlerSourceFile.writeText(handlerSource)
        }

        val implSourceFile =
            File(mainKotlinSourceDir, "${defaultPackageTree.joinToString("/")}/${implClassName}.kt")
        println("implSourceFile: $implSourceFile")
        // create a sample HandleImpl
        if (!implSourceFile.exists()) {
            implSourceFile.parentFile.mkdirs()
            implSourceFile.writeText(functionSampleImpl(defaultPackageName, implClassName, function))
        }
    }
}


