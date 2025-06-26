package org.testedsoftware.accountview

import com.fasterxml.jackson.databind.ObjectMapper
import kotlin.String

object UserInfoDeserializer {
    fun deserialize(str: String, version: String) : UserInfo {
        return if (version.equals(UserInfoVersion.latest)) {
            ObjectMapper().readValue<UserInfo>(str, UserInfo::class.java) as UserInfo
        } else if (version.equals("2")) {
            val userInfo2 = ObjectMapper().readValue<UserInfo2>(str, UserInfo2::class.java)
            convert2to3(userInfo2)
        } else if (version.equals("1")) {
            val userInfo1 = ObjectMapper().readValue<UserInfo1>(str, UserInfo1::class.java)
           convert2to3(convert1to2(userInfo1))
        }else if (version.equals("0")) {
            val userInfo0 = ObjectMapper().readValue<UserInfo0>(str, UserInfo0::class.java)

            convert2to3(convert1to2(convert0to1(userInfo0)))
        } else {
            throw Exception("invalid version: $version")
        }
    }

    fun convert2to3(userInfo2: UserInfo2): UserInfo =
        UserInfo(loginEvents=userInfo2.loginEvents, uploads= mutableListOf(), accounts=userInfo2.accounts.map {acc->convertAccount2toAccount3(acc)}.toMutableList(),
            categories =  userInfo2.categories, autoCatFilters = mutableListOf()/*oops lost the autoCatFilters*/)

    fun convert1to2(userInfo1: UserInfo1) : UserInfo2 =
//        val userInfo1 = ObjectMapper().readValue<UserInfo1>(str, UserInfo1::class.java)
        UserInfo2(loginEvents=userInfo1.loginEvents, uploads= mutableListOf(), accounts=userInfo1.accounts.map {acc->convertAccount1toAccount2(acc)}.toMutableList(),
            categories =  userInfo1.categories, autoCatFilters = mutableListOf())


    fun convert0to1(userInfo0: UserInfo0) : UserInfo1 =
        //val userInfo0 = ObjectMapper().readValue<UserInfo0>(str, UserInfo0::class.java)
        UserInfo1(loginEvents=userInfo0.loginEvents, uploads= mutableListOf(),
            accounts=userInfo0.accounts.map { acc->convertAccount0toAccount1(acc) }.toMutableList(),
            categories =  mutableListOf())


    //TODO - should notbenecessary - eliminate with versionable (top level)
    fun convertAccount0toAccount1(account0: UserInfo0.Account0) : UserInfo1.Account0 =
         ObjectMapper().readValue<UserInfo1.Account0>(ObjectMapper().writeValueAsString(account0), UserInfo1.Account0::class.java)


    fun convertAccount1toAccount2(account1: UserInfo1.Account0) : UserInfo2.Account0 =
         ObjectMapper().readValue<UserInfo2.Account0>(ObjectMapper().writeValueAsString(account1), UserInfo2.Account0::class.java)


    fun convertAccount2toAccount3(account2: UserInfo2.Account0) : Account =
         Account(name=account2.name, sortCode=account2.sortCode,  accountNumber=account2.sortCode,
             transactionGroups=account2.transactionGroups.map { acc->transactionGroup2To3(acc) }.toMutableList(),
             pivotTables = mutableListOf())

    fun transactionGroup2To3(transactionGroup0: UserInfo2.Account0.TransactionGroup0) = TransactionGroup(fromInclusiveDate =  transactionGroup0.fromInclusiveDate,
        transactions = transactionGroup0.transactions, toInclusiveDate = transactionGroup0.toInclusiveDate,
        pivotTables = mutableListOf(), rawTransactionFile = updateRawTransactionFile(transactionGroup0.rawTransactionFile),
        transactionFile = updateTransactionFile(transactionGroup0.transactionFile))

    fun updateRawTransactionFile(rawTransactionFile2: UserInfo2.Account0.TransactionGroup0.RawTransactionFile) =
        TransactionGroup.RawTransactionFile(filename = rawTransactionFile2.filename,
                location=rawTransactionFile2.location, contentType =  rawTransactionFile2.contentType,
                           uploadTime = rawTransactionFile2.uploadTime)

    fun updateTransactionFile(transactionFile2: UserInfo2.Account0.TransactionGroup0.TransactionFile) =
        TransactionGroup.TransactionFile(filename = transactionFile2.filename,
            location=transactionFile2.location, contentType =  transactionFile2.contentType,
            uploadTime = transactionFile2.uploadTime)

}