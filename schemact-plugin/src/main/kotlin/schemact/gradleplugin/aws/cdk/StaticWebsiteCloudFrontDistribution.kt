package schemact.gradleplugin.aws.cdk;

import schemact.domain.Domain
import software.amazon.awscdk.Fn
import software.amazon.awscdk.services.cloudfront.CfnDistribution
import software.amazon.awscdk.services.lambda.CfnUrl
import software.amazon.awscdk.services.route53.CfnRecordSetGroup
import software.amazon.awscdk.services.route53.CfnRecordSetGroup.RecordSetProperty
import software.constructs.Construct
import java.util.*

fun createWebsiteResourcesCloudFrontDistribution(scope: Construct, domain: Domain, websiteDomainName: String,
                                                 idToFunctionUrl: kotlin.collections.Map<String, CfnUrl>
): CfnDistribution {
    val originConfigProperties = mutableListOf(createDefaultOriginProperty(websiteDomainName))
    originConfigProperties.addAll(idToFunctionUrl.map { createFunctionOriginProperty(it.key, it.value) })
    return CfnDistribution.Builder.create(scope, "websiteResourcesCloudFrontDistribution")
        .distributionConfig(
            CfnDistribution.DistributionConfigProperty.builder()
                .aliases(
                    mutableListOf(websiteDomainName)
                )
                .cacheBehaviors(
                       idToFunctionUrl.keys.map { createFunctionCacheBehaviour(it) }
                )
                .defaultCacheBehavior(
                    CfnDistribution.DefaultCacheBehaviorProperty.builder()
                        .allowedMethods(
                            mutableListOf(
                                "GET",
                                "HEAD"
                            )
                        )
                        .compress(true)
                        .defaultTtl(0)
                        .forwardedValues(
                            CfnDistribution.ForwardedValuesProperty.builder()
                                .queryString(true)
                                .build()
                        )
                        .minTtl(0)
                        .targetOriginId("S3Origin")
                        .viewerProtocolPolicy("allow-all")
                        .build()
                )
                .defaultRootObject("index.html")
                .enabled(true)
                .httpVersion("http2")
                .origins(originConfigProperties)
                .viewerCertificate(
                    CfnDistribution.ViewerCertificateProperty.builder()
                        .acmCertificateArn(domain.wildcardCertificateRef)
                        .sslSupportMethod("sni-only")
                        .build()
                )
                .build()
        )
        .build()
}

fun createFunctionOriginProperty(id: String, functionUrl: CfnUrl) :  CfnDistribution.OriginProperty {
    return CfnDistribution.OriginProperty.builder()
        .customOriginConfig(
            CfnDistribution.CustomOriginConfigProperty.builder()
                .httpPort(80)
                .httpsPort(443)
                .originProtocolPolicy("match-viewer")
                .build()
        )
        .domainName(Fn.select(2, Fn.split("/", functionUrl.getAttrFunctionUrl())))
    .id(id)
    .build()
}

fun createDefaultOriginProperty(domainName: String): CfnDistribution.OriginProperty =
    CfnDistribution.OriginProperty.builder()
        .customOriginConfig(
            CfnDistribution.CustomOriginConfigProperty.builder()
                .httpPort(80)
                .httpsPort(443)
                .originProtocolPolicy("http-only")
                .build()
        )
        .domainName(
            java.lang.String.join(
                "",
                domainName,
                ".s3-website-us-east-1.amazonaws.com" //TODO cater for non east1
            )
        )
        .id("S3Origin")
        .build()

fun createFunctionCacheBehaviour(id: String)  =  CfnDistribution.CacheBehaviorProperty.builder()
    .allowedMethods(
        mutableListOf(
            "GET",
            "HEAD",
            "OPTIONS",
            "PUT",
            "PATCH",
            "POST",
            "DELETE"
        )
    )
    .compress(true)
    .defaultTtl(0)
    .forwardedValues(
        CfnDistribution.ForwardedValuesProperty.builder()
            .queryString(true)
            .build()
    )
    .minTtl(0)
    .pathPattern("/functions/$id")
    .targetOriginId(id)
    .viewerProtocolPolicy("allow-all")
    .build()

