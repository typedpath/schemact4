package schemact.gradleplugin.aws
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import schemact.domain.Deployment
import schemact.domain.Domain
import schemact.gradleplugin.aws.util.copyToS3

fun deployUiCode(domain: Domain, deployment: Deployment, uiCodeLocation: String, region: Regions=Regions.US_EAST_1) {
    val websiteDomainName = "${deployment.subdomain}.${domain.name}"
    val s3Builder = AmazonS3Client.builder()
    s3Builder.region = region.getName()
    copyToS3(bucketName = websiteDomainName, distributionDirectory=uiCodeLocation, bucketFolder = "", region=region)
}
