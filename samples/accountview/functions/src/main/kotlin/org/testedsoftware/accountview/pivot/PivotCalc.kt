package org.testedsoftware.accountview.pivot


import org.testedsoftware.accountview.PivotTable
import org.testedsoftware.accountview.Transaction
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter

object PivotCalc {
    fun pivotCategories(transactions: List<Transaction>) : PivotTable {
        val transactionByCategory = transactions.groupBy {  it.category }
        val transactionsByMonth = transactions.groupBy { firstOfMonth(it.date)}

        val categories = transactionByCategory.keys.toList().sorted()

        var earliestDate = firstOfMonth(transactions.last().date)
        var latestDate =firstOfMonth(transactions.first().date)

        var currentDate = earliestDate
        val monthDates = mutableListOf<LocalDate>()
        while (currentDate==latestDate || currentDate.isBefore(latestDate)) {
            monthDates.add(currentDate)
            currentDate = currentDate.plusMonths(1)
        }

        val headerColumn = PivotTable.LabelColumn(labelTitle="category", labels = categories.toMutableList(), footer="Total")

        val monthFormatter = DateTimeFormatter.ofPattern("MMM yy")

        println("pivotCategories found  ${monthDates}")

        var valueColumns: MutableList<PivotTable.ValueColumn>
        var totalValueColumn = PivotTable.ValueColumn(header="${monthFormatter.format(earliestDate)}-${monthFormatter.format(latestDate)}, ",
                footer=0, values = MutableList(size= categories.size+1) { i->0 })
        valueColumns =  monthDates.map {
            var trs = transactionsByMonth[it]
            if (trs==null) trs = emptyList()
            val values = categories.map { cat->   trs.filter{ it.category==cat  }.map { it.amount }.sum()  }.toMutableList()
            val result = PivotTable.ValueColumn(header=monthFormatter.format(it), values=values, trs.map { it.amount }.sum())
            values.forEachIndexed { index, value -> totalValueColumn.values[index]+=value }
            result
        }.toMutableList()

        totalValueColumn.footer = totalValueColumn.values.sum()
        valueColumns.add(totalValueColumn)

        return PivotTable(name="category", header = headerColumn,  valueColumns=valueColumns)
    }

    private fun string2Date(str: String) =  LocalDate.parse(str, java.time.format.DateTimeFormatter.ofPattern( "dd/MM/yyyy"))

    private fun  firstOfMonth(strDate: String) : LocalDate {
        val date = string2Date(strDate)
        return LocalDate.of(date.year, date.month, 1)
    }

}


