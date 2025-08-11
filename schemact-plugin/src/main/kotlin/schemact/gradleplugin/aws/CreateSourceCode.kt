package schemact.gradleplugin.aws

import org.gradle.configurationcache.extensions.capitalized
import schemact.domain.*
import schemact.domain.Function
import schemact.gradleplugin.RestPolicy
import schemact.gradleplugin.aws.functiontemplates.*
import schemact.gradleplugin.aws.functiontemplates.FunctionTypescriptClientInjectedTemplate.functionTypescriptClientInjectedTemplate
import schemact.gradleplugin.injection.APIGatewayV2HTTPEventHandlerInjectedTemplate.templateLambdaEventHandlerFiles
import schemact.gradleplugin.injection.AwsLambdaDependencyGrapher
import schemact.gradleplugin.injection.ParameterDependencyGraph
import schemact.gradleplugin.injection.functionSampleImplNew
import java.io.File

object CreateSourceCode {
    fun createSourceCode(
        injectGenDir: File,
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


        val allComplexTopLevelTypes = module.functions.flatMap {
           RestPolicy(it.paramType, it.returnType).complexTopLevelTypes }.toMutableSet()

        val allEntitiesWithMoreThan1Connection = getAllConnections(allComplexTopLevelTypes).groupBy { it.entity2 }.filter {it.value.size > 1}.map { it.key }.filter { it !is PrimitiveType }
        println("createSourceCode allEntitiesWithMoreThan1Connection=${allEntitiesWithMoreThan1Connection.map { it.name }.joinToString(",")}")
        allComplexTopLevelTypes.addAll(allEntitiesWithMoreThan1Connection)
       // println("createSourceCode allComplexTopLevelTypes=${allComplexTopLevelTypes.map { it.name }.joinToString(",")}")

        // TODO - add return types
        allComplexTopLevelTypes.forEach {
            writeDataClassFile(entity=it, defaultPackageName = packageName, defaultPackageTree = packageTree, genDir=injectGenDir, topLevelEntities =allComplexTopLevelTypes )
        }

        schemact.userKeyedDatabase?.let {
            println("userKeyedDatabase writing userType based on allComplexTopLevelTypes=${allComplexTopLevelTypes.map { it.name }.joinToString (",")}")
            writeDataClassFile(entity=it.userInfoType, defaultPackageName = packageName, defaultPackageTree = packageTree, genDir=injectGenDir, topLevelEntities =allComplexTopLevelTypes )
           it.previousUserInfoTypes.forEach {
               println("userKeyedDatabase writing previous type ${it.name}.${it.version}")
               writeDataClassFile(entity=it, defaultPackageName = packageName, defaultPackageTree = packageTree, genDir=injectGenDir, topLevelEntities =allComplexTopLevelTypes )

           }
        }
        // TODO
        module.functions.forEach {
            println("creating service code for function ${it.name} + client code for these websites: " +
                        functionToStaticWebsite.flatMap { it.value }.joinToString(",") { it.name }
            )
            schemact.userKeyedDatabase?.userInfoType?.let {
                // generate the user keyed database code
            }
            println("created website code in: ${staticWebSiteToSourceRoot.entries.joinToString(",") { "${it.key.name}=>${it.value}" }}")
            createFunctionCode(
                function = it,
                module = module,
                injectGenDir = injectGenDir,
                mainKotlinSourceDir = mainKotlinSourceDir,
                packageTree = packageTree,
                staticWebSites = functionToStaticWebsite.get(it) ?: emptyList(),
                staticWebSiteToSourceRoot = staticWebSiteToSourceRoot,
                defaultLocalServerDomain=defaultLocalServerDomain,
                allComplexTopLevelTypes=allComplexTopLevelTypes,
            )
            // TODO try new template ! ! - refer to codelocations package + handler class name

        }
        module.functionClients.forEach {
            if (it.language!=Language.Kotlin) throw RuntimeException("module ${module.name} has unsupported language ${it.language}")
            createKotlinClientFunctionCode(functionClient = it, schemact=schemact,
                genDir = injectGenDir, packageTree=packageTree)
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

    fun getAllConnections(topLevelTypes: Set<Entity>, visited: MutableSet<Connection> = mutableSetOf()) : Set<Connection>{
        topLevelTypes.forEach {
            it.connections.filter {!visited.contains(it)}.forEach { connection ->
                visited.add(connection)
                getAllConnections(setOf(connection.entity2), visited)
            }
        }
        return visited
    }

    private fun createFunctionCode(
        function: Function,
        module: Module,
        injectGenDir: File,
        packageTree: List<String>,
        mainKotlinSourceDir: File,
        staticWebSites: List<StaticWebsite>,
        staticWebSiteToSourceRoot: Map<StaticWebsite, File>,
        defaultLocalServerDomain: String?,
        allComplexTopLevelTypes: Set<Entity>
    ) {
        println("creating code for function ${function.name} in ${injectGenDir.absolutePath}")


        val packageName = packageTree.joinToString(".")

        val handlerClassName = CodeLocations.handlerClassName(function.name)

        // TODO generate new form service code

        val parameterDependencyGraph = AwsLambdaDependencyGrapher.expandAndCheckRequirements(function)

        generateServiceCodeNew(
            function,
            module,
            packageTree,
            injectGenDir,
            packageName,
            handlerClassName,
            mainKotlinSourceDir,
            allComplexTopLevelTypes,
            parameterDependencyGraph=parameterDependencyGraph
        )


        staticWebSites.forEach {
            val sourceRootLocation = staticWebSiteToSourceRoot.get(it)
            sourceRootLocation ?:throw RuntimeException("cant find sourceRoot from website ${it.name}")

            generateClientCode(
                sourceRoot = sourceRootLocation,
                packageName = packageName,
                module=module,
                function = function,
                parameterDependencyGraph,
                defaultLocalServerDomain=defaultLocalServerDomain
            )
        }

    }

    private fun generateClientCode(
        sourceRoot: File, packageName: String, module:Module, function: Function,
        parameterDependencyGraph: ParameterDependencyGraph,
        defaultLocalServerDomain: String?
    ) {
        val dataClasses = listOf(function.returnType).filter { !it.isValueType }
        for (dataClass in dataClasses) {
            val file = File(sourceRoot, "functions/${dataClass.name}.ts")
            file.parentFile.mkdirs()
            file.writeText(FunctionTypescriptClientTemplate.interfaceDef(dataClass))
        }

        val file = File(sourceRoot, "functions/${function.name}.ts")
        file.parentFile.mkdirs()
        file.writeText(
            functionTypescriptClientInjectedTemplate( packageName = packageName, function = function,
                module=module,
                parameterDependencyGraph = parameterDependencyGraph,
                defaultLocalServerDomain=defaultLocalServerDomain)
        )
    }

    private fun writeSourceFile(genDir: File, packageTree: List<String>,  className: String, code: String) {
        val file = File(genDir, "${packageTree.joinToString("/")}/${className}.kt")
        file.parentFile.mkdirs()
        with (file) {
            writeText(code)
        }
    }

    private fun writeDataClassFile(entity: Entity, defaultPackageTree: List<String>, defaultPackageName: String,  genDir: File, topLevelEntities: Set<Entity>) {
        val prefferedPackageName = entity.prefferedPackage
        val packageTree = if (prefferedPackageName==null ) defaultPackageTree else entity.prefferedPackage!!.split(".")
        val packageName = prefferedPackageName?:defaultPackageName
        val dataClassName = entity.name
        //val dataClassSubPath = "${packageTree.joinToString("/")}/${dataClassName}.kt"
        //val dataClassFile = File(genDir, dataClassSubPath)
        //dataClassFile.parentFile.mkdirs()
        writeSourceFile(genDir, packageTree, dataClassName, dataClass(`package`=packageName, entity = entity, topLevelEntities))
    }

    private fun generateServiceCodeNew(
        function: Function,
        module: Module,
        defaultPackageTree: List<String>,
        genDir: File,
        defaultPackageName: String,
        handlerClassName: String,
        mainKotlinSourceDir: File,
        allComplexTopLevelTypes: Set<Entity>,
        parameterDependencyGraph: ParameterDependencyGraph
    ) {

        val implClassName = "${CodeLocations.implClassName(function.name)}"

        val implSourceFile =
            File(mainKotlinSourceDir, "${defaultPackageTree.joinToString("/")}/${implClassName}.kt")
        println("implSourceFile: $implSourceFile")

        // create a sample HandleImpl
        if (!implSourceFile.exists()) {
            implSourceFile.parentFile.mkdirs()
            implSourceFile.writeText(functionSampleImplNew(defaultPackageName, implClassName, function))
        }

        val srcMap = templateLambdaEventHandlerFiles(function = function, domainPath = defaultPackageTree, implClassName = implClassName,
            handlerClassName = "${handlerClassName}", parameterDependencyGraph = parameterDependencyGraph)
        srcMap.forEach {
            val file = genDir.resolve(it.key)
            file.parentFile.mkdirs()
            file.writeText(it.value)
        }
    }

}


