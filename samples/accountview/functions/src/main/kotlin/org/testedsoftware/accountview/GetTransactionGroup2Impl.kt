
package org.testedsoftware.accountview

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.testedsoftware.accountview.pivot.PivotCalc

// created by template functionSampleImplNew

class GetTransactionGroup2Impl { 
    // created from template  functionSampleImplNew at 2025-08-07T12:56:04.795413100       
    fun getTransactionGroup2(WriteUserPrivateBucketData: schemact.aws.WriteUserPrivateBucketData,
                             ReadUserPrivateBucketData: schemact.aws.ReadUserPrivateBucketData, UpdateUserInfo: schemact.aws.UpdateUserData<UserInfo>, fromInclusiveDate: String, toInclusiveDate: String, accountNumber: String) : TransactionGroup {
        var result: TransactionGroup? = null

        UpdateUserInfo(
             {
                    existingData ->
                val account = existingData?.accounts?.find { accountNumber.equals(it.accountNumber)  }?:
                throw Exception("Account Number not found $accountNumber ")
                val transactionGroup = account.transactionGroups.find { it.fromInclusiveDate == fromInclusiveDate && it.toInclusiveDate == toInclusiveDate }?:throw Exception("Transaction Group not found $fromInclusiveDate to $toInclusiveDate for accountNumber $accountNumber")
                val key = "${transactionGroup.transactionFile.location}/${transactionGroup.transactionFile.filename}"
                val content = ReadUserPrivateBucketData(key)
                val transactions: List<Transaction> = ObjectMapper().readValue(content,  object : TypeReference<List<Transaction>>() {} )
                result = TransactionGroup(
                    fromInclusiveDate = fromInclusiveDate,
                    toInclusiveDate = toInclusiveDate,
                    rawTransactionFile = transactionGroup.rawTransactionFile,
                    transactionFile = transactionGroup.transactionFile,
                    transactions = transactions.onEach { if (!it.categorized) AutoCat.autoCat(it, existingData.autoCatFilters) }
                        .map { Transaction(date=it.date, subcategory = it.subcategory, amount = it.amount,
                            memo=it.memo, category = it.category, categorized =  it.categorized, frequency = it.frequency, sourceCategory = it.sourceCategory) }
                        .toMutableList(),
                    pivotTables = listOf(PivotCalc.pivotCategories(transactions)).toMutableList()
                )
                existingData
            },
             {
                    str, version-> UserInfoDeserializer.deserialize(str, version)
            },
             { UserInfo()})
    return result!!
    }
    
}
