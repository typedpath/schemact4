
package org.testedsoftware.accountview
// created by template functionSampleImpl
import com.fasterxml.jackson.annotation.JsonProperty
import org.testedsoftware.accountview.DynamoDbUtil.getUserData
import schemact.aws.CognitoClientDetails
import schemact.aws.VerifyCognito.verifyCognitoJwt


class GetTransactionGroupImpl { 
    // created from template  functionSampleImpl at 2025-05-13T16:21:52.217063500       
    fun getTransactionGroup(userTableName: String, privateBucketName: String, Authorization: String, cognitoDetails: CognitoClientDetails, fromInclusiveDate: String, toInclusiveDate: String, accountNumber: String) : Account.TransactionGroup {
        val cognitoData = verifyCognitoJwt(Authorization, cognitoDetails)
        println(cognitoData)
        val userId = cognitoData.sub
        val email = cognitoData.email!!
        val existingData = getUserData<UserInfo>(userTableName=userTableName, userId=userId,
            email=email, dataType=UserInfo::class.java)
        val account = existingData?.accounts?.find { accountNumber.equals(it.accountNumber)  }?:throw Exception("Account Number not found $accountNumber for user email:$email userId: $userId")
        val transactionGroup = account.transactionGroups.find { it.fromInclusiveDate == fromInclusiveDate && it.toInclusiveDate == toInclusiveDate }?:throw Exception("Transaction Group not found $fromInclusiveDate to $toInclusiveDate for accountNumber $accountNumber for user email:$email userId: $userId")
        val key = "${userId}${transactionGroup.rawTransactionFile.location}"
         val transactions = ReadRawTransactionFile.read(privateBucketName, key)

         return Account.TransactionGroup(fromInclusiveDate=fromInclusiveDate,
             toInclusiveDate=toInclusiveDate, rawTransactionFile = Account.TransactionGroup.File(
                 filename="a filename", location="alocation", uploadTime="14/5/24",
                 contentType = "Test content type"),
             transactions = transactions.toMutableList()
         )
    }
    
}
