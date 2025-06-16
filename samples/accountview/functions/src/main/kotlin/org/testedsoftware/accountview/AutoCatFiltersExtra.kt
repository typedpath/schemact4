package org.testedsoftware.accountview

object  AutoCatFiltersExtra {
fun defaultAutoCatFilters() : List<UserInfo.AutoCatFilter> {
    return listOf(
        UserInfo.AutoCatFilter(name="Amazon", pattern =  "(.*)AMZNMktplace(.+)", type="regex", category = "Groceries", frequency = "", sourceCategory = ""),
        UserInfo.AutoCatFilter(name="TESCO", pattern =  "(.*)TESCO(.+)STORES(.+)", type="regex", category = "Groceries", frequency = "", sourceCategory = ""),
        UserInfo.AutoCatFilter(name="Greggs", pattern =  "(.*)Greggs(.+)", type="regex", category = "Coffee", frequency = "", sourceCategory = ""),

        )
}
}