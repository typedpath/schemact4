package org.testedsoftware.accountview

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
    @Test
    fun testAutocatMbna() {
        val transaction = Transaction(date="", subcategory="",
            amount=123, memo="\"MBNA LIMITED 5407582200055254 DDR \"",category="", frequency="", sourceCategory="", categorized=false)
        val filter =    AutoCatFilter(name="MBNA", pattern =  "(.*)MBNA(.+)", type="regex", category = "CreditCard", frequency = "", sourceCategory = "")
//{"name":"MBNA","pattern":" (.*)MBNA(.+) ","type":"regex","category":"CreditCard","frequency":"","sourceCategory":""}
        AutoCat.autoCat(transaction, listOf(filter))
        assertSame(transaction.category, "CreditCard")
//        assert(transaction.category=="Groceries")
    }
}