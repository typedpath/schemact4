package org.testedsoftware.accountview

import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper


import java.time.LocalDate

object RawTransactionFileIO {

    private val s3 = AmazonS3ClientBuilder.standard().withRegion("us-east-1").build()

    fun readBarclaysCsvContent(content: String) : List<Transaction> {
        val transactions =  mutableListOf<Transaction>()
        content.lines().forEachIndexed {
                i, line ->
            println("proessing line: $line")
            val cells = line.split(",")
            if (i!=0 && cells.size>=6) {
                transactions.add(
                    Transaction(
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

    fun readBarclaysCsv(bucketName: String, key: String) : List<Transaction>{
        val getObjectRequest = GetObjectRequest(bucketName, key)
        val s3Object = s3.getObject(getObjectRequest)
        val contentType = s3Object.objectMetadata.contentType
        val content = s3Object.objectContent.bufferedReader().use { it.readText() }
        println(content)
        return readBarclaysCsvContent(content)
    }

    fun writeJson(bucketName: String,  key: String, transactions: List<Transaction>) {
        val inputStream = (ObjectMapper()).writeValueAsString(transactions).byteInputStream()
        val om = ObjectMetadata()
        om.contentType =  "application/json; charset=utf-8"
        val writeObjectRequest = PutObjectRequest(bucketName, key, inputStream, om)
        val s3Object = s3.putObject(writeObjectRequest)
    }

    fun readJson(bucketName: String,  key: String) : List<Transaction>{
        val getObjectRequest = GetObjectRequest(bucketName, key)
        val s3Object = s3.getObject(getObjectRequest)
        val contentType = s3Object.objectMetadata.contentType
        val content = s3Object.objectContent.bufferedReader().use { it.readText() }
        return stringToTransactions(content)
    }

    fun stringToTransactions(content: String) : List<Transaction> =
        ObjectMapper().readValue(content,  object : TypeReference<List<Transaction>>() {} )

}





fun tx(amount:  Int, memo:  String, category:  String, frequency:  String, sourceCategory:  String, categorized:  Boolean) =
    Transaction(date= LocalDate.now().toString(), subcategory="subcategory",  amount = amount, memo=memo, frequency=frequency, sourceCategory = sourceCategory,
        categorized=categorized, category="mycategory")


