package schemact.gradleplugin

import org.gradle.api.Project
import org.gradle.api.UnknownDomainObjectException
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import schemact.domain.*
import schemact.domain.Function
import schemact.gradleplugin.KotlinFunctionClientDependencies.kotlinFunctionClientDependencies
import schemact.gradleplugin.TaskNaming.groupName
import schemact.gradleplugin.aws.AwsDependencies.awsLambdaDependencies
import schemact.gradleplugin.aws.CreateSourceCode.createSourceCode
import schemact.gradleplugin.golang.templates.Makefile
import schemact.gradleplugin.golang.templates.goMod
import schemact.gradleplugin.golang.templates.mainGo
import java.io.File

object GenSourceTask {
    fun createGenKotlinSourceTask(
        project: Project, schemact: Schemact, domain: Domain, module: Module,
        staticWebSiteToSourceRoot: Map<StaticWebsite, File>?
    ) {

        if (module.type == Module.Type.StandaloneFunction) {
            val deps = project.configurations.getByName("implementation").getDependencies()
            awsLambdaDependencies.forEach {
                println("adding dependency $it")
                deps.add(project.getDependencies().create(it))
            }
            if (module.functionClients.filter { it.language==Language.Kotlin }.isNotEmpty()) {
                kotlinFunctionClientDependencies.forEach {
                    println("adding dependency $it")
                    deps.add(project.getDependencies().create(it))
                }
            }
        }

        val kotlinJvmProjectExtension =  try {
            project.extensions.getByType(KotlinJvmProjectExtension::class.java)
        } catch (ex: UnknownDomainObjectException) {
            throw RuntimeException("cant find KotlinJvmProjectExtension - try referencing plugin kotlin(\"jvm\")", ex)
        }

        val mainSourceSet = kotlinJvmProjectExtension.sourceSets.getByName("main")
        val sourceGenDir = "${project.buildDir}/schemactsourcegen/kotlin"

        mainSourceSet.kotlin.srcDir(sourceGenDir)
        File(sourceGenDir).mkdirs()
        val genTask = project.tasks.create("genCode") { task ->
            task.group = groupName(module)
            task.actions.add {
                val mainKotlinSourceDir =
                    project.extensions.getByType(KotlinJvmProjectExtension::class.java).sourceSets.filter {
                        it.name == "main"
                    }.flatMap { it.kotlin.srcDirs }.filter { it.path.contains("main") }.first()

                mainKotlinSourceDir ?: throw RuntimeException("cant find main kotlin source dir")

                // if there are website clients is the location known to generate them ?

                val safeStaticWebSiteToSourceRoot = staticWebSiteToSourceRoot ?: emptyMap()
                val functionToStaticWebsite = validateFunctionClients(
                    module.functions,
                    schemact,
                    safeStaticWebSiteToSourceRoot
                )

                // staticWebsite2Functions
                createSourceCode(
                    genDir = File(sourceGenDir),
                    mainKotlinSourceDir = mainKotlinSourceDir,
                    functionToStaticWebsite = functionToStaticWebsite,
                    staticWebSiteToSourceRoot = safeStaticWebSiteToSourceRoot,
                    domain = domain,
                    schemact = schemact, module = module
                )

            }
        }
        val buildTask = project.tasks.getByPath("compileKotlin")
        buildTask.dependsOn(genTask)

    }

    fun validateFunctionClients(
        functions: List<schemact.domain.Function>,
        schemact: Schemact,
        staticWebSiteToSourceRoot: Map<StaticWebsite, File>
    ): Map<Function, List<StaticWebsite>> {


        val functionsToStaticWebsites = functions.map {
            val f = it
            Pair(it, schemact.staticWebsites.filter {
                it.functionClients.find { f == it.function } != null
            })
        }.filter { it.second.size > 0 }.associate { it }

        val staticWebsitesWithClients =
            functionsToStaticWebsites.entries.flatMap { it.value }.toSet()
        // validations
        if (functionsToStaticWebsites.size > 0) {
            fun requiredFunctionClients() = "${
                functionsToStaticWebsites.map {
                    Pair(
                        it.key,
                        it.value.map { it.name }.joinToString(",")
                    )
                }
                    .map { "${it.first}:${it.second}" }.joinToString(" ")
            }"

            val staticWebSitesWithUnknownSrcRoots =
                staticWebsitesWithClients.minus(staticWebSiteToSourceRoot.keys)
            if (staticWebSitesWithUnknownSrcRoots.size > 0) {
                throw RuntimeException("function code gen requires src location for these websites : ${
                    staticWebSitesWithUnknownSrcRoots
                        .joinToString(",") { it.name }
                } "
                )
            }
            val nonRequiredSourceRootSpecs =
                staticWebSiteToSourceRoot.keys.minus(staticWebsitesWithClients)
            if (nonRequiredSourceRootSpecs.size > 0) {
                throw RuntimeException(
                    "website source roots specified that do not have function clients ${
                        nonRequiredSourceRootSpecs.joinToString(
                            ","
                        ) { it.name }
                    }"
                )
            }
        }

        val nonExistantWebsiteSourceRoots = staticWebSiteToSourceRoot.values.filter {
            !it.exists()
        }.map { it.absolutePath }

        if (nonExistantWebsiteSourceRoots.size > 0) {
            throw RuntimeException(
                "these websiteSourceRoots do not exist: ${
                    nonExistantWebsiteSourceRoots.joinToString(
                        ","
                    )
                }"
            )
        }


        return functionsToStaticWebsites
    }


}
