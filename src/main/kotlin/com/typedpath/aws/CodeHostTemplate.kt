package com.typedpath.aws

import com.typedpath.awscloudformation.schema.AWS_S3_Bucket
import com.typedpath.awscloudformation.serverlessschema.ServerlessCloudformationTemplate

class CodeHostTemplate(params: StackParams) : ServerlessCloudformationTemplate() {

    val functionCodeBucket = AWS_S3_Bucket {
        bucketName = params.functionCodeBucketName()
    }
}