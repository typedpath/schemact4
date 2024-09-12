package schemact.gradleplugin

import org.gradle.api.Plugin
import org.gradle.api.Project
//import org.gradle.api.artifacts.DependencyResolutionListener
//import org.gradle.api.artifacts.ResolvableDependencies
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.jvm.tasks.Jar
import schemact.domain.*
import schemact.gradleplugin.DebugInfo.printProjectInfo
import schemact.gradleplugin.aws.UiCode.buildUiCode
import schemact.gradleplugin.aws.UiCode.deployUiCode
import schemact.gradleplugin.aws.cdk.DeployHostCdk.deployHostCdk
import schemact.gradleplugin.aws.cdk.deployCodeCdk

import java.io.File
import kotlin.io.path.Path


const val TASK_GROUP_NAME = "schemact"
private const val PACKAGE_CODE_MODULE_TASK_NAME = "packageCode"

class SchemactPlugin : Plugin<Project>  {

    private lateinit var project: Project

    override fun apply(project: Project) {

        this.project=project

        val extension: SchemactPluginConfig = project.extensions
            .create("schemactConfig", SchemactPluginConfig::class.java)

        fun createTasksFor(deployment: Deployment) {
            val schemact = extension.schemact
            val domain = schemact.domains[0]
            val module = extension.module
            val moduleToJars = moduleToJars(project, schemact)

            val deployCodeAllowed = moduleToJars.isNotEmpty() && module==null

            val deployCodeTaskName = "${deployment.subdomain}_deployCode"

            if (deployCodeAllowed)
                  project.tasks.create(deployCodeTaskName) {
                    it.group = "${TASK_GROUP_NAME}_${deployment.subdomain}"
                    it.actions.add {
                        deployCodeCdk(domain, deployment, moduleToJars)
                    }
                }
            val functionToFunctionJars = moduleToJars.flatMap {
                val jar = it.value
                it.key.functions.map { Pair(it, jar) }
            }.associate { it }

            val deployInfrastructureAllowed = functionToFunctionJars.isNotEmpty() && module==null
            val deployInfrastructureTaskName = "${deployment.subdomain}_deployInfrastructure"

            if (deployInfrastructureAllowed) project.tasks.create(deployInfrastructureTaskName) {
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

            val deployUiCodeTaskName = "${deployment.subdomain}_deployUiCode"

            if (uiCodeLocation != null) project.tasks.create(deployUiCodeTaskName) {
                it.group = "${TASK_GROUP_NAME}_${deployment.subdomain}"
                it.actions.add {
                    deployUiCode(domain, deployment, uiCodeLocation)
                }
            }

            if (deployCodeAllowed && deployInfrastructureAllowed ) {
                val taskName = "${deployment.subdomain}_buildAndDeploy"
                val task = project.tasks.create(taskName) {
                    it.group = "${TASK_GROUP_NAME}_${deployment.subdomain}"
                    it.actions.add {
                        deployCodeCdk(domain, deployment, moduleToJars)
                        deployHostCdk(
                            domain = domain,
                            schemact = schemact,
                            deployment = deployment,
                            functionToFunctionJars = functionToFunctionJars
                        )
                        if (uiCodeLocation != null) {
                            deployUiCode(domain, deployment, uiCodeLocation)
                        }
                    }
                }
                val moduleDependsOn = schemact.modules.map {":${it.name}:$PACKAGE_CODE_MODULE_TASK_NAME"}
                task.dependsOn(moduleDependsOn)
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

            project.tasks.create("printId2Modules") {
                it.group = "${TASK_GROUP_NAME}_debug"
                it.actions.add {
                    printId2Modules(project, extension.schemact)
                }
            }
            extension.module?.let {
                val functions = it.functions
                if (functions.isNotEmpty()) {
                    GenSourceTask.createGenSourceTask(project=project, schemact=extension.schemact,
                        domain=extension.schemact.domains[0], module = it, staticWebSiteToSourceRoot=extension.staticWebSiteToSourceRoot)
                    createPackageFunctionsTask(project, it)
                }
            }

            val buildUiCodeTaskName = "buildUiCodeTODOfixthis"

            val uiCodeBuildScript = extension.uiCodeBuildScript
            uiCodeBuildScript?.let {
                project.tasks.create(buildUiCodeTaskName) {
                    it.group = "schemact"
                    it.actions.add {
                        buildUiCode(rootProjectDir = project.projectDir,
                            tempDir = Path("build/tmp"),
                            uiCodeBuildScript)
                    }
                }
            }

        }
    }
}

fun moduleToJars(project:Project, schemact: Schemact) : Map<Module, File> =
    schemact.modules.associate { Pair(it, File("${project.projectDir}/${it.name}/${moduleToBinarySubPath(it)}")) }

fun moduleToBinarySubPath(module: Module) =
    when (module.type) {
        Module.Type.StandaloneFunction -> "build/libs/${packagedJarName(module)}"
        Module.Type.GoStandaloneFunction -> "bin/${packagedGoZipName(module)}"
        else -> throw RuntimeException("module ${module.name} has unsupported type ${module.type}")
    }


fun createPackageFunctionsTask(project: Project, module: Module) {
    val packageCodeTask = project.tasks.create(PACKAGE_CODE_MODULE_TASK_NAME, Jar::class.java) { task->
        task.group = TASK_GROUP_NAME
        task.duplicatesStrategy = DuplicatesStrategy.INCLUDE
        task.description = "bundles the functions into a jar"
        //task.archiveClassifier.set("${TASK_GROUP_NAME}-aws-lambda")
        task.archiveFileName.set(packagedJarName(module))

        val dependencies =
            project.configurations.getByName("runtimeClasspath").resolve()
                //.onEach { println("adding jar ${it.javaClass} $it")  }
                .map(project::zipTree).toMutableList()
        println("createPackageFunctionsTask points at ${project.buildDir}")
        dependencies.add(project.fileTree("${project.buildDir}/classes/kotlin/main"))
        dependencies.add(project.fileTree("${project.buildDir}/resources/main"))
        task.from(dependencies)
    }
    val buildTask = project.tasks.getByPath("build")
    packageCodeTask.dependsOn(buildTask)
}

fun packagedJarName(module: Module)  = "${module.name}-${module.version}-schemact-aws-lambda.jar"
fun packagedGoZipName(module: Module)  = "${module.name}-${module.version}-schemact-aws-lambda.zip"


fun printId2Modules(project: Project, schemact: Schemact) {
    // val sourceSet = sourceSets.create("schemactgen")
    moduleToJars(project, schemact) .forEach {
            println("${it.key.name} => ${it.value.absolutePath}")
     }
}

