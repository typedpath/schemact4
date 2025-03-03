package schemact.gradleplugin.aws.cdk

import schemact.domain.Deployment
import schemact.domain.Domain
import schemact.domain.Schemact
import schemact.gradleplugin.aws.cdk.DeployCdkStack.deployCdkStack

object DeployPipelineCdk {
fun deployPipeline(schemact: Schemact, domain: Domain, deployment: Deployment) {

    val stackName =  pipelineStackName(domain, deployment)

    val repoString = "typedpath/paramicons"
    val branch = "buildtest"

    deployCdkStack(stackName) {
        CDKPipelineTemplate(it, stackName, schemact=schemact, domain = domain, deployment = deployment,
            repoString = repoString, branch = branch)
    }


}
    fun pipelineStackName(domain: Domain, deployment: Deployment) = "${deployment.subdomain}-${domain.name.replace('.', '-')}-pipeline"



}