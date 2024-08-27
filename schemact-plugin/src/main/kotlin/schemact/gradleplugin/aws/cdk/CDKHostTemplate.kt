package schemact.gradleplugin.aws.cdk;
import CodeLocations.handlerFullClassName
import schemact.domain.*
import schemact.domain.Function
import software.amazon.awscdk.Stack
import software.amazon.awscdk.StackProps
import software.amazon.awscdk.services.cloudfront.CfnDistribution
import software.amazon.awscdk.services.iam.CfnRole
import software.amazon.awscdk.services.lambda.CfnFunction
import software.amazon.awscdk.services.lambda.CfnPermission
import software.amazon.awscdk.services.lambda.CfnUrl
import software.amazon.awscdk.services.route53.CfnRecordSetGroup
import software.amazon.awscdk.services.s3.CfnBucket
import software.amazon.awscdk.services.s3.CfnBucketPolicy
import software.constructs.Construct
import java.io.File
import java.util.*
import kotlin.collections.List
import kotlin.collections.mutableListOf

class CDKHostTemplate(scope: Construct, id: String?, props: StackProps?,
                      schemact: Schemact,
                      domain: Domain, deployment: Deployment,
                      codeBucketName: String,
                      functionToFunctionJars: Map<Function, File>
)  : Stack(scope, id, props) {

    val websiteDomainName = "${deployment.subdomain}.${domain.name}"

    init {
        val functionRole = createFunctionRole()
        val idToFunctionUrl: Map<String, CfnUrl> =  functionToFunctionJars.entries.associate {
            it.key.name to
            createFunction(id = it.key.name, function = it.key, domain = domain, schemact = schemact, codeBucketName = codeBucketName, jarFileName =  it.value.name, staticWebsiteBucketName = websiteDomainName, functionRole = functionRole)
        }
        val websiteResourcesHostingBucket = createWebsiteResourcesHostingBucket()
        createWebsiteResourcesHostingBucketPolicy(websiteResourcesHostingBucket)
        val cfnDistribution = createWebsiteResourcesCloudFrontDistribution(scope = this, domain=domain, websiteDomainName=websiteDomainName, idToFunctionUrl=idToFunctionUrl)
        createWebsiteResourcesDnsRecordSetGroup(websiteDomainName = websiteDomainName, domain=domain, cloudFrontDistribution = cfnDistribution)
    }

    fun environmentVariables(function: Function, bucketName: String) : Map<String, String> {
        return function.paramType.fieldsFromInfrastructure().map {
            if (it.entity2 is StaticWebsite.BucketName) it.name to bucketName
            else throw RuntimeException("unknown infrastructure field type ${it.entity2.name} in ${it.name}.${it}")
        }.associateBy({it.first}, {it.second})
    }

    fun createFunction(id: String, function: Function, domain: Domain, schemact: Schemact, codeBucketName: String, jarFileName: String, staticWebsiteBucketName: String,
                       functionRole: CfnRole) : CfnUrl {
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
                        .variables(environmentVariables(function, staticWebsiteBucketName))
                        .build()
                )
                .handler("${handlerFullClassName(schemact = schemact, domain=domain, id=id)}")
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

        val functionLambdaUrl: CfnUrl = CfnUrl.Builder.create(this, "${id}LambdaUrl")
            .authType("NONE")
            .targetFunctionArn(function.getRef())
            .build()
        return functionLambdaUrl
    }

    fun createFunctionRole() : CfnRole  =
            CfnRole.Builder.create(this, "functionRole")
                .assumeRolePolicyDocument(
                    java.util.Map.of(
                        "Statement", mutableListOf(
                            java.util.Map.of(
                                "Action", mutableListOf(
                                    "sts:AssumeRole"
                                ),
                                "Effect", "Allow",
                                "Principal", java.util.Map.of<String, List<String>>(
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
                    mutableListOf(
                        CfnRole.PolicyProperty.builder()
                            .policyDocument(
                                java.util.Map.of(
                                    "Statement", mutableListOf(
                                        java.util.Map.of(
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
                java.util.Map.of<String, Any>(
                    "Statement", mutableListOf<kotlin.collections.Map<String, Any>>(
                        java.util.Map.of<String, Any>(
                            "Action", mutableListOf<String>(
                                "s3:GetObject"
                            ),
                            "Effect", "Allow",
                            "Principal", java.util.Map.of<String, List<String>>(
                                "AWS", mutableListOf<String>(
                                    "*"
                                )
                            ),
                            "Resource", mutableListOf<String>(
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

    fun createWebsiteResourcesDnsRecordSetGroup(websiteDomainName: String, domain:Domain, cloudFrontDistribution: CfnDistribution) =
        CfnRecordSetGroup.Builder.create(this, "websiteResourcesDnsRecordSetGroup")
            .comment("DNS records associated with ${websiteDomainName}. static site")
            .hostedZoneName("${domain.name}.")
            .recordSets(
                Arrays.asList<CfnRecordSetGroup.RecordSetProperty>(
                    CfnRecordSetGroup.RecordSetProperty.builder()
                        .aliasTarget(
                            CfnRecordSetGroup.AliasTargetProperty.builder()
                                .dnsName(cloudFrontDistribution.getAttrDomainName())
                                .hostedZoneId(domain.cdnZoneReference)
                                .build()
                        )
                        .name(websiteDomainName)
                        .type("A")
                        .build(),
                    CfnRecordSetGroup.RecordSetProperty.builder()
                        .aliasTarget(
                            CfnRecordSetGroup.AliasTargetProperty.builder()
                                .dnsName(cloudFrontDistribution.getAttrDomainName())
                                .hostedZoneId(domain.cdnZoneReference)
                                .build()
                        )
                        .name(websiteDomainName)
                        .type("AAAA")
                        .build()
                )
            )
            .build()


}
