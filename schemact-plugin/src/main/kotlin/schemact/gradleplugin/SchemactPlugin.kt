package schemact.gradleplugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import schemact.domain.Deployment
import schemact.domain.Function
import schemact.gradleplugin.aws.cdk.deployCodeCdk
import schemact.gradleplugin.aws.cdk.deployHostCdk
import schemact.gradleplugin.cdk.deployUiCode
import java.io.File

class SchemactPlugin : Plugin<Project> {


    fun sourceGenDir(project: Project) = "${project.buildDir}/schemactsourcegen/kotlin"
    override fun apply(project: Project) {


        val extension: SchemactPluginConfig = project.extensions
            .create("schemactConfig", SchemactPluginConfig::class.java)


        //

        //

        fun createTasksFor(deployment: Deployment) {
            val schemact = extension.schemact
            val domain = schemact.domains[0]
            val idToFunctionJars = extension.idToFunctionJars
            if (idToFunctionJars != null)
                project.tasks.create("${deployment.subdomain}_deployCode") {
                    it.group = "schemact_${deployment.subdomain}"
                    it.actions.add {
                        deployCodeCdk(domain, deployment, idToFunctionJars)
                    }
                }
            if (idToFunctionJars != null) project.tasks.create("${deployment.subdomain}_deployInfrastructure") {
                it.group = "schemact_${deployment.subdomain}"
                it.actions.add {
                    deployHostCdk(
                        domain = domain,
                        deployment = deployment,
                        idToFunctionJars = idToFunctionJars
                    )
                }

            }
            val uiCodeLocation = extension.uiCodeLocation
            if (uiCodeLocation != null) project.tasks.create("${deployment.subdomain}_deployUiCode") {
                it.group = "schemact_${deployment.subdomain}"
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
                it.group = "schemact_debug"
                it.actions.add {
                    printSourceSets(project)
                }
            }

            project.tasks.create("printId2Functions") {
                it.group = "schemact_debug"
                it.actions.add {
                    printId2Functions(project, extension.functions)
                }
            }
            val schemact = extension.schemact
            val domain = schemact.domains[0]
            extension.functions?.let {
                val functions = it
                if (!functions.isEmpty()) {
                    val mainSourceSet =
                        project.extensions.getByType(KotlinJvmProjectExtension::class.java)
                            .sourceSets.getByName("main")
                    val sourceGenDir = sourceGenDir(project)
                    mainSourceSet.kotlin.srcDir(sourceGenDir)
                    File(sourceGenDir).mkdirs()
                    project.tasks.create("genCode") {
                        it.group = "schemact"
                        it.actions.add {
                            println("here **************")
                            createSourceCode(
                                genDir = File(sourceGenDir),
                                domain = domain, schemact = schemact, functions = functions
                            )
                        }
                    }
                }
            }

        }
    }
}

fun printSourceSets(project: Project) {
    val sourceSets = project.extensions.getByType(KotlinJvmProjectExtension::class.java).sourceSets
    // val sourceSet = sourceSets.create("schemactgen")
    sourceSets.forEach {
        println("k source set: ${it.name}")
        it.kotlin.srcDirs.forEach {
            println(" srcDir: ${it.absolutePath}")
        }
        it.kotlin.forEach {
            println("   source:   ${it.absolutePath}")
        }
    }
}

fun printId2Functions(project: Project, functions: List<Function>?) {
    // val sourceSet = sourceSets.create("schemactgen")
     functions?.let {
         it.forEach { // svgthumbnailer-1.0.14-SNAPSHOT-fat
            println("${it.name} => ${project.buildDir}/lib/${it.name}-${project.version}-fat.jar")
         }
     }
}

