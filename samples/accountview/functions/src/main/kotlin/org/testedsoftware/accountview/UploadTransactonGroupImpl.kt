
package org.testedsoftware.accountview
// created by template functionSampleImpl
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.ObjectMetadata
import schemact.aws.CognitoClientDetails
import schemact.aws.VerifyCognito.verifyCognitoJwt
import schemact.react.File
import java.io.ByteArrayInputStream
import java.time.LocalDateTime


class UploadTransactonGroupImpl { 
    // created from template  functionSampleImpl at 2025-05-12T15:57:02.558775500       
    fun uploadTransactonGroup(userTableName: String, privateBucketName: String, Authorization: String, cognitoDetails: CognitoClientDetails, file: File, fromInclusiveDate: String, toInclusiveDate: String, accountNumber: String) : UserInfo {
        val cognitoData = verifyCognitoJwt(Authorization, cognitoDetails)
        println(cognitoData)
        val userId = cognitoData.sub
        val email = cognitoData.email?:"unknown"


        val s3: AmazonS3 =
            AmazonS3ClientBuilder.standard()
                .withRegion(cognitoDetails.region)
                .build()

        // Upload to S3 with environment-specific prefix
        val path = "/transactionGroups/${file.filename}"
        val key = "$userId$path"
        val metadata = ObjectMetadata().apply {
            this.contentType = file.contentType
            this.contentLength = file.content.size.toLong()
        }
        val s3Bucket = privateBucketName
        s3.putObject(s3Bucket, key, ByteArrayInputStream(file.content), metadata)

        // Generate file URL
        val fileUrl = "https://$s3Bucket.s3.amazonaws.com/$key"

        return DynamoDbUtil.createOrUpdate(userTableName=userTableName,
            userId=userId, email=email, UserInfo::class.java,
            defaultData = {UserInfo(loginEvents=mutableListOf<String>())},
            update = { data ->
                    val account = data.accounts.find { it.accountNumber == accountNumber }
                        ?: throw IllegalArgumentException("Account with accountNumber $accountNumber not found")

                    val transactionGroup = Account.TransactionGroup(
                        fromInclusiveDate = fromInclusiveDate,
                        toInclusiveDate = toInclusiveDate,
                        rawTransactionFile = Account.TransactionGroup.File(
                            filename = file.filename,
                            location = path,
                            contentType = file.contentType,
                            uploadTime = LocalDateTime.now().toString()
                        )
                    )
                    // Add the new transaction group to the found account
                    account.transactionGroups.add(transactionGroup)
                    data
            })
    }



}
