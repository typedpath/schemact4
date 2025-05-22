package org.testedsoftware.accountview

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.testedsoftware.accountview.RawTransactionFileIO.readJson
import org.testedsoftware.accountview.RawTransactionFileIO.writeJson

class RawTransactionFileIOTest {

@Test
    fun test() {
        /*val transactions = RawTransactionFileIO.read("accountview.testedsoftware.org-private",
            "04986408-8051-702d-fbab-6856452156ed/transactionGroups/jan2024-dec2024.csv")
         */
        val bucketName = "accountview.testedsoftware.org-private"
        val key = "tests/RawTransactionFileIO/transactions.csv"

        val transactionsIn = listOf(tx(amount=12345, memo="some stuff", category="Groceries", frequency = "", sourceCategory = "", categorized = false))

        writeJson(bucketName = bucketName, key=key, transactions=transactionsIn)
        val transactionsOut = readJson(bucketName = bucketName, key=key)

        println(ObjectMapper().writeValueAsString(transactionsOut))
        assertEquals(transactionsIn, transactionsOut)
    }
}