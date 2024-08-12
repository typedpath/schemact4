package schemact.gradleplugin

import com.typedpath.aws.deployCode
import com.typedpath.aws.deployInfrastructure
import com.typedpath.aws.deployUiCode
import org.gradle.api.Plugin
import org.gradle.api.Project
import schemact.domain.Deployment
import java.io.File

class SchemactPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        var extension: SchemactPluginConfig = project.getExtensions()
            .create("schemactConfig", SchemactPluginConfig::class.java)

   /*    project.tasks.create("schemactit") {
           println(extension.testMessage)
           it.actions.add ( {println("inside schemactit")} )
       }

        project.tasks.register("schemactitr") {
            println(extension.testMessage)
            it.actions.add ( {println("inside schemactitr")} )
        }
*/

    fun createTasksFor( deployment: Deployment) {
        project.tasks.create("${deployment.subdomain}_deployCode") {
            it.group = "schemact_${deployment.subdomain}"
            it.actions.add (
                {
                    deployCode(extension.schemact, deployment.subdomain, File(extension.thumbnailerJar))
                }
            )
        }
        project.tasks.create("${deployment.subdomain}_deployUiCode") {
            it.group = "schemact_${deployment.subdomain}"
            it.actions.add (
                {
                    deployUiCode(extension.schemact, deployment.subdomain, extension.uiCodeLocation)
                }
            )
        }
        project.tasks.create("${deployment.subdomain}_deployInfrastructure") {
            it.group = "schemact_${deployment.subdomain}"
            it.actions.add (
                {
                    deployInfrastructure(extension.schemact, deployment.subdomain, File(extension.thumbnailerJar))
                }
            )
        }
    }

    project.afterEvaluate {
        println(extension.testMessage)
        project.tasks.create("schemactitae") {
            it.actions.add ( {println("inside schemactitae ${extension.testMessage}")} )
        }
        extension.schemact.domains[0].deployments.forEach {
            createTasksFor(it)
        }

    }


    }




}