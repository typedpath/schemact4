package org.testedsoftware.accountview

import java.lang.System.lineSeparator

object AutoCat {
    val Groceries = "Groceries"
    val Morrisons = "Morrison"
    val Waitrose = "Waitrose"
    val Tesco = "Tesco"
    val Marks = "Marks"
    val Sainsburys = "Sainsburys"
    val groceryStorePrefixes = setOf(Morrisons, Waitrose, Tesco, Marks, Sainsburys)

    // autoCatFilters:  MutableList<AutoCatFilter>

    fun autoCat(transaction: Transaction, autoCatFilters:  List<AutoCatFilter>) {
println("autoCat:in ${autoCatFilters.map{"${lineSeparator()}autoCat.pattern : ${it.pattern}"}.joinToString(",")}" )
        println()
        println("autoCat:transaction.category ${transaction.category}")
        println("autoCat:transaction.memo ${transaction.memo}")
        autoCatFilters.find {
            it.type=="regex" && transaction.memo.contains(Regex(it.pattern))
                    || it.type=="contains" && transaction.memo.contains(it.pattern)}?.apply {
println("autoCat:hit")
            transaction.category=category
            transaction.frequency=frequency
            transaction.sourceCategory=sourceCategory
        }
println("autoCat:out")
    }
}