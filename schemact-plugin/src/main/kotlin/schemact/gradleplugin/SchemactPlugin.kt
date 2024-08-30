package schemact.gradleplugin

import org.gradle.api.Plugin
import org.gradle.api.Project
//import org.gradle.api.artifacts.DependencyResolutionListener
//import org.gradle.api.artifacts.ResolvableDependencies
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import schemact.domain.*
import schemact.domain.Function
import schemact.gradleplugin.aws.cdk.deployCodeCdk
import schemact.gradleplugin.aws.cdk.deployHostCdk
import schemact.gradleplugin.aws.createSourceCode
import schemact.gradleplugin.aws.deployUiCode
import java.io.File
import schemact.gradleplugin.aws.awsLambdaDependencies


const val TASK_GROUP_NAME = "schemact"

class SchemactPlugin : Plugin<Project>/*, DependencyResolutionListener*/  {

    val TASK_GROUP_NAME = "schemact"
    lateinit var project: Project

    override fun apply(project: Project) {

        this.project=project

        val extension: SchemactPluginConfig = project.extensions
            .create("schemactConfig", SchemactPluginConfig::class.java)

        fun createTasksFor(deployment: Deployment) {
            val schemact = extension.schemact
            val domain = schemact.domains[0]
            val module = extension.module
            val moduleToJars = moduleToJars(project, schemact)
            if (moduleToJars.size>0 && module==null)
                project.tasks.create("${deployment.subdomain}_deployCode") {
                    it.group = "${TASK_GROUP_NAME}_${deployment.subdomain}"
                    it.actions.add {
                        deployCodeCdk(domain, deployment, moduleToJars)
                    }
                }
            val functionToFunctionJars = moduleToJars.flatMap {
                val jar = it.value
                it.key.functions.map { Pair(it, jar) }
            }.associate { it }

            if (functionToFunctionJars.size>0 && module==null) project.tasks.create("${deployment.subdomain}_deployInfrastructure") {
                it.group = "${TASK_GROUP_NAME}_${deployment.subdomain}"
                it.actions.add {
                    deployHostCdk(
                        domain = domain,
                        schemact = schemact,
                        deployment = deployment,
                        functionToFunctionJars = functionToFunctionJars
                    )
                }
            }
            val uiCodeLocation = extension.uiCodeBuildLocation
            if (uiCodeLocation != null) project.tasks.create("${deployment.subdomain}_deployUiCode") {
                it.group = "${TASK_GROUP_NAME}_${deployment.subdomain}"
                it.actions.add {
                    deployUiCode(domain, deployment, uiCodeLocation)
                }
            }
        }

        project.afterEvaluate {

            extension.schemact.domains[0].deployments.forEach {
                createTasksFor(it)
            }

            project.tasks.create("printProjectInfo") {
                it.group = "${TASK_GROUP_NAME}_debug"
                it.actions.add {
                    printProjectInfo(project)
                }
            }

            project.tasks.create("printId2Functions") {
                it.group = "${TASK_GROUP_NAME}_debug"
                it.actions.add {
                    printId2Modules(extension.schemact.modules)
                }
            }
            extension.module?.let {
                val functions = it.functions
                if (functions.isNotEmpty()) {
                    createGenSourceTask(project=project, schemact=extension.schemact,
                        domain=extension.schemact.domains[0], functions=functions, staticWebSiteToSourceRoot=extension.staticWebSiteToSourceRoot)
                    createPackageFunctionsTask(project, it)
                }
            }
        }
    }


    /*override fun beforeResolve(dependencies: ResolvableDependencies) {
        println("******** beforeResolve")
        val deps = project.configurations.getByName("implementation").getDependencies()
        awsLambdaDependencies.forEach {
            println ("adding dependency $it")
            deps.add(project.getDependencies().create(it))
        }
        project.gradle.removeListener(this)
    }

    override fun afterResolve(dependencies: ResolvableDependencies) {
    }*/
}

fun moduleToJars(project:Project, schemact: Schemact) : Map<Module, File> =
    schemact.modules.associate { Pair(it, File("${project.projectDir}/${it.name}/build/libs/${packagedJarName(it)}")) }


fun createGenSourceTask(project: Project, schemact: Schemact, domain: Domain, functions: List<Function>,
                        staticWebSiteToSourceRoot: Map<StaticWebsite, File>?) {

    val deps = project.configurations.getByName("implementation").getDependencies()
    awsLambdaDependencies.forEach {
        println ("adding dependency $it")
        deps.add(project.getDependencies().create(it))
    }

    val mainSourceSet =
        project.extensions.getByType(KotlinJvmProjectExtension::class.java)
            .sourceSets.getByName("main")
    val sourceGenDir = "${project.buildDir}/schemactsourcegen/kotlin"

    mainSourceSet.kotlin.srcDir(sourceGenDir)
    File(sourceGenDir).mkdirs()
    val genTask = project.tasks.create("genCode") { task ->
        task.group = TASK_GROUP_NAME
        task.actions.add {
            val mainKotlinSourceDir =
                project.extensions.
                getByType(KotlinJvmProjectExtension::class.java).sourceSets.filter {
                    it.name=="main"
                }.flatMap { it.kotlin.srcDirs }.filter { it.path.contains("main") }.first()

            mainKotlinSourceDir?:throw RuntimeException("cant find main kotlin source dir")

            // if there are website clients is the location known to generate them ?

            val safeStaticWebSiteToSourceRoot = staticWebSiteToSourceRoot?: emptyMap()
            val functionToStaticWebsite = validateFunctionClients(functions, schemact, safeStaticWebSiteToSourceRoot)

            // staticWebsite2Functions
            createSourceCode(
                genDir = File(sourceGenDir),
                mainKotlinSourceDir = mainKotlinSourceDir,
                functionToStaticWebsite =functionToStaticWebsite,
                staticWebSiteToSourceRoot = safeStaticWebSiteToSourceRoot,
                domain = domain,
                schemact = schemact, functions = functions
            )

        }
    }
    val buildTask = project.tasks.getByPath("compileKotlin")
    buildTask.dependsOn(genTask)

}

