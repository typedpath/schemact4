
package org.testedsoftware.accountview
// created by template functionSampleImpl
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import schemact.aws.CognitoClientDetails
import schemact.aws.VerifyCognito.verifyCognitoJwt


class CategorizeTransactionsImpl {
    // created from template  functionSampleImpl at 2025-05-20T12:05:21.616220700       
    fun categorizeTransactions(userTableName: String, privateBucketName: String, Authorization: String, cognitoDetails: CognitoClientDetails, fromInclusiveDate: String, toInclusiveDate: String, accountNumber: String, transactionUpdates: List<TransactionUpdate>) : TransactionGroup {
        // create a categoriesChange event

            // call the event processor

        val cognitoData = verifyCognitoJwt(Authorization, cognitoDetails)

        val updateUserInfo:   UpdateUserInfoOld =
                {
                    update ->
                    UserInfoUpdaterOld.update(userTableName=userTableName, userId=cognitoData.sub,
                        email = cognitoData.email?:"noemail", update=update)
                }

        val writeToUserToDataPrivateBucket : WriteToUserToDataPrivateBucket =
            {
                key, value ->
                    val s3 = AmazonS3ClientBuilder.standard().withRegion("us-east-1").build()
                    val inputStream = (ObjectMapper()).writeValueAsString(value).byteInputStream()
                    val om = ObjectMetadata()
                    om.contentType =  "application/json; charset=utf-8"
                    val privatisedKey = "${cognitoData.sub}$key"
                    val writeObjectRequest = PutObjectRequest(privateBucketName, privatisedKey, inputStream, om)
                    val s3Object = s3.putObject(writeObjectRequest)
            }


        val readUserDataPrivateBucket : ReadUserDataPrivateBucket =
            {
            key ->
                     val s3 = AmazonS3ClientBuilder.standard().withRegion("us-east-1").build()
                     val privatisedKey = "${cognitoData.sub}$key"
                     val getObjectRequest = GetObjectRequest(privateBucketName, privatisedKey)
                     try {
                         val s3Object = s3.getObject(getObjectRequest)
                         val contentType = s3Object.objectMetadata.contentType
                         s3Object.objectContent.bufferedReader().use { it.readText() }
                     } catch (ex: Exception) {
                         println("failed to read object $privatisedKey in $privateBucketName")
                         throw ex
                     }
            }


       return categorizeTransactions( updateUserInfo=updateUserInfo,
           writeToUserToDataPrivateBucket=writeToUserToDataPrivateBucket,
           readUserDataPrivateBucket =  readUserDataPrivateBucket,
           fromInclusiveDate=fromInclusiveDate, toInclusiveDate=toInclusiveDate, accountNumber=accountNumber,
           transactionUpdates=transactionUpdates )

    }

    fun categorizeTransactions(
        updateUserInfo:   UpdateUserInfoOld,
        writeToUserToDataPrivateBucket:WriteToUserToDataPrivateBucket,
        readUserDataPrivateBucket : ReadUserDataPrivateBucket,
        fromInclusiveDate: String, toInclusiveDate: String, accountNumber: String,
        transactionUpdates: List<TransactionUpdate>  ) : TransactionGroup {

        var transactionGroup: TransactionGroup?=null
        var transactionGroupTransactions = mutableListOf<Transaction>()
        val update: (userInfo: UserInfo) -> UserInfo = {
            userInfo ->
            val account = userInfo.accounts.find { it.accountNumber.equals(accountNumber) }?:throw Exception("Account not found $accountNumber")
            transactionGroup = account.transactionGroups.find { it.fromInclusiveDate.equals(fromInclusiveDate) && it.toInclusiveDate.equals(toInclusiveDate) }?:throw
            Exception("Transaction group not found account: $accountNumber fromInclusiveDate: $fromInclusiveDate toInclusiveDate: $toInclusiveDate")
            val strTransactions = readUserDataPrivateBucket(transactionGroup.transactionFile.path())
            // TODO make 1 transaction def only !!
            val transactions = ObjectMapper().readValue(strTransactions,  object : TypeReference<List<Transaction>>() {}).toMutableList()
            println("updating ${transactionUpdates.size} transactions ")
            transactionUpdates.forEach {
                if (it.index<0 ||  it.index>transactions.size) {
                    throw IllegalArgumentException("Transaction update index out of bounds: ${it.index} - size: ${transactions.size}")
                }
                println("updating ${it.index} transactions to ${ObjectMapper().writeValueAsString(it)}")
                transactions[it.index] = it.transaction
            }

            writeToUserToDataPrivateBucket(transactionGroup.transactionFile.path(),  transactions)
            transactionGroupTransactions=transactions.map { Transaction(
                date=it.date,
                subcategory=it.subcategory,
                amount=it.amount,
                memo=it.memo,
                category=it.category,
                frequency=it.frequency,
                sourceCategory=it.sourceCategory,
                categorized=it.categorized
            ) }.toMutableList()
            userInfo
        }

        val userInfo = updateUserInfo(update)
        transactionGroup!!.transactions=transactionGroupTransactions
        return transactionGroup

    }

    
}
