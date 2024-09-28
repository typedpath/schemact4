
import WildcardCertificateTemplate.Companion.getCertArn
import schemact.gradleplugin.aws.cdk.DeployCdkStack.deployCdkStack
import software.amazon.awscdk.CfnOutput
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.certificatemanager.Certificate
import software.constructs.Construct

fun main() {
    val domain = "typedpath.com"
    println(getCertArn(domain))
}

class WildcardCertificateTemplate(scope: Construct, domain: String) : Stack(scope) {
    companion object {
        val certificateArnKey = "certificateArn"
        fun getCertArn(domain: String) : String {
            val outputs = deployCdkStack("${domain.replace('.', '-')}-certificate") {
                WildcardCertificateTemplate(it, domain)
            }
            return outputs.find { it.outputKey==certificateArnKey }?.outputValue?:throw RuntimeException("cert arn not found")
        }
    }

    init {
         val cert = Certificate.Builder.create(this, "${domain}_cert")
             .certificateName(domain)
             .domainName("*.${domain}")
             .subjectAlternativeNames(listOf(domain)).build()
         CfnOutput.Builder.create(this, certificateArnKey).key(certificateArnKey)
             .value(cert.certificateArn).build()
    }
}
