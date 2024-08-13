package com.myorg;

import software.constructs.Construct;

import java.util.*;
import software.amazon.awscdk.CfnMapping;
import software.amazon.awscdk.CfnTag;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;

import software.amazon.awscdk.*;
import software.amazon.awscdk.services.cloudfront.*;
import software.amazon.awscdk.services.iam.*;
import software.amazon.awscdk.services.lambda.*;
import software.amazon.awscdk.services.route53.*;
import software.amazon.awscdk.services.s3.*;

class ParamiconsTestedSoftwareOrgStack extends Stack {
    private Object functionUrl;

    private Object functionUrlDomainOnly;

    public Object getFunctionUrl() {
        return this.functionUrl;
    }

    public Object getFunctionUrlDomainOnly() {
        return this.functionUrlDomainOnly;
    }

    public ParamiconsTestedSoftwareOrgStack(final Construct scope, final String id) {
        super(scope, id, null);
    }

    public ParamiconsTestedSoftwareOrgStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        this.addTransform("AWS::Serverless-2016-10-31");

        CfnRole thumbnailerFunctionFunctionRole = CfnRole.Builder.create(this, "thumbnailerFunctionFunctionRole")
                .assumeRolePolicyDocument(Map.of("Statement", Arrays.asList(
                        Map.of("Action", Arrays.asList(
                                "sts:AssumeRole"),
                        "Effect", "Allow",
                        "Principal", Map.of("Service", Arrays.asList(
                                "edgelambda.amazonaws.com",
                                "lambda.amazonaws.com")))),
                "Version", "2012-10-17"))
                .managedPolicyArns(Arrays.asList(
                        "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"))
                .policies(Arrays.asList(
                        CfnRole.PolicyProperty.builder()
                                .policyDocument(Map.of("Statement", Arrays.asList(
                                        Map.of("Action", Arrays.asList(
                                                "s3:PutObject",
                                                "s3:*"),
                                        "Effect", "Allow",
                                        "Resource", Arrays.asList(
                                                "arn:aws:s3:::paramicons.testedsoftware.org/*"))),
                                "Version", "2012-10-17"))
                                .policyName("s3Policy")
                                .build()))
                .build();

        CfnBucket websiteResourcesHostingBucket = CfnBucket.Builder.create(this, "websiteResourcesHostingBucket")
                .bucketName("paramicons.testedsoftware.org")
                .publicAccessBlockConfiguration(CfnBucket.PublicAccessBlockConfigurationProperty.builder()
                        .blockPublicPolicy(false)
                        .build())
                .websiteConfiguration(CfnBucket.WebsiteConfigurationProperty.builder()
                        .errorDocument("index.html")
                        .indexDocument("index.html")
                        .build())
                .build();

        CfnFunction svgThumbnailerFunction = CfnFunction.Builder.create(this, "svgThumbnailerFunction")
                .code(CfnFunction.CodeProperty.builder()
                        .s3Bucket("code.paramicons.testedsoftware.org")
                        .s3Key("svgthumbnailer-1.0.13-fat.jar")
                        .build())
                .environment(CfnFunction.EnvironmentProperty.builder()
                        .variables(Map.of("s3bucket", "paramicons.testedsoftware.org"))
                        .build())
                .handler("org.testedsoftware.paramicons.SvgThumbnailHandler")
                .memorySize(1024)
                .role(thumbnailerFunctionFunctionRole.getAttrArn())
                .runtime("java17")
                .timeout(30)
                .build();

        CfnBucketPolicy websiteResourcesHostingBucketPolicy = CfnBucketPolicy.Builder.create(this, "websiteResourcesHostingBucketPolicy")
                .bucket(websiteResourcesHostingBucket.getRef())
                .policyDocument(Map.of("Statement", Arrays.asList(
                        Map.of("Action", Arrays.asList(
                                "s3:GetObject"),
                        "Effect", "Allow",
                        "Principal", Map.of("AWS", Arrays.asList(
                                "*")),
                        "Resource", Arrays.asList(
                                String.join("",
                                        "arn:aws:s3:::",
                                        websiteResourcesHostingBucket.getRef(),
                                        "/*")))),
                "Version", "2012-10-17"))
                .build();

        CfnPermission thumbnailerFunctionPermission = CfnPermission.Builder.create(this, "thumbnailerFunctionPermission")
                .action("lambda:InvokeFunctionUrl")
                .functionName(svgThumbnailerFunction.getRef())
                .functionUrlAuthType("NONE")
                .principal("*")
                .build();

        CfnUrl thumbnailerLambdaUrl = CfnUrl.Builder.create(this, "thumbnailerLambdaUrl")
                .authType("NONE")
                .targetFunctionArn(svgThumbnailerFunction.getRef())
                .build();

