
package org.testedsoftware.accountview

import java.time.LocalDateTime

// created by template functionSampleImplNew

class UploadTransactionGroupImpl { 
    // created from template  functionSampleImplNew at 2025-08-10T13:31:59.696656200       
    fun uploadTransactionGroup(WriteUserPrivateBucketData: schemact.aws.WriteUserPrivateBucketData,
                               UpdateUserInfo: schemact.aws.UpdateUserData<UserInfo>,
                               file: schemact.react.File, fromInclusiveDate: String, toInclusiveDate: String, accountNumber: String) : UserInfo {
        val groupName = file.filename.substring(0, file.filename.lastIndexOf("."))

        // Upload to S3 with environment-specific prefix
        val location = "/transactionGroups/${groupName}"
        val rawDataKey = "$location/${file.filename}"

        val strContent = String(file.content)

        WriteUserPrivateBucketData(rawDataKey, strContent)

        val transactions = BarclaysCsvReader.readBarclaysCsvContent(strContent)
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

        WriteUserPrivateBucketData("$location/${transactionFileName}", transactions)

        return AccountViewUserInfoUpdate.accountViewUserInfoUpdate(UpdateUserInfo) {
                data ->
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
        }
    }
    
}
