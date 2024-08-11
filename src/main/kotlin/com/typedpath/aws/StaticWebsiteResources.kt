package com.typedpath.aws

import com.amazonaws.regions.Regions
import com.typedpath.awscloudformation.CloudFormationTemplate
import com.typedpath.awscloudformation.schema.*
import com.typedpath.iam2kotlin.IamPolicy
import com.typedpath.iam2kotlin.resources.s3.S3Action

/**
 * defines awscloudformation template defs for infrastructure for static website served from s3 with cert on cloudfront
 */
class StaticWebsiteResources (
    val hostingBucket: AWS_S3_Bucket, val hostingBucketPolicy: AWS_S3_BucketPolicy,
    val cloudFrontDistribution: AWS_CloudFront_Distribution,
    val dnsRecordSetGroup: AWS_Route53_RecordSetGroup
) : CloudFormationTemplate.ResourceGroup()

class OriginConfig(val origin: AWS_CloudFront_Distribution.Origin, val lambdaFunctionAssociations: List<AWS_CloudFront_Distribution.LambdaFunctionAssociation> = emptyList())

fun createStaticWebsiteResources(
    template: CloudFormationTemplate,
    websiteDomainName: String,
    sslCertArn: String,
    deploymentFolder: String?,
    domainRoot: String,
    region: Regions,
    cloudfrontHostedZoneId: String,
    lambdaFunctionAssociations: List<AWS_CloudFront_Distribution.LambdaFunctionAssociation> = emptyList(),
    path2ExtraOrigins: Map<String, OriginConfig> = emptyMap()
): StaticWebsiteResources {
    val hostingBucket = AWS_S3_Bucket {
        bucketName = websiteDomainName
        publicAccessBlockConfiguration = AWS_S3_Bucket.PublicAccessBlockConfiguration() {
            blockPublicPolicy=false
        }
        websiteConfiguration = AWS_S3_Bucket.WebsiteConfiguration {
            errorDocument = "index.html"
            indexDocument = "index.html"
        }
    }

    val hostingBucketPolicy = AWS_S3_BucketPolicy(template.ref(hostingBucket), IamPolicy {
        statement {
            effect = IamPolicy.EffectType.Allow
            principal = mutableMapOf(
                Pair(IamPolicy.PrincipalType.AWS, listOf("*"))
            )
            action(S3Action.GetObject)
            resource(
                IamPolicy.Resource(
                    template.join(
                        "",
                        listOf("arn:aws:s3:::", template.ref(hostingBucket), "/*")
                    )
                )
            )
        }
    })

    fun createOrigins(path2ExtraOrigins: Map<String, OriginConfig>): List<AWS_CloudFront_Distribution.Origin> {
        val origins = path2ExtraOrigins.map{ it.value.origin }.toMutableList()
        origins.add(
            // for variable regions             - DomainName: {"Fn::Join": ["", [{Ref: WebSite}, ".", {"Fn::FindInMap": [RegionMap, {Ref: "AWS::Region"}, websiteendpoint]}]]}
            AWS_CloudFront_Distribution.Origin(
                domainName = template.join(
                    "",
                    listOf(template.ref(hostingBucket), ".s3-website-${region.name.replace('_', '-').toLowerCase()}.amazonaws.com")
                ),
                id = "S3Origin"
            ) {
                customOriginConfig = AWS_CloudFront_Distribution.CustomOriginConfig("http-only") {
                    hTTPPort = 80
                    hTTPSPort = 443
                    originPath = if (deploymentFolder==null) null else "/$deploymentFolder"
                }
            })
        return origins
    }

    fun createCacheBehaviours(path2ExtraOrigins: Map<String, OriginConfig>) : List<AWS_CloudFront_Distribution.CacheBehavior>  =
         path2ExtraOrigins.entries.map {
             AWS_CloudFront_Distribution.CacheBehavior(targetOriginId = it.value.origin.id, pathPattern = it.key,
                  viewerProtocolPolicy ="allow-all" ) {
                 allowedMethods = listOf("GET", "HEAD", "OPTIONS", "PUT", "PATCH", "POST", "DELETE")
                 compress = true
                 defaultTTL = 0.0//30.0
                 minTTL = 0.0//10.0
                 forwardedValues = AWS_CloudFront_Distribution.ForwardedValues(queryString = true)
                 this@CacheBehavior.lambdaFunctionAssociations = it.value.lambdaFunctionAssociations
             }
         }

    val cloudFrontDistribution = AWS_CloudFront_Distribution(
        distributionConfig = AWS_CloudFront_Distribution.DistributionConfig(enabled = true,
            defaultCacheBehavior = AWS_CloudFront_Distribution.DefaultCacheBehavior(
                targetOriginId = "S3Origin",
                viewerProtocolPolicy = "allow-all",
            ) {
                allowedMethods = listOf("GET", "HEAD")
                compress = true
                defaultTTL = 0.0//30.0
                minTTL = 0.0//10.0
                this.lambdaFunctionAssociations = lambdaFunctionAssociations
                forwardedValues = AWS_CloudFront_Distribution.ForwardedValues(queryString = true)
            }) {
            viewerCertificate = AWS_CloudFront_Distribution.ViewerCertificate {
                acmCertificateArn = sslCertArn
                sslSupportMethod = "sni-only"
            }
            httpVersion = "http2"
            aliases = listOf(websiteDomainName)
            defaultRootObject = "index.html"
            origins = createOrigins(path2ExtraOrigins)
            cacheBehaviors = createCacheBehaviours(path2ExtraOrigins)
        }
    )

    val dnsRecordSetGroup = AWS_Route53_RecordSetGroup {
        hostedZoneName = "$domainRoot."
        comment = "DNS records associated with $hostedZoneName static site"
        recordSets = listOf(
            AWS_Route53_RecordSetGroup.RecordSet(
                name = websiteDomainName,
                type = "A"
            ) {
                aliasTarget = AWS_Route53_RecordSetGroup.AliasTarget(
                    dNSName = template.ref(cloudFrontDistribution.domainNameAttribute()),
                    hostedZoneId = cloudfrontHostedZoneId
                )
            },
            AWS_Route53_RecordSetGroup.RecordSet(
                name = websiteDomainName,
                type = "AAAA"
            ) {
                aliasTarget = AWS_Route53_RecordSetGroup.AliasTarget(
                    dNSName = template.ref(cloudFrontDistribution.domainNameAttribute()),
                    hostedZoneId = cloudfrontHostedZoneId
                )
            }
        )
    }

    return StaticWebsiteResources(
            hostingBucket,
            hostingBucketPolicy,
            cloudFrontDistribution,
            dnsRecordSetGroup
    )


}