fun validateFunctionClients(functions: List<Function>, schemact: Schemact, staticWebSiteToSourceRoot: Map<StaticWebsite, File>) : Map<Function, List<StaticWebsite>> {


    val functionsToStaticWebsites = functions.map {
        val f = it
        Pair(it, schemact.staticWebsites.filter{
            it.functionClients.find { f ==it.function }!=null
        } )
    }.filter { it.second.size>0 }.associate { it }

    val staticWebsitesWithClients = functionsToStaticWebsites.entries.flatMap { it.value }.toSet()
    // validations
    if (functionsToStaticWebsites.size>0) {
        fun requiredFunctionClients() = "${functionsToStaticWebsites.map { Pair(it.key, it.value.map{it.name}.joinToString (",")) }
            .map{ "${it.first}:${it.second}" }.joinToString (" ")}"
        val staticWebSitesWithUnknownSrcRoots = staticWebsitesWithClients.minus(staticWebSiteToSourceRoot.keys)
        if (staticWebSitesWithUnknownSrcRoots.size>0) {
            throw RuntimeException("function code gen requires src location for these websites : ${staticWebSitesWithUnknownSrcRoots
                .joinToString(",") { it.name }} ")
        }
        val nonRequiredSourceRootSpecs = staticWebSiteToSourceRoot.keys.minus(staticWebsitesWithClients)
        if (nonRequiredSourceRootSpecs.size>0) {
            throw RuntimeException("website source roots specified that do not have function clients ${nonRequiredSourceRootSpecs.joinToString(",") { it.name }}")
        }
    }

    val nonExistantWebsiteSourceRoots = staticWebSiteToSourceRoot.values.filter{
        !it.exists()
    }.map { it.absolutePath }

    if( nonExistantWebsiteSourceRoots.size>0) {
        throw RuntimeException("these websiteSourceRoots do not exist: ${nonExistantWebsiteSourceRoots.joinToString(",")}")
    }


    return functionsToStaticWebsites
}


fun createPackageFunctionsTask(project: Project, module: Module) {
    val packageCodeTask = project.tasks.create("packageCode", Jar::class.java) { task->
        task.group = TASK_GROUP_NAME
        task.duplicatesStrategy = DuplicatesStrategy.INCLUDE
        task.description = "bundles the functions into a jar"
        //task.archiveClassifier.set("${TASK_GROUP_NAME}-aws-lambda")
        task.archiveFileName.set(packagedJarName(module))

        val dependencies =
            project.configurations.getByName("runtimeClasspath").resolve()
                //.onEach { println("adding jar ${it.javaClass} $it")  }
                .map(project::zipTree).toMutableList()
        dependencies.add(project.fileTree("${project.buildDir}/classes/kotlin/main"))
        dependencies.add(project.fileTree("${project.buildDir}/resources/main"))
        task.from(dependencies)
    }
    val buildTask = project.tasks.getByPath("build")
    packageCodeTask.dependsOn(buildTask)
}

fun printProjectInfo(project: Project) {
    val sourceSets = project.extensions.getByType(KotlinJvmProjectExtension::class.java).sourceSets
    // val sourceSet = sourceSets.create("schemactgen")
    sourceSets.forEach {
        it.kotlin.srcDirs.forEach {
            println(" name:${it.name}   srcDir: ${it.absolutePath} ")
        }
        it.kotlin.forEach {
            println("   source:   ${it.absolutePath}")
        }
    }
    val jSourceSets = project.extensions.getByType(JavaPluginExtension::class.java).sourceSets
    jSourceSets.forEach {
        println("j source set ${it.name} output: ${it.output.dirs.map { it.absolutePath }.joinToString (",")}")
    }

    project.configurations.forEach {
        println("config : ${it.name} ${it.javaClass}")
    }

    project.configurations.getByName("runtimeClasspath").resolve().forEach {
           println("   runtimeClasspath artifact: ${it.name} ${it}")
    }

    project.configurations.getByName("kotlinCompilerClasspath").resolve().forEach {
        println("   kotlinCompilerClasspath artifact: ${it.name} ${it}")
    }

    project.tasks.forEach {
        println("   task : ${it.name} ${it}")
    }

}

fun packagedJarName(module: Module)  = "${module.name}-${module.version}-schemact-aws-lambda.jar"

fun printId2Modules( modules: List<Module>) {
    // val sourceSet = sourceSets.create("schemactgen")
    modules.forEach {
            println("${it.name} => ${packagedJarName(it)}")
     }
}

