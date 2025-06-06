package org.testedsoftware.accountview

fun TransactionGroup.TransactionFile.path() : String = "$location/$filename"
fun TransactionGroup.TransactionFile.keyFromUserId(userId: String) : String = "$userId${path()}"
