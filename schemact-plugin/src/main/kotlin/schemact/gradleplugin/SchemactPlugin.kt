package schemact.gradleplugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.file.FileTree
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import schemact.domain.Deployment
import schemact.domain.Domain
import schemact.domain.Function
import schemact.domain.Schemact
import schemact.gradleplugin.aws.cdk.deployCodeCdk
import schemact.gradleplugin.aws.cdk.deployHostCdk
import schemact.gradleplugin.cdk.deployUiCode
import java.io.File

class SchemactPlugin : Plugin<Project> {


    override fun apply(project: Project) {


        val extension: SchemactPluginConfig = project.extensions
            .create("schemactConfig", SchemactPluginConfig::class.java)

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
            extension.functions?.let {
                val functions = it
                if (functions.isNotEmpty()) {
                    createGenSourceTask(project=project, schemact=extension.schemact,
                        domain=extension.schemact.domains[0], functions=functions)
                    createPackageFunctionsTask(project)
                    createPackageFunctionsTask2(project)
                }
            }
        }
    }
}

fun createGenSourceTask(project: Project, schemact: Schemact, domain: Domain, functions: List<Function>) {
    val mainSourceSet =
        project.extensions.getByType(KotlinJvmProjectExtension::class.java)
            .sourceSets.getByName("main")
    val sourceGenDir = "${project.buildDir}/schemactsourcegen/kotlin"

    mainSourceSet.kotlin.srcDir(sourceGenDir)
    File(sourceGenDir).mkdirs()
    project.tasks.create("genCode") { task ->
        task.group = "schemact"
        task.actions.add {
            println("here **************")
            val mainKotlinSourceDir =
                project.extensions.
                getByType(KotlinJvmProjectExtension::class.java).sourceSets.filter {
                    it.name=="main"
                }.flatMap { it.kotlin.srcDirs }.filter { it.path.contains("main") }.first()

            mainKotlinSourceDir?:throw RuntimeException("cant find main kotlin source dir")

            createSourceCode(
                genDir = File(sourceGenDir),
                mainKotlinSourceDir = mainKotlinSourceDir,
                domain = domain, schemact = schemact, functions = functions
            )
        }
    }
}

fun createPackageFunctionsTask2(project: Project) {
    project.tasks.create("packageCode2", Jar::class.java) { task->
        task.group = "schemact"
        task.duplicatesStrategy = DuplicatesStrategy.INCLUDE
        task.description = "bundles the functions into a jar"
        task.archiveClassifier.set("schemact-aws-lambda")

        val dependencies =
            project.configurations.getByName("kotlinCompilerClasspath").resolve()
            .map(project::zipTree).toMutableList()
        dependencies.add(project.fileTree("${project.buildDir}/classes/kotlin/main"))

        task.from(dependencies)
    }

}

fun createPackageFunctionsTask(project: Project) {
    println("createPackageFunctionsTask 0" )

    project.tasks.create("packageCode", Jar::class.java) { task->
        println("createPackageFunctionsTask" )

        task.group = "schemact"
        task.duplicatesStrategy = DuplicatesStrategy.INCLUDE
        task.archiveClassifier.set("fat")
        val sourceSets = project.extensions.getByType(KotlinJvmProjectExtension::class.java).sourceSets
        val mainSourceSet: KotlinSourceSet = sourceSets.getByName("main")
        println("mainSourceSet=$mainSourceSet" )



        println("mainSourceSet: ${mainSourceSet.javaClass} ${mainSourceSet}")

        //project.

        //task.from(mainSourceSet)
        println("createPackageFunctionsTask 2" )
        val runtimeClassPathConfig = project.configurations.getByName("runtimeClasspath")
        println("createPackageFunctionsTask 3" )
        //task.dependsOn(runtimeClassPathConfig)
        println("createPackageFunctionsTask 4" )
        println ("runtimeClassPathConfig: ${runtimeClassPathConfig.javaClass} ${runtimeClassPathConfig}")

        task.from ( {
            runtimeClassPathConfig.filter {it.name.endsWith("jar")  }
           // .onEach { println("processing ${it.javaClass} $it")  }
            .map{
                //println("about to zipTree ${it.javaClass} $it")
                project.zipTree(it)
               // println("Zip Treed ${it.javaClass} $it")
            }
        })

    }

/*
val fatJar = tasks.register<Jar>("fatJar") {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    archiveClassifier.set("fat")

    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}
 */
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
        println("configx: ${it.name} ${it.javaClass}")
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
         it.forEach { // svgthumbnailer-1.0.14-SNAPSHOT-fat
            println("${it.name} => ${project.buildDir}/lib/${it.name}-${project.version}-fat.jar")
         }
     }
}

