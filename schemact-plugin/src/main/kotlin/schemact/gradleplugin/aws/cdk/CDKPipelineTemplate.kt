package schemact.gradleplugin.aws.cdk

import schemact.domain.Deployment
import schemact.domain.Domain
import schemact.domain.Schemact
import software.amazon.awscdk.Stack
import software.amazon.awscdk.StackProps
import software.amazon.awscdk.pipelines.CodePipeline
import software.amazon.awscdk.pipelines.CodePipelineSource
import software.amazon.awscdk.pipelines.ConnectionSourceOptions
import software.amazon.awscdk.pipelines.ShellStep
import software.amazon.awscdk.services.secretsmanager.Secret
import software.constructs.Construct


class CDKPipelineTemplate(scope: Construct, id: String?, props: StackProps?=null,
                          val schemact: Schemact,
                          domain: Domain, deployment: Deployment,
                          repoString: String, branch: String,
    )  : Stack(scope, id, props) {

    fun pipelineName(domain: Domain, deployment: Deployment) =
        "${deployment.subdomain}-${domain.name.replace('.', '-')}-pipeline"


    init {

    val pipelineName = pipelineName(domain, deployment)
    val pipeline = CodePipeline.Builder.create (this, "pipeline")
        .synth(
            ShellStep.Builder.create("Synth")
        .input(CodePipelineSource.gitHub(repoString, branch))
         .commands(listOf("gradle devparamicons_deployCode"))
         .build()
        )
        .build()
}


}