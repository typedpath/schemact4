package org.testedsoftware.accountview

object  AutoCatFiltersExtra {
fun defaultAutoCatFilters() : List<AutoCatFilter> {
    return listOf(
        AutoCatFilter(name="Amazon", pattern =  "(.*)AMZNMktplace(.+)", type="regex", category = "Groceries", frequency = "", sourceCategory = ""),
        AutoCatFilter(name="TESCO", pattern =  "(.*)TESCO(.+)STORES(.+)", type="regex", category = "Groceries", frequency = "", sourceCategory = ""),
        AutoCatFilter(name="Greggs", pattern =  "(.*)Greggs(.+)", type="regex", category = "Coffee", frequency = "", sourceCategory = ""),
        )
}
}