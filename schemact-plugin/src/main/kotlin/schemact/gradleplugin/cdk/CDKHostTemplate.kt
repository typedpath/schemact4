package schemact.gradleplugin.cdk;
import schemact.domain.Deployment
import schemact.domain.Domain
import software.amazon.awscdk.Stack
import software.amazon.awscdk.StackProps
import software.amazon.awscdk.services.iam.CfnRole
import software.amazon.awscdk.services.lambda.CfnFunction
import software.amazon.awscdk.services.lambda.CfnPermission
import software.amazon.awscdk.services.lambda.CfnUrl
import software.amazon.awscdk.services.s3.CfnBucket
import software.amazon.awscdk.services.s3.CfnBucketPolicy
import software.constructs.Construct
import java.io.File
import java.util.*
import java.util.Map
import kotlin.collections.List
import kotlin.collections.forEach
import kotlin.collections.mutableListOf

class CDKHostTemplate(scope: Construct, id: String?, props: StackProps?,
                      domain: Domain, deployment: Deployment,
                      codeBucketName: String,
                      idToFunctionJars: kotlin.collections.Map<String, File>
)  : Stack(scope, id, props) {

    val websiteDomainName = "${deployment.subdomain}.${domain.name}"

    init {
        val functionRole = createFunctionRole()
        idToFunctionJars.entries.forEach {
            createFunction(it.key, codeBucketName, it.value.name, websiteDomainName, functionRole)
        }
        val websiteResourcesHostingBucket = createWebsiteResourcesHostingBucket()
        createWebsiteResourcesHostingBucketPolicy(websiteResourcesHostingBucket)
        //TODO the cloudfront distribution
}

    fun createFunction(id: String, codeBucketName: String, jarFileName: String, staticWebsiteBucketName: String, functionRole: CfnRole) {
        val function: CfnFunction =
            CfnFunction.Builder.create(this, "${id}Function")
                .code(
                    CfnFunction.CodeProperty.builder()
                        .s3Bucket(codeBucketName)
                        .s3Key(jarFileName)
                        .build()
                )
                .environment(
                    CfnFunction.EnvironmentProperty.builder()
                        .variables(Map.of("s3bucket", staticWebsiteBucketName))
                        .build()
                )
                .handler("org.testedsoftware.paramicons.${id}Handler")
                .memorySize(1024)
                .role(functionRole.getAttrArn())
                .runtime("java17")
                .timeout(30)
                .build()

        val functionPermission: CfnPermission =
            CfnPermission.Builder.create(this, "${id}FunctionPermission")
                .action("lambda:InvokeFunctionUrl")
                .functionName(function.getRef())
                .functionUrlAuthType("NONE")
                .principal("*")
                .build()

        val functionLambdaUrl: CfnUrl = CfnUrl.Builder.create(this, "thumbnailerLambdaUrl")
            .authType("NONE")
            .targetFunctionArn(function.getRef())
            .build()
    }

    fun createFunctionRole() : CfnRole  =
            CfnRole.Builder.create(this, "functionRole")
                .assumeRolePolicyDocument(
                    Map.of(
                        "Statement", Arrays.asList(
                            Map.of(
                                "Action", mutableListOf(
                                    "sts:AssumeRole"
                                ),
                                "Effect", "Allow",
                                "Principal", Map.of<String, List<String>>(
                                    "Service", mutableListOf(
                                        "edgelambda.amazonaws.com",
                                        "lambda.amazonaws.com"
                                    )
                                )
                            )
                        ),
                        "Version", "2012-10-17"
                    )
                )
                .managedPolicyArns(
                    mutableListOf(
                        "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
                    )
                )
                .policies(
                    Arrays.asList(
                        CfnRole.PolicyProperty.builder()
                            .policyDocument(
                                Map.of(
                                    "Statement", Arrays.asList(
                                        Map.of(
                                            "Action", mutableListOf(
                                                "s3:PutObject",
                                                "s3:*"
                                            ),
                                            "Effect", "Allow",
                                            "Resource", mutableListOf(
                                                "arn:aws:s3:::${websiteDomainName}/*"
                                            )
                                        )
                                    ),
                                    "Version", "2012-10-17"
                                )
                            )
                            .policyName("s3Policy")
                            .build()
                    )
                )
                .build()


    fun createWebsiteResourcesHostingBucket(): CfnBucket =
        CfnBucket.Builder.create(this, "websiteResourcesHostingBucket")
            .bucketName(websiteDomainName)
            .publicAccessBlockConfiguration(
                CfnBucket.PublicAccessBlockConfigurationProperty.builder()
                    .blockPublicPolicy(false)
                    .build()
            )
            .websiteConfiguration(
                CfnBucket.WebsiteConfigurationProperty.builder()
                    .errorDocument("index.html")
                    .indexDocument("index.html")
                    .build()
            )
            .build()

    fun createWebsiteResourcesHostingBucketPolicy(websiteResourcesHostingBucket: CfnBucket) =
        CfnBucketPolicy.Builder.create(this, "websiteResourcesHostingBucketPolicy")
            .bucket(websiteResourcesHostingBucket.getRef())
            .policyDocument(
                Map.of<String, Any>(
                    "Statement", Arrays.asList<kotlin.collections.Map<String, Any>>(
                        Map.of<String, Any>(
                            "Action", mutableListOf<String>(
                                "s3:GetObject"
                            ),
                            "Effect", "Allow",
                            "Principal", Map.of<String, List<String>>(
                                "AWS", mutableListOf<String>(
                                    "*"
                                )
                            ),
                            "Resource", Arrays.asList<String>(
                                java.lang.String.join(
                                    "",
                                    "arn:aws:s3:::",
                                    websiteResourcesHostingBucket.getRef(),
                                    "/*"
                                )
                            )
                        )
                    ),
                    "Version", "2012-10-17"
                )
            )
            .build()

}
