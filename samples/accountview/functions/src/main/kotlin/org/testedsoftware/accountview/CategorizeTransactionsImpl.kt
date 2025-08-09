
package org.testedsoftware.accountview

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper

// created by template functionSampleImplNew

class CategorizeTransactionsImpl { 
    // created from template  functionSampleImplNew at 2025-08-09T22:47:06.804452700       
    fun categorizeTransactions(WriteUserPrivateBucketData: schemact.aws.WriteUserPrivateBucketData, ReadUserPrivateBucketData: schemact.aws.ReadUserPrivateBucketData, UpdateUserInfo: schemact.aws.UpdateUserData<UserInfo>, fromInclusiveDate: String, toInclusiveDate: String, accountNumber: String, transactionUpdates: List<TransactionUpdate>) : TransactionGroup {
        var transactionGroup: TransactionGroup?=null
        var transactionGroupTransactions = mutableListOf<Transaction>()
        val userInfo =  AccountViewUserInfoUpdate.accountViewUserInfoUpdate(UpdateUserInfo) { userInfo ->
            val account = userInfo.accounts.find { it.accountNumber.equals(accountNumber) }
                ?: throw Exception("Account not found $accountNumber")
            transactionGroup = account.transactionGroups.find {
                it.fromInclusiveDate.equals(fromInclusiveDate) && it.toInclusiveDate.equals(
                    toInclusiveDate
                )
            }
                ?: throw Exception("Transaction group not found account: $accountNumber fromInclusiveDate: $fromInclusiveDate toInclusiveDate: $toInclusiveDate")
            val strTransactions = ReadUserPrivateBucketData(transactionGroup.transactionFile.path())
            // TODO make 1 transaction def only !!
            val transactions = ObjectMapper().readValue(
                strTransactions,
                object : TypeReference<List<Transaction>>() {}).toMutableList()
            println("updating ${transactionUpdates.size} transactions ")
            transactionUpdates.forEach {
                if (it.index < 0 || it.index > transactions.size) {
                    throw IllegalArgumentException("Transaction update index out of bounds: ${it.index} - size: ${transactions.size}")
                }
                println("updating ${it.index} transactions to ${ObjectMapper().writeValueAsString(it)}")
                transactions[it.index] = it.transaction
            }

            WriteUserPrivateBucketData(transactionGroup.transactionFile.path(), transactions)
            transactionGroupTransactions = transactions.map {
                Transaction(
                    date = it.date,
                    subcategory = it.subcategory,
                    amount = it.amount,
                    memo = it.memo,
                    category = it.category,
                    frequency = it.frequency,
                    sourceCategory = it.sourceCategory,
                    categorized = it.categorized
                )
            }.toMutableList()
            userInfo
        }
        transactionGroup!!.transactions=transactionGroupTransactions
        return transactionGroup!!
    }
    
}
