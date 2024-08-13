package com.typedpath.aws

import com.amazonaws.regions.Regions
import java.io.File

data class StackParams(
    val region: Regions,
    val name: String,
    val rootDomain: String,
    val wildCardSslCertArn: String,
    val cloudFrontHostedZoneId: String,
    val idToCodeJar: Map<String, File>
) {
    fun functionCodeBucketName() = "code.${name}.${rootDomain}"
    fun functionCodeStackName() = "${name}Code"

    fun websiteDomainName() = "${name}.${rootDomain}"

}