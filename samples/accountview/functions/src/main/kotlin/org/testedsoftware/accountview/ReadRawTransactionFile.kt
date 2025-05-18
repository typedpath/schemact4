package org.testedsoftware.accountview

import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.GetObjectRequest
import com.fasterxml.jackson.databind.ObjectMapper

object ReadRawTransactionFile {

    private val s3 = AmazonS3ClientBuilder.standard().withRegion("us-east-1").build()

    fun read( bucketName: String,  key: String) : List<Account.TransactionGroup.Transaction>{
        val getObjectRequest = GetObjectRequest(bucketName, key)
        val s3Object = s3.getObject(getObjectRequest)
        val contentType = s3Object.objectMetadata.contentType
        val content = s3Object.objectContent.bufferedReader().use { it.readText() }
        println(content)
        val transactions =  mutableListOf<Account.TransactionGroup.Transaction>()
        content.lines().forEachIndexed {
            i, line ->
              println("proessing line: $line")
              val cells = line.split(",")
              if (i!=0 && cells.size>=6) {
                  transactions.add(
                      Account.TransactionGroup.Transaction(
                         date = cells[1], subcategory =  cells[4],
                         amount =  (cells[3].toDouble() * 100).toInt(),
                         memo = cells[5],
                         sourceCategory = "",
                         category = "",
                         frequency = "",
                          categorized = false
                      ))
              }
        }
        return transactions
    }

}

fun main(args: Array<String>) {
    val transactions = ReadRawTransactionFile.read("accountview.testedsoftware.org-private",
        "04986408-8051-702d-fbab-6856452156ed/transactionGroups/jan2024-dec2024.csv")
    println(ObjectMapper().writeValueAsString(transactions))

}