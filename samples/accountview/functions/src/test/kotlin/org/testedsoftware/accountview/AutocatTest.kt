package org.testedsoftware.accountview

import com.fasterxml.jackson.annotation.JsonProperty
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class AutocatTest {
    @Test
    fun testAutocat() {
        val transaction = Transaction(date="", subcategory="",
            amount=123, memo="TESCO-STORES-5749     \\tON 27 DEC CPM\\t",category="", frequency="", sourceCategory="", categorized=false)
        AutoCat.autoCat(transaction, AutoCatFiltersExtra.defaultAutoCatFilters())
       assertSame(transaction.category, "Groceries")
//        assert(transaction.category=="Groceries")
    }
}