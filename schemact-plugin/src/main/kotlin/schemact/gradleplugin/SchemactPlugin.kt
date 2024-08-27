package schemact.gradleplugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import schemact.domain.*
import schemact.domain.Function
import schemact.gradleplugin.aws.cdk.deployCodeCdk
import schemact.gradleplugin.aws.cdk.deployHostCdk
import schemact.gradleplugin.aws.createSourceCode
import schemact.gradleplugin.cdk.deployUiCode
import java.io.File

const val TASK_GROUP_NAME = "schemact"

class SchemactPlugin : Plugin<Project> {

    val TASK_GROUP_NAME = "schemact"

    override fun apply(project: Project) {


        val extension: SchemactPluginConfig = project.extensions
            .create("schemactConfig", SchemactPluginConfig::class.java)

        fun createTasksFor(deployment: Deployment) {
            val schemact = extension.schemact
            val domain = schemact.domains[0]
            val functionToFunctionJars = extension.functionToFunctionJars
            if (functionToFunctionJars != null)
                project.tasks.create("${deployment.subdomain}_deployCode") {
                    it.group = "${TASK_GROUP_NAME}_${deployment.subdomain}"
                    it.actions.add {
                        deployCodeCdk(domain, deployment, functionToFunctionJars)
                    }
                }
            if (functionToFunctionJars != null) project.tasks.create("${deployment.subdomain}_deployInfrastructure") {
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

            project.tasks.create("printSourceSets") {
                it.group = "${TASK_GROUP_NAME}_debug"
                it.actions.add {
                    printSourceSets(project)
                }
            }

            project.tasks.create("printId2Functions") {
                it.group = "${TASK_GROUP_NAME}_debug"
                it.actions.add {
                    printId2Functions(project, extension.functions)
                }
            }
            extension.functions?.let {
                val functions = it
                if (functions.isNotEmpty()) {
                    createGenSourceTask(project=project, schemact=extension.schemact,
                        domain=extension.schemact.domains[0], functions=functions, staticWebSiteToSourceRoot=extension.staticWebSiteToSourceRoot)
                    createPackageFunctionsTask(project)
                }
            }
        }
    }
}

fun createGenSourceTask(project: Project, schemact: Schemact, domain: Domain, functions: List<Function>,
                        staticWebSiteToSourceRoot: Map<StaticWebsite, File>?) {
    val mainSourceSet =
        project.extensions.getByType(KotlinJvmProjectExtension::class.java)
            .sourceSets.getByName("main")
    val sourceGenDir = "${project.buildDir}/schemactsourcegen/kotlin"

    mainSourceSet.kotlin.srcDir(sourceGenDir)
    File(sourceGenDir).mkdirs()
    project.tasks.create("genCode") { task ->
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


fun createPackageFunctionsTask(project: Project) {
    project.tasks.create("packageCode", Jar::class.java) { task->
        task.group = TASK_GROUP_NAME
        task.duplicatesStrategy = DuplicatesStrategy.INCLUDE
        task.description = "bundles the functions into a jar"
        task.archiveClassifier.set("${TASK_GROUP_NAME}-aws-lambda")

        val dependencies =
            project.configurations.getByName("runtimeClasspath").resolve()
                //.onEach { println("adding jar ${it.javaClass} $it")  }
                .map(project::zipTree).toMutableList()
        dependencies.add(project.fileTree("${project.buildDir}/classes/kotlin/main"))
        dependencies.add(project.fileTree("${project.buildDir}/resources/main"))
        task.from(dependencies)
    }

}

fun printSourceSets(project: Project) {
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


}

fun printId2Functions(project: Project, functions: List<Function>?) {
    // val sourceSet = sourceSets.create("schemactgen")
     functions?.let {
         it.forEach {
            println("${it.name} => ${project.buildDir}/lib/${it.name}-${project.version}-fat.jar")
         }
     }
}

