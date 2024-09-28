package schemact.gradleplugin.aws.cdk

import software.amazon.awscdk.RemovalPolicy
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.s3.Bucket
import software.amazon.awscdk.services.iam.Role

import software.constructs.Construct
class CDKPublicCodeHostTemplate(scope: Construct, bucketName: String) : Stack(scope) {
init {
   // Bucket.Builder.create( this, bucketName)
    //    .removalPolicy(RemovalPolicy.DESTROY)
     //   .bucketName(bucketName).build()
    // TODO A records + permissions
    // might as well use CDKHostTemplate


}

}