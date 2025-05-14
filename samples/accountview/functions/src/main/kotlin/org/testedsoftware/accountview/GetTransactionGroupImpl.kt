
package org.testedsoftware.accountview
// created by template functionSampleImpl
import com.fasterxml.jackson.annotation.JsonProperty
import schemact.aws.CognitoClientDetails


class GetTransactionGroupImpl { 
    // created from template  functionSampleImpl at 2025-05-13T16:21:52.217063500       
    fun getTransactionGroup(userTableName: String, privateBucketName: String, Authorization: String, cognitoDetails: CognitoClientDetails, fromInclusiveDate: String, toInclusiveDate: String, accountNumber: String) : Account.TransactionGroup {
         return Account.TransactionGroup(fromInclusiveDate=fromInclusiveDate,
             toInclusiveDate=toInclusiveDate, rawTransactionFile = Account.TransactionGroup.File(
                 filename="a filename", location="alocation", uploadTime="14/5/24",
                 contentType = "Test content type"),
             transactions = mutableListOf(
                 Account.TransactionGroup.Transaction(date= "12/5/24", subcategory="CreditCard",
                         amount=12345, memo="fishing equipment")
             )
         )
    }
    
}
