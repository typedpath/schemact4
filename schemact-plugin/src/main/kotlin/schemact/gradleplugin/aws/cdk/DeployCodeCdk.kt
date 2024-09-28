package schemact.gradleplugin.aws.cdk


import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.PutObjectRequest

import schemact.domain.Deployment
import schemact.domain.Domain
import schemact.domain.Function
import schemact.domain.Module
import schemact.gradleplugin.aws.cdk.DeployCdkStack.deployCdkStack
import java.io.File


fun functionCodeBucketName(domain: Domain, deployment: Deployment): String =
    "code.${deployment.subdomain}.${domain.name}"

fun functionCodeStackName(domain: Domain, deployment: Deployment) = "${deployment.subdomain}-${domain.name.replace('.', '-')}-code"

fun deployCodeCdk(domain: Domain,
                   deployment: Deployment,
                  moduleToJars : Map<Module, File>,
                  region: Regions = Regions.US_EAST_1) {
    val stackName =  functionCodeStackName(domain, deployment)

    deployCdkStack(stackName) {
        CDKCodeHostTemplate(it, functionCodeBucketName(domain, deployment))
    }

    val s3Builder = AmazonS3Client.builder()
    s3Builder.region = region.getName()
    // .region(devStackParams.region) .build()
    moduleToJars.values.toSet().forEach{
        val codeFile = it
        println("reading file ${codeFile.absolutePath}")
        val req = PutObjectRequest(functionCodeBucketName(domain, deployment),"${codeFile.name}", codeFile)
        val s3 = s3Builder.build()
        s3.putObject(req )
    }




}