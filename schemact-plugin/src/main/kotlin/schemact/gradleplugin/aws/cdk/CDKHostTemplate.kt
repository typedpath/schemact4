package schemact.gradleplugin.aws.cdk
import PrivateBucketCdk
import schemact.domain.*
import schemact.domain.Function
import schemact.gradleplugin.FunctionIdKey
import schemact.gradleplugin.RestPolicy
import schemact.gradleplugin.aws.cdk.CreateWebsiteResourcesCloudFrontDistribution.createWebsiteResourcesCloudFrontDistribution
import schemact.gradleplugin.aws.functiontemplates.CodeLocations.handlerFullClassName
import schemact.gradleplugin.functionId
import software.amazon.awscdk.Stack
import software.amazon.awscdk.StackProps
import software.amazon.awscdk.services.cloudfront.CfnDistribution
import software.amazon.awscdk.services.dynamodb.Table
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

// rename to cloudfronthosttemplate ?
class CDKHostTemplate(scope: Construct, id: String?, props: StackProps?,
                      val schemact: Schemact,
                      domain: Domain, deployment: Deployment,
                      codeBucketName: String,
                      functionToFunctionJars: Map<Function, File>
)  : Stack(scope, id, props) {


  val websiteDomainName = "${deployment.subdomain}.${domain.name}"
  var privateBucketName: String? = null

    init {
        val entityToEnvironmentVariable =mutableMapOf<Entity, String>()
        val userTableName = "${websiteDomainName}-users"
        var userTable: Table? = null
        if (schemact.userKeyedDatabase != null)  {
            entityToEnvironmentVariable[InfrastructureInjectables.DynamoDBTablenameType]=userTableName
            // tableName org.testedsoftware.sample-users" is valid
// id e.g. SampleUsers
            userTable = UserTableStack.userTableStack(
                scope = this,
                id = "${deployment.subdomain}Users",
                tableName = userTableName,
                deleteWithStack = false
            )
        }
        schemact.privateBucket?.let {
            privateBucketName= "${websiteDomainName}-private"
            PrivateBucketCdk.create(this, privateBucketName!!)
            entityToEnvironmentVariable[InfrastructureInjectables.PrivateBucketNameType]=privateBucketName!!
        }

        val websiteResourcesHostingBucket = createWebsiteResourcesHostingBucket()
        entityToEnvironmentVariable[InfrastructureInjectables.BucketNameType]=websiteResourcesHostingBucket.bucketName!!
        if (schemact.auth !=null) {
            val cognitoDetails = CDKCognitoStack.cognitoStack(this, this, this.websiteDomainName)
            entityToEnvironmentVariable[InfrastructureInjectables.CognitoClientDetails.entity]=cognitoDetails.writeAsJsonString()
        }
        val functionRole = CDKFunctionRoleTemplate.createFunctionRole(scope = this, websiteDomainName=websiteDomainName,
            usersTable=userTable, privateBucketName=privateBucketName)
        val idToFunctionUrl: Map<String, CfnUrl> =  functionToFunctionJars.entries.associate {
            functionId(schemact.findModule(it.key), it.key) to
            createFunction(id = it.key.name, function = it.key, module=schemact.findModule(it.key),
                domain = domain, schemact = schemact, codeBucketName = codeBucketName, jarFileName =  it.value.name, entityToEnvironmentVariable = entityToEnvironmentVariable, functionRole = functionRole)
        }


        createWebsiteResourcesHostingBucketPolicy(websiteResourcesHostingBucket)
        val cfnDistribution = createWebsiteResourcesCloudFrontDistribution(scope = this, domain=domain, websiteDomainName=websiteDomainName, idToFunctionUrl=idToFunctionUrl)
        createWebsiteResourcesDnsRecordSetGroup(websiteDomainName = websiteDomainName, domain=domain, cloudFrontDistribution = cfnDistribution)
    }


    fun environmentVariables(module: Module, function: Function, entityToEnvironmentVariable: Map<Entity, String>) : Map<String, String> {
        println("environmentVariables ${entityToEnvironmentVariable.entries.joinToString { "${it.key.name}=${it.value}"  }}" )
        val result =  RestPolicy(function.paramType, function.returnType).argsFromEnvironment.map {
            if (entityToEnvironmentVariable.containsKey(it.entity2)) it.name to (entityToEnvironmentVariable[it.entity2])!!
            else throw RuntimeException("unknown infrastructure field type ${it.entity2.name} in function ${function.name}.${it.entity1.name}.${it.name}")
        }.associateBy({it.first}, {it.second}).toMutableMap()
        result[FunctionIdKey] = functionId(module, function)
        return result
    }

    fun functionRuntime(module: Module) =
        when (module.type) {
            Module.Type.StandaloneFunction -> "java17"
            Module.Type.GoStandaloneFunction -> "provided.al2023"
            else -> throw RuntimeException("unsupported module type ${module.type.name}")
        }

    fun createFunction(id: String, function: Function, module: Module, domain: Domain, schemact: Schemact,
                       codeBucketName: String, jarFileName: String, entityToEnvironmentVariable: Map<Entity, String>,
                       functionRole: CfnRole) : CfnUrl {
        val cfnFunction: CfnFunction =
            CfnFunction.Builder.create(this, "${id}Function")
                .code(
                    CfnFunction.CodeProperty.builder()
                        .s3Bucket(codeBucketName)
                        .s3Key(jarFileName)
                        .build()
                )
                .environment(
                    CfnFunction.EnvironmentProperty.builder()
                        .variables(environmentVariables(module, function, entityToEnvironmentVariable))
                        .build()
                )
                .handler(handlerFullClassName(schemact = schemact, module=module, domain=domain, id=id))
                .memorySize(1024)
                .role(functionRole.getAttrArn())
                .runtime(functionRuntime(module))
                .architectures(listOf("arm64"))
                .timeout(30)
                .build()

        val functionPermission: CfnPermission =
            CfnPermission.Builder.create(this, "${id}FunctionPermission")
                .action("lambda:InvokeFunctionUrl")
                .functionName(cfnFunction.getRef())
                .functionUrlAuthType("NONE")
                .principal("*")
                .build()

        val functionLambdaUrl: CfnUrl = CfnUrl.Builder.create(this, "${id}LambdaUrl")
            .authType("NONE")
            .targetFunctionArn(cfnFunction.getRef())
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
                    "Statement", mutableListOf<Map<String, Any>>(
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
