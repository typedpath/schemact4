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
            val schemact = extension.schemact

            val domain = schemact.domains[0]
            val idToFunctionJars = extension.idToFunctionJars
            if (idToFunctionJars!=null)
                project.tasks.create("${deployment.subdomain}_deployCode") {
                it.group = "schemact_${deployment.subdomain}"
                it.actions.add {
                        deployCodeCdk(domain, deployment, idToFunctionJars)
                    }
            }
            if (idToFunctionJars!=null) project.tasks.create("${deployment.subdomain}_deployInfrastructure") {
                it.group = "schemact_${deployment.subdomain}"
                it.actions.add {
                        deployHostCdk(domain = domain, deployment = deployment, idToFunctionJars = idToFunctionJars)
                    }

            }
            val uiCodeLocation = extension.uiCodeLocation
            if (uiCodeLocation!=null) project.tasks.create("${deployment.subdomain}_deployUiCode") {
                it.group = "schemact_${deployment.subdomain}"
                it.actions.add {
                        deployUiCode(domain, deployment, uiCodeLocation)
                    }
            }

            let2(extension.codeGenerationTargetDirectory, extension.functions) {
                  dir, functions ->
                    project.tasks.create("${deployment.subdomain}_genCode") {
                      it.group = "schemact_${deployment.subdomain}"
                      it.actions.add {
                          createSourceCode(dir, functions)
                    }
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