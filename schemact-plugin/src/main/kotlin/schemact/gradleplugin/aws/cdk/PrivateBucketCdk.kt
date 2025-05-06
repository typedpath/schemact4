import software.amazon.awscdk.App
import software.amazon.awscdk.Environment
import software.amazon.awscdk.Stack
import software.amazon.awscdk.StackProps
import software.amazon.awscdk.services.s3.Bucket
import software.amazon.awscdk.services.s3.BlockPublicAccess
import software.amazon.awscdk.services.s3.BucketEncryption
import software.amazon.awscdk.services.s3.BucketAccessControl
import software.constructs.Construct

object PrivateBucketCdk {
    fun create(props: Construct, privateBucketName: String) {
        Bucket.Builder.create(props, privateBucketName)
            .bucketName(privateBucketName)
            .versioned(true)
            .blockPublicAccess(BlockPublicAccess.BLOCK_ALL)
            .accessControl(BucketAccessControl.PRIVATE)
            .encryption(BucketEncryption.S3_MANAGED)
            .removalPolicy(software.amazon.awscdk.RemovalPolicy.RETAIN)
            .build()
    }
}

/*fun main() {
    val app = App()
    val env = Environment.builder()
        .account(System.getenv("CDK_DEFAULT_ACCOUNT") ?: "your-account-id")
        .region(System.getenv("CDK_DEFAULT_REGION") ?: "us-east-1")
        .build()
    S3BucketStack(StackProps.builder().env(env).build())
    app.synth()
}*/