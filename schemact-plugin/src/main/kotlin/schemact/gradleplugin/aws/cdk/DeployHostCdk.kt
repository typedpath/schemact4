package schemact.gradleplugin.aws.cdk

import com.amazonaws.regions.Regions

import schemact.domain.Deployment
import schemact.domain.Domain
import java.io.File

fun deployHostCdk(domain: Domain,
                  deployment: Deployment,
                  idToFunctionJars: Map<String, File>,
                  region: Regions = Regions.US_EAST_1) {
    val stackName =  "${deployment.subdomain}-${domain.name.replace('.', '-')}"

    deployCdkStack(stackName = stackName, region = region) {
        CDKHostTemplate(scope = it, id = null, props = null, domain = domain,
            deployment = deployment, codeBucketName = functionCodeBucketName(domain, deployment), idToFunctionJars = idToFunctionJars)
    }

    }