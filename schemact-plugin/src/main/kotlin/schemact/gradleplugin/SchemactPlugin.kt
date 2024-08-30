package schemact.gradleplugin

import org.gradle.api.Plugin
import org.gradle.api.Project
//import org.gradle.api.artifacts.DependencyResolutionListener
//import org.gradle.api.artifacts.ResolvableDependencies
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.jvm.tasks.Jar
import schemact.domain.*
import schemact.gradleplugin.DebugInfo.printProjectInfo
import schemact.gradleplugin.aws.DeployUiCode.deployUiCode
import schemact.gradleplugin.aws.cdk.DeployHostCdk.deployHostCdk
import schemact.gradleplugin.aws.cdk.deployCodeCdk

import java.io.File


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

            if (deployCodeAllowed)
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

            val deployInfrastructureAllowed = functionToFunctionJars.isNotEmpty() && module==null

            if (deployInfrastructureAllowed) project.tasks.create("${deployment.subdomain}_deployInfrastructure") {
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
            val deployUICodeAllowed = uiCodeLocation != null

            if (uiCodeLocation != null) project.tasks.create("${deployment.subdomain}_deployUiCode") {
                it.group = "${TASK_GROUP_NAME}_${deployment.subdomain}"
                it.actions.add {
                    deployUiCode(domain, deployment, uiCodeLocation)
                }
            }

            if (deployCodeAllowed && deployInfrastructureAllowed /*&& deployUICodeAllowed*/) {
                val task = project.tasks.create("${deployment.subdomain}_buildAndDeploy") {
                    it.group = "${TASK_GROUP_NAME}_${deployment.subdomain}"
                }
                schemact.modules.forEach {
                    task.dependsOn(":${it.name}:$PACKAGE_CODE_MODULE_TASK_NAME")
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

            project.tasks.create("printId2Modules") {
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
}

fun moduleToJars(project:Project, schemact: Schemact) : Map<Module, File> =
    schemact.modules.associate { Pair(it, File("${project.projectDir}/${it.name}/build/libs/${packagedJarName(it)}")) }


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
        dependencies.add(project.fileTree("${project.buildDir}/classes/kotlin/main"))
        dependencies.add(project.fileTree("${project.buildDir}/resources/main"))
        task.from(dependencies)
    }
    val buildTask = project.tasks.getByPath("build")
    packageCodeTask.dependsOn(buildTask)
}

fun packagedJarName(module: Module)  = "${module.name}-${module.version}-schemact-aws-lambda.jar"

fun printId2Modules( modules: List<Module>) {
    // val sourceSet = sourceSets.create("schemactgen")
    modules.forEach {
            println("${it.name} => ${packagedJarName(it)}")
     }
}

