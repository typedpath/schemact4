
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


class UploadTransactionGroupImpl {
    // created from template  functionSampleImpl at 2025-05-12T15:57:02.558775500       
    fun uploadTransactionGroup(userTableName: String, privateBucketName: String, Authorization: String, cognitoDetails: CognitoClientDetails, file: File, fromInclusiveDate: String, toInclusiveDate: String, accountNumber: String) : UserInfo {
        val cognitoData = verifyCognitoJwt(Authorization, cognitoDetails)
        println(cognitoData)
        val userId = cognitoData.sub
        val email = cognitoData.email?:"unknown"
        // TODO switch to use Transaction Group extension function

        val s3: AmazonS3 =
            AmazonS3ClientBuilder.standard()
                .withRegion(cognitoDetails.region)
                .build()

        val groupName = file.filename.substring(0, file.filename.lastIndexOf("."))

        // Upload to S3 with environment-specific prefix
        val location = "/transactionGroups/${groupName}"
        val rawKey = "$userId$location/${file.filename}"
        val metadata = ObjectMetadata().apply {
            this.contentType = file.contentType
            this.contentLength = file.content.size.toLong()
        }
        val s3Bucket = privateBucketName
        s3.putObject(s3Bucket, rawKey, ByteArrayInputStream(file.content), metadata)

        val transactions = RawTransactionFileIO.readBarclaysCsvContent(String(file.content))
        val transactionFileName = "$groupName.json"

        val rawTransactionFile = TransactionGroup.RawTransactionFile(
            filename = file.filename,
            location = location,
            contentType = file.contentType,
            uploadTime = LocalDateTime.now().toString()
        )

        val transactionFile= TransactionGroup.TransactionFile(filename=transactionFileName,
            location = location, contentType = "application/json",
            uploadTime = LocalDateTime.now().toString() )

        println("writing json to (s3Bucket, transactionFileKey)- ($s3Bucket, ${transactionFile.keyFromUserId(userId)})")
        RawTransactionFileIO.writeJson(s3Bucket, transactionFile.keyFromUserId(userId),  transactions)


        return UserInfoUpdater.update(userTableName=userTableName,  userId = userId,
            email = email,
            update= { data ->
                val account = data.accounts.find { it.accountNumber == accountNumber }
                    ?: throw IllegalArgumentException("Account with accountNumber $accountNumber not found")

                val transactionGroup = TransactionGroup(
                    fromInclusiveDate = fromInclusiveDate,
                    toInclusiveDate = toInclusiveDate,
                    rawTransactionFile = rawTransactionFile, transactionFile=transactionFile,
                )
                // Add the new transaction group to the found account
                account.transactionGroups.add(transactionGroup)
                data
            })
    }



}
