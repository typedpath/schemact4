package schemact.gradleplugin.aws

import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import schemact.domain.Deployment
import schemact.domain.Domain
import schemact.domain.Schemact
import schemact.gradleplugin.aws.cdk.*
import schemact.gradleplugin.aws.cdk.DeployCdkStack.deployCdkStack
import schemact.gradleplugin.aws.util.S3Util.copyToS3
import java.io.File

fun main() {

    val f=File(System.getenv("HOMEPATH"))
    println(f.absolutePath)
    val domain =Domain("typedpath.com", wildcardCertificateRef =
    WildcardCertificateTemplate.getCertArn("typedpath.com"),
        cdnZoneReference = "Z2FDTNDATAQYW2")
    val deployment = Deployment(subdomain = "schemact4code", codeBranch = "N/A")
    // use codeLocations ?
    val path = "repository/com/typedpath/schemact4"
    val repoRoot=File(f, ".m2/$path")
    repoRoot.listFiles().forEach {
        println(f.name)
    }

    val codeBucketName = "${deployment.subdomain}.${domain.name}"
    val codeBucketStackName = codeBucketName.replace('.', '-')

    val schemact = Schemact("schemact4code",
        domains = listOf(domain)
        )
    // should output bucket name !!
    val result = deployCdkStack(codeBucketStackName) {
        CDKHostTemplate(it, codeBucketStackName, null,
        schemact=schemact,
        domain = domain, deployment=deployment,
        codeBucketName= "N/A",
        functionToFunctionJars = mapOf()
        )
    }

    val s3Builder = AmazonS3Client.builder()
    val region = Regions.US_EAST_1
    s3Builder.region =  region.getName()
    copyToS3(
        bucketName = codeBucketName,
        distributionDirectory = repoRoot,
        bucketFolder = path,
        region = region
    )



}