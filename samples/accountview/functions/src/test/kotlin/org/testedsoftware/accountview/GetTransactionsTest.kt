package org.testedsoftware.accountview

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.testedsoftware.accountview.AutoCatFiltersExtra.defaultAutoCatFilters
import org.testedsoftware.accountview.TransactionGroup.RawTransactionFile
import org.testedsoftware.accountview.TransactionGroup.TransactionFile
import schemact.aws.ReadUserDataPrivateBucket
import schemact.aws.UpdateUserInfo

class GetTransactionsTest {

    val target = GetTransactionGroupImpl()
    @Test
    fun testGetTransactions() {
        val tescoTransaction = Transaction(date="12/03/2025",
             subcategory="",
            amount=123, memo="TESCO-STORES whatever", category="", frequency="", sourceCategory="", categorized=false)
        val startTransactions = listOf(tescoTransaction)
        val readUserDataPrivateBucket: ReadUserDataPrivateBucket = {
                key ->
                    println("readUserDataPrivateBucket key: $key")
                    ObjectMapper().writeValueAsString(startTransactions)
        }
        // TODO make this val ?
        var userInfo = UserInfo()
        val updateUserInfo: UpdateUserInfo = {
            update ->
            // TODO put in filter
            println("updateUserInfo.update:in")
                if (update!=null) userInfo = update(userInfo)
                println("updateUserInfo.update:out:${userInfo}")
                userInfo
        }

        val fromInclusiveDate = "12/05/98"
        val toInclusiveDate= "12/05/98"
        val  accountNumber = "12345678"

        val account = Account(
            name="testaccount",
            sortCode="",
            accountNumber=accountNumber,
        )
        userInfo.accounts.add(account)
        val transactionGroup = TransactionGroup(fromInclusiveDate, toInclusiveDate,
            RawTransactionFile(
                filename = "rawTransactionFileName",
                location = "/transactionGroups",
                contentType = "application/json",
                uploadTime = "12/05/98",
            ),
            transactionFile= TransactionFile(
                filename = "transactionFile",
                location = "/transactionGroups",
                contentType = "application/json",
                uploadTime = "12/05/98",
            ), mutableListOf())

        account.transactionGroups.add(transactionGroup)
        // TODO should be part of a structured update, separate from login
        userInfo.autoCatFilters = defaultAutoCatFilters().toMutableList()

        val result = target.getTransactionGroup(readUserDataPrivateBucket,
            updateUserInfo,
            fromInclusiveDate, toInclusiveDate, accountNumber)
        println("result:in")
        println(result)
        println("result.transactions.size: ${result.transactions.size}")
        println("result.transactions[0].category: ${result.transactions[0].category}")
        println("result:out")
    }

}