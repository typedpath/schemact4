package schemact.gradleplugin.aws

import multiPartCode
import org.gradle.configurationcache.extensions.capitalized
import schemact.domain.*
import schemact.domain.Function
import schemact.gradleplugin.RestPolicy
import schemact.gradleplugin.aws.functiontemplates.*
import schemact.gradleplugin.aws.functiontemplates.FunctionTypescriptClientTemplate.functionTypescriptClientTemplate
import schemact.gradleplugin.aws.functiontemplates.injectionsupport.DynamoDbUtilTemplate
import schemact.gradleplugin.aws.functiontemplates.injectionsupport.InjectablesFactoryTemplate
import schemact.gradleplugin.aws.functiontemplates.injectionsupport.InjectablesTemplate
import schemact.gradleplugin.aws.functiontemplates.injectionsupport.UserDataUpdaterTemplate
import schemact.gradleplugin.aws.functiontemplates.injectionsupport.VerifyCognitoTemplate
import schemact.gradleplugin.injection.APIGatewayV2HTTPEventHandlerInjectedTemplate.templateLambdaEventHandlerFiles
import schemact.gradleplugin.injection.functionSampleImplNew
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


        val allComplexTopLevelTypes = module.functions.flatMap {
           RestPolicy(it.paramType, it.returnType).complexTopLevelTypes }.toMutableSet()

        val allEntitiesWithMoreThan1Connection = getAllConnections(allComplexTopLevelTypes).groupBy { it.entity2 }.filter {it.value.size > 1}.map { it.key }.filter { it !is PrimitiveType }
        println("createSourceCode allEntitiesWithMoreThan1Connection=${allEntitiesWithMoreThan1Connection.map { it.name }.joinToString(",")}")
        allComplexTopLevelTypes.addAll(allEntitiesWithMoreThan1Connection)
       // println("createSourceCode allComplexTopLevelTypes=${allComplexTopLevelTypes.map { it.name }.joinToString(",")}")

        // TODO - add return types
        allComplexTopLevelTypes.forEach {
            writeDataClassFile(entity=it, defaultPackageName = packageName, defaultPackageTree = packageTree, genDir=genDir, topLevelEntities =allComplexTopLevelTypes )
        }

        schemact.userKeyedDatabase?.let {
            println("userKeyedDatabase writing userType based on allComplexTopLevelTypes=${allComplexTopLevelTypes.map { it.name }.joinToString (",")}")
           writeDataClassFile(entity=it.userInfoType, defaultPackageName = packageName, defaultPackageTree = packageTree, genDir=genDir, topLevelEntities =allComplexTopLevelTypes )
           it.previousUserInfoTypes.forEach {
               println("userKeyedDatabase writing previous type ${it.name}.${it.version}")
               writeDataClassFile(entity=it, defaultPackageName = packageName, defaultPackageTree = packageTree, genDir=genDir, topLevelEntities =allComplexTopLevelTypes )
           }
        }
        // TODO
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
                defaultLocalServerDomain=defaultLocalServerDomain,
                allComplexTopLevelTypes=allComplexTopLevelTypes,
                userInfoType = schemact.userKeyedDatabase?.userInfoType
            )
            // TODO try new template ! ! - refer to codelocations package + handler class name

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
        genDir: File,
        packageTree: List<String>,
        mainKotlinSourceDir: File,
        staticWebSites: List<StaticWebsite>,
        staticWebSiteToSourceRoot: Map<StaticWebsite, File>,
        defaultLocalServerDomain: String?,
        allComplexTopLevelTypes: Set<Entity>,
        userInfoType: Entity?=null
    ) {
        println("creating code for function ${function.name} in ${genDir.absolutePath}")


        genDir.mkdirs()
        val packageName = packageTree.joinToString(".")

        mapOf("VerifyCognito" to VerifyCognitoTemplate.VerifyCognitoTemplate(packageName),
            "Injectables" to InjectablesTemplate.InjectablesTemplate(packageName, if (userInfoType!=null)userInfoType.name else null),
            "InjectablesFactory" to InjectablesFactoryTemplate.InjectablesFactoryTemplate(packageName, userInfoType),
            ).forEach {
            writeSourceFile(genDir, packageTree, it.key,it.value)
        }
        if (userInfoType!=null) {
            mapOf("DynamoDbUtil" to DynamoDbUtilTemplate.DynamoDbUtilTemplate(packageName),
                "${userInfoType.name}Updater" to UserDataUpdaterTemplate.UserDataUpdaterTemplate(entity = userInfoType, packageName=packageName))
                .forEach {
                writeSourceFile(genDir, packageTree, it.key,it.value)
            }
        }

        val implClassName = CodeLocations.implClassName(function.name)
        val handlerClassName = CodeLocations.handlerClassName(function.name)
        val restPolicy = RestPolicy(function.paramType, function.returnType)

        // TODO generate new form service code
        generateServiceCodeNew(
            function,
            module,
            packageTree,
            genDir,
            packageName,
            handlerClassName,
            restPolicy,
            mainKotlinSourceDir,
            allComplexTopLevelTypes
        )

        generateServiceCode(
            function,
            module,
            packageTree,
            genDir,
            packageName,
            implClassName,
            handlerClassName,
            restPolicy,
            mainKotlinSourceDir,
            allComplexTopLevelTypes
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
        val dataClasses = listOf(function.returnType).filter { !it.isValueType }
        for (dataClass in dataClasses) {
            val file = File(sourceRoot, "functions/${dataClass.name}.ts")
            file.parentFile.mkdirs()
            file.writeText(FunctionTypescriptClientTemplate.interfaceDef(dataClass))
        }

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
        restPolicy: RestPolicy,
        mainKotlinSourceDir: File,
        allComplexTopLevelTypes: Set<Entity>
    ) {

        val implClassName = "${CodeLocations.implClassName(function.name)}New"

        val implSourceFile =
            File(mainKotlinSourceDir, "${defaultPackageTree.joinToString("/")}/${implClassName}.kt")
        println("implSourceFile: $implSourceFile")

        // create a sample HandleImpl
        if (!implSourceFile.exists()) {
            implSourceFile.parentFile.mkdirs()
            implSourceFile.writeText(functionSampleImplNew(defaultPackageName, implClassName, function))
        }

        val srcMap = templateLambdaEventHandlerFiles(function = function, domainPath = defaultPackageTree, implClassName = implClassName, handlerClassName = "${handlerClassName}Injected")
        srcMap.forEach {
            val file = genDir.resolve(it.key)
            file.parentFile.mkdirs()
            file.writeText(it.value)
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
        mainKotlinSourceDir: File,
        allComplexTopLevelTypes: Set<Entity>
    ) {
        // find all the entities in the arguments
        // assume all definitions are nested
        // generate source
        val allTopLevelConnections = restPolicy.allTopLevelConnections

        //assume argFrom environment are defined elsewhere
        val complexTopLevelTypes = allComplexTopLevelTypes//restPolicy.complexTopLevelTypes// allTopLevelConnections.map { it.entity2 }.filter { it !is PrimitiveType || it.connections.size>0}.toMutableSet()
        println("generateServiceCode complexTopLevelTypes for function ${function.name} reviewing : ${allTopLevelConnections.joinToString(","){it.name}}")
        println("generateServiceCode complexTopLevelTypes for function ${function.name}: ${complexTopLevelTypes.joinToString(","){it.name}}")
        complexTopLevelTypes.forEach {
            writeDataClassFile(entity = it, defaultPackageTree=defaultPackageTree, defaultPackageName = defaultPackageName,
                genDir= genDir, topLevelEntities = complexTopLevelTypes)
        }

        if (restPolicy.argsFromMultiPart.size>0) {
            writeSourceFile(genDir, defaultPackageTree, "MultiPart", multiPartCode(defaultPackageTree.joinToString(".")))
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


