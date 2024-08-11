package com.typedpath.aws

import com.typedpath.awscloudformation.CloudFormationTemplate
import com.typedpath.awscloudformation.schema.*
import com.typedpath.awscloudformation.serverlessschema.ServerlessCloudformationTemplate
import com.typedpath.iam2kotlin.IamPolicy
import com.typedpath.iam2kotlin.resources.s3.S3Action
import com.typedpath.iam2kotlin.resources.sts.StsAction

// TODO fix bug in awscloudformation forcing templates to be in package com.typedpath.**

class HostTemplate(params: StackParams) : ServerlessCloudformationTemplate() {

    val websiteDomainName = params.websiteDomainName()

    val thumbnailerFunctionFunctionRole = AWS_IAM_Role(assumeRolePolicyDocument = IamPolicy {
        statement {
            effect = IamPolicy.EffectType.Allow
            principal = mutableMapOf(
                IamPolicy.PrincipalType.Service to listOf(
                    "edgelambda.amazonaws.com",
                    "lambda.amazonaws.com"
                )
            )
            action(StsAction.AssumeRole)
        }
    }) {
        managedPolicyArns = listOf( "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole")
        policies = listOf(
            AWS_IAM_Role.Policy(policyName = "s3Policy", policyDocument = IamPolicy() {
                statement {
                    effect = IamPolicy.EffectType.Allow
                    action(S3Action.PutObject)
                    action(S3Action.All)
                    resource(IamPolicy.Resource("arn:aws:s3:::${websiteDomainName}/*"))
                }
            }) {

            }
        )
    }

    val svgThumbnailerFunction = AWS_Lambda_Function(
        code = AWS_Lambda_Function.Code {
            s3Bucket = params.functionCodeBucketName()
            s3Key = params.codeJar.name
        },
        role = ref(thumbnailerFunctionFunctionRole.arnAttribute())
    ) {
        handler = "org.testedsoftware.paramicons.SvgThumbnailHandler"
        runtime = "java17"
        timeout = 30
        memorySize = 1024
        environment = AWS_Lambda_Function.Environment() {
            variables = mapOf("s3bucket" to websiteDomainName)
        }
    }

    val thumbnailerLambdaUrl = AWS_Lambda_Url(
        authType = "NONE",
        targetFunctionArn = ref(svgThumbnailerFunction)
    ) { }

    val thumbnailerOrigin = AWS_CloudFront_Distribution.Origin(
        domainName = "!Select [2, !Split [\"/\", !GetAtt thumbnailerLambdaUrl.FunctionUrl ]]",
        id = "webcrawler") {
        customOriginConfig = AWS_CloudFront_Distribution.CustomOriginConfig("match-viewer") {
            hTTPPort = 80
            hTTPSPort = 443

        }
    }

    val thumbnailerFunctionPermission = AWS_Lambda_Permission(functionName = ref(svgThumbnailerFunction),
        action = "lambda:InvokeFunctionUrl",
        principal = "*"
    ) {
        functionUrlAuthType = "NONE"
    }


    val websiteResources: CloudFormationTemplate.ResourceGroup = createStaticWebsiteResources(
        template = this,
        websiteDomainName = websiteDomainName,
        sslCertArn = params.wildCardSslCertArn,
        deploymentFolder = null, domainRoot = params.rootDomain, region = params.region,
        cloudfrontHostedZoneId = params.cloudFrontHostedZoneId
        //,
        //lambdaFunctionAssociations =  listOf(webCrawlerFunctionAssociation)
        ,path2ExtraOrigins = mapOf( "/share/*" to OriginConfig(origin= thumbnailerOrigin,
            lambdaFunctionAssociations = emptyList()//listOf(webCrawlerFunctionAssociation)
            )
        )
    )

    val FunctionUrl = Output(this.ref(thumbnailerLambdaUrl.functionUrlAttribute())) {
        description = "API endpoint URL for Prod environment"
    }

    val FunctionUrlDomainOnly = Output(this.rawInstrinsicFunctionCall("!Select [2, !Split [\"/\", !GetAtt thumbnailerLambdaUrl.FunctionUrl ]]")) {
        description = "API endpoint URL for Prod environment"
    }

}


