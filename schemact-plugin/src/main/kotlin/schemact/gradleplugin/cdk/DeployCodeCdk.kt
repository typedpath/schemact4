package schemact.gradleplugin.cdk

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.cloudformation.model.Output
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.PutObjectRequest

import schemact.domain.Deployment
import schemact.domain.Domain
import java.io.File

fun functionCodeBucketName(domain: Domain, deployment: Deployment): String =
    "code.${deployment.subdomain}.${domain.name}"

fun deployCodeCdk(domain: Domain,
                   deployment: Deployment,
                   idToCodeJar : Map<String, File>,
                  region: Regions = Regions.US_EAST_1) {
    val stackName =  "${deployment.subdomain}-${domain.name.replace('.', '-')}-code"

    deployCdkStack(stackName) {
        CDKCodeHostTemplate(it, functionCodeBucketName(domain, deployment))
    }

    val s3Builder = AmazonS3Client.builder()
    s3Builder.region = region.getName()
    // .region(devStackParams.region) .build()
    idToCodeJar.values.forEach{
        val codeFile = it
        println("reading file ${codeFile.absolutePath}")
        val req = PutObjectRequest(functionCodeBucketName(domain, deployment),"${codeFile.name}", codeFile)
        val s3 = s3Builder.build()
        s3.putObject(req )
    }




}