package org.testedsoftware.accountview

object  BarclaysCsvReader {
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
                        memo = stripCellQuotes(cells[5]),
                        sourceCategory = "",
                        category = "",
                        frequency = "",
                        categorized = false
                    ))
            }
        }
        return transactions
    }

    private fun stripCellQuotes(str: String) = if (str.startsWith('"') && str.endsWith('"')) str.substring(1, str.length-1) else str

}