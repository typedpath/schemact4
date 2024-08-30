package schemact.gradleplugin.aws.cdk

import com.amazonaws.regions.Regions

import schemact.domain.Deployment
import schemact.domain.Domain
import schemact.domain.Function
import schemact.domain.Schemact
import java.io.File

object DeployHostCdk {
    fun deployHostCdk(
        domain: Domain,
        schemact: Schemact,
        deployment: Deployment,
        functionToFunctionJars: Map<Function, File>,
        region: Regions = Regions.US_EAST_1
    ) {
        val stackName = "${deployment.subdomain}-${domain.name.replace('.', '-')}"

        DeployCdkStack.deployCdkStack(stackName = stackName, region = region) {
            CDKHostTemplate(
                scope = it,
                id = null,
                props = null,
                domain = domain,
                schemact = schemact,
                deployment = deployment,
                codeBucketName = functionCodeBucketName(domain, deployment),
                functionToFunctionJars = functionToFunctionJars
            )
        }

    }
}