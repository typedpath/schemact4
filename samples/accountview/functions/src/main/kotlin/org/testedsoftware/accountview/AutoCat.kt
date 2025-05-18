package org.testedsoftware.accountview

object AutoCat {
    val Groceries = "Groceries"
    val Morrisons = "Morrison"
    val Waitrose = "Waitrose"
    val Tesco = "Tesco"
    val Marks = "Marks"
    val Sainsburys = "Sainsburys"
    val groceryStorePrefixes = setOf(Morrisons, Waitrose, Tesco, Marks, Sainsburys)


    fun autoCat(transaction: Account.TransactionGroup.Transaction) {
       val lMemo = transaction.memo.lowercase()
        when  {
           groceryStorePrefixes.any { lMemo.startsWith(it.lowercase()) } -> {
               transaction.category = Groceries
           }
       }
    }
}