        CfnDistribution websiteResourcesCloudFrontDistribution = CfnDistribution.Builder.create(this, "websiteResourcesCloudFrontDistribution")
                .distributionConfig(CfnDistribution.DistributionConfigProperty.builder()
                        .aliases(Arrays.asList(
                                "paramicons.testedsoftware.org"))
                        .cacheBehaviors(Arrays.asList(
                                CfnDistribution.CacheBehaviorProperty.builder()
                                        .allowedMethods(Arrays.asList(
                                                "GET",
                                                "HEAD",
                                                "OPTIONS",
                                                "PUT",
                                                "PATCH",
                                                "POST",
                                                "DELETE"))
                                        .compress(true)
                                        .defaultTtl(0)
                                        .forwardedValues(CfnDistribution.ForwardedValuesProperty.builder()
                                                .queryString(true)
                                                .build())
                                        .minTtl(0)
                                        .pathPattern("/share/*")
                                        .targetOriginId("webcrawler")
                                        .viewerProtocolPolicy("allow-all")
                                        .build()))
                        .defaultCacheBehavior(CfnDistribution.DefaultCacheBehaviorProperty.builder()
                                .allowedMethods(Arrays.asList(
                                        "GET",
                                        "HEAD"))
                                .compress(true)
                                .defaultTtl(0)
                                .forwardedValues(CfnDistribution.ForwardedValuesProperty.builder()
                                        .queryString(true)
                                        .build())
                                .minTtl(0)
                                .targetOriginId("S3Origin")
                                .viewerProtocolPolicy("allow-all")
                                .build())
                        .defaultRootObject("index.html")
                        .enabled(true)
                        .httpVersion("http2")
                        .origins(Arrays.asList(
                                CfnDistribution.OriginProperty.builder()
                                        .customOriginConfig(CfnDistribution.CustomOriginConfigProperty.builder()
                                                .httpPort(80)
                                                .httpsPort(443)
                                                .originProtocolPolicy("match-viewer")
                                                .build())
                                        .domainName(Fn.select(2, Fn.split(/, thumbnailerLambdaUrl.getAttrFunctionUrl())))
                                        .id("webcrawler")
                                        .build(),
                                CfnDistribution.OriginProperty.builder()
                                        .customOriginConfig(CfnDistribution.CustomOriginConfigProperty.builder()
                                                .httpPort(80)
                                                .httpsPort(443)
                                                .originProtocolPolicy("http-only")
                                                .build())
                                        .domainName(String.join("",
                                                websiteResourcesHostingBucket.getRef(),
                                                ".s3-website-us-east-1.amazonaws.com"))
                                        .id("S3Origin")
                                        .build()))
                        .viewerCertificate(CfnDistribution.ViewerCertificateProperty.builder()
                                .acmCertificateArn("arn:aws:acm:us-east-1:950651224730:certificate/78fab14f-b918-42cd-bb4f-2bea3153d252")
                                .sslSupportMethod("sni-only")
                                .build())
                        .build())
                .build();

        CfnRecordSetGroup websiteResourcesDnsRecordSetGroup = CfnRecordSetGroup.Builder.create(this, "websiteResourcesDnsRecordSetGroup")
                .comment("DNS records associated with testedsoftware.org. static site")
                .hostedZoneName("testedsoftware.org.")
                .recordSets(Arrays.asList(
                        CfnRecordSetGroup.RecordSetProperty.builder()
                                .aliasTarget(CfnRecordSetGroup.AliasTargetProperty.builder()
                                        .dnsName(websiteResourcesCloudFrontDistribution.getAttrDomainName())
                                        .hostedZoneId("Z2FDTNDATAQYW2")
                                        .build())
                                .name("paramicons.testedsoftware.org")
                                .type("A")
                                .build(),
                        CfnRecordSetGroup.RecordSetProperty.builder()
                                .aliasTarget(CfnRecordSetGroup.AliasTargetProperty.builder()
                                        .dnsName(websiteResourcesCloudFrontDistribution.getAttrDomainName())
                                        .hostedZoneId("Z2FDTNDATAQYW2")
                                        .build())
                                .name("paramicons.testedsoftware.org")
                                .type("AAAA")
                                .build()))
                .build();

        this.functionUrl = thumbnailerLambdaUrl.getAttrFunctionUrl();
        CfnOutput.Builder.create(this, "CfnOutputFunctionUrl")
                .key("FunctionUrl")
                .value(this.functionUrl.toString())
                .description("API endpoint URL for Prod environment")
                .build();

        this.functionUrlDomainOnly = Fn.select(2, Fn.split(/, thumbnailerLambdaUrl.getAttrFunctionUrl()));
        CfnOutput.Builder.create(this, "CfnOutputFunctionUrlDomainOnly")
                .key("FunctionUrlDomainOnly")
                .value(this.functionUrlDomainOnly.toString())
                .description("API endpoint URL for Prod environment")
                .build();

    }
}
