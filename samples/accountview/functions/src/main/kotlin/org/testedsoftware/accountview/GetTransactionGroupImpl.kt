
package org.testedsoftware.accountview
// created by template functionSampleImpl
import org.testedsoftware.accountview.DynamoDbUtil.getUserData
import schemact.aws.CognitoClientDetails
import schemact.aws.VerifyCognito.verifyCognitoJwt


class GetTransactionGroupImpl { 
    // created from template  functionSampleImpl at 2025-05-13T16:21:52.217063500       
    fun getTransactionGroup(userTableName: String, privateBucketName: String, Authorization: String, cognitoDetails: CognitoClientDetails, fromInclusiveDate: String, toInclusiveDate: String, accountNumber: String) : TransactionGroup {
        val cognitoData = verifyCognitoJwt(Authorization, cognitoDetails)
        println(cognitoData)
        val userId = cognitoData.sub
        val email = cognitoData.email!!
        val existingData = getUserData<UserInfo>(userTableName=userTableName, userId=userId,
            email=email, dataType=UserInfo::class.java)
        val account = existingData?.accounts?.find { accountNumber.equals(it.accountNumber)  }?:throw Exception("Account Number not found $accountNumber for user email:$email userId: $userId")
        val transactionGroup = account.transactionGroups.find { it.fromInclusiveDate == fromInclusiveDate && it.toInclusiveDate == toInclusiveDate }?:throw Exception("Transaction Group not found $fromInclusiveDate to $toInclusiveDate for accountNumber $accountNumber for user email:$email userId: $userId")
        val key = "${userId}${transactionGroup.transactionFile.location}/${transactionGroup.transactionFile.filename}"

        try {
            val transactions =
                RawTransactionFileIO.readJson(bucketName = privateBucketName, key = key)

            return TransactionGroup(
                fromInclusiveDate = fromInclusiveDate,
                toInclusiveDate = toInclusiveDate,
                rawTransactionFile = transactionGroup.rawTransactionFile,
                transactionFile = transactionGroup.transactionFile,
                transactions = transactions.onEach { if (!it.categorized) AutoCat.autoCat(it) }
                    .toMutableList()
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
            println("failed to load transactions from $privateBucketName/$key because ${ex.message}  ")
            throw ex
        }
    }
    
}
