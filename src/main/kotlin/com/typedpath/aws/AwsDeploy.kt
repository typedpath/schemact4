package com.typedpath.aws

import com.amazonaws.regions.Regions
import schemact.domain.Schemact
import java.io.File

fun deployCode(schemact : Schemact,
               deploymentName: String,
               thumbnailerCodeJar: File
               /**TODO make this a list of function mappings*/) {

    deployCode(asStackParams(schemact, deploymentName, thumbnailerCodeJar))
}

fun deployInfrastructure(schemact : Schemact,
               deploymentName: String,
               thumbnailerCodeJar: File
               /**TODO make this a list of function mappings*/) {

    deployInfrastructure(asStackParams(schemact, deploymentName, thumbnailerCodeJar))
}

fun deployUiCode(schemact : Schemact, deploymentName : String, uiCodeLocation: String) {
    deployUiCode(asStackParams(schemact, deploymentName, File("TODO")), uiCodeLocation)
}

fun asStackParams(schemact : Schemact,
                  deploymentName: String,
                  thumbnailerCodeJar: File) : StackParams {
    if (schemact.domains.size!=1) {
        throw RuntimeException("exactly 1 domain only expected")
    }
    val domain = schemact.domains[0]
    val deployment = domain.deployments.find { deploymentName.equals(it.subdomain) }
    if (deployment==null) {
        throw RuntimeException("unknown deployment $deploymentName")
    }

    //TODO unhardcode these
    return StackParams(region = Regions.US_EAST_1,
        name = deploymentName,
        rootDomain = domain.name,
        wildCardSslCertArn = domain.wildcardCertificateRef,
        cloudFrontHostedZoneId = domain.cdnZoneReference,
        // TODO change this to a list of functions
        codeJar = thumbnailerCodeJar
    )

}