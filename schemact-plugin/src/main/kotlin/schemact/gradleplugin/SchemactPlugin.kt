package schemact.gradleplugin

import com.typedpath.aws.deployCode
import com.typedpath.aws.deployInfrastructure
import com.typedpath.aws.deployUiCode
import org.gradle.api.Plugin
import org.gradle.api.Project
import schemact.domain.Deployment
import schemact.gradleplugin.cdk.deployCodeCdk
import schemact.gradleplugin.cdk.deployHostCdk
import java.io.File

class SchemactPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        var extension: SchemactPluginConfig = project.getExtensions()
            .create("schemactConfig", SchemactPluginConfig::class.java)

        fun createTasksFor( deployment: Deployment) {
            val domain = extension.schemact.domains[0]
            project.tasks.create("${deployment.subdomain}_deployCode") {
                it.group = "schemact_${deployment.subdomain}"
                it.actions.add {
                        deployCodeCdk(domain, deployment, extension.idToFunctionJars)
                    }
            }
            project.tasks.create("${deployment.subdomain}_deployInfrastructure") {
                it.group = "schemact_${deployment.subdomain}"
                it.actions.add (
                    {
                        deployHostCdk(domain = domain, deployment = deployment, idToFunctionJars = extension.idToFunctionJars)
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
        }

        fun createTasksForOld( deployment: Deployment) {

        project.tasks.create("${deployment.subdomain}_deployCode") {
            it.group = "schemact_${deployment.subdomain}"
            it.actions.add (
                {
                    deployCode(extension.schemact, deployment.subdomain, extension.idToFunctionJars)
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
                    deployInfrastructure(extension.schemact, deployment.subdomain,  extension.idToFunctionJars)
                }
            )
        }
    }

    project.afterEvaluate {
        extension.schemact.domains[0].deployments.forEach {
            createTasksFor(it)
        }
    }


    }




}