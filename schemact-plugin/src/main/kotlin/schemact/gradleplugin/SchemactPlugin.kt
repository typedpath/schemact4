package schemact.gradleplugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import schemact.domain.Deployment
import schemact.gradleplugin.aws.cdk.deployCodeCdk
import schemact.gradleplugin.aws.cdk.deployHostCdk
import schemact.gradleplugin.cdk.deployUiCode

class SchemactPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension: SchemactPluginConfig = project.extensions
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
                it.actions.add {
                        deployHostCdk(domain = domain, deployment = deployment, idToFunctionJars = extension.idToFunctionJars)
                    }

            }
            project.tasks.create("${deployment.subdomain}_deployUiCode") {
                it.group = "schemact_${deployment.subdomain}"
                it.actions.add {
                        deployUiCode(domain, deployment, extension.uiCodeLocation)
                    }
            }
        }

    project.afterEvaluate {
        extension.schemact.domains[0].deployments.forEach {
            createTasksFor(it)
        }
    }


    }




}