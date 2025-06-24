package samples.accountview

import schemact.domain.Entity
import schemact.domain.IntType
import schemact.domain.StringType
import schemact.domain.bool
import schemact.domain.string

val transactionFile = Entity(name="TransactionFile",
    "uploaded file") {
    string("filename", "file name", maxLength = 2000)
    string("location", "where in the s3", maxLength = 2000)
    string("contentType", "what i sit", maxLength = 2000)
    string("uploadTime", "uploadTime", maxLength = 2000)
}

val randomFile = Entity(name="RandomFile",
    "uploaded file") {
    string("filename", "file name", maxLength = 2000)
    string("location", "where in the s3", maxLength = 2000)
    string("contentType", "what i sit", maxLength = 2000)
    string("uploadTime", "uploadTime", maxLength = 2000)
}

val rawTransactionFile = Entity(name="RawTransactionFile",
    "uploaded file") {
    string("filename", "file name", maxLength = 2000)
    string("location", "where in the s3", maxLength = 2000)
    string("contentType", "what i sit", maxLength = 2000)
    string("uploadTime", "uploadTime", maxLength = 2000)
}

val cashAmount =  IntType()

val transaction = Entity(name="Transaction", description = "Transaction") {
    string("date", "date", maxLength = 10)
    string("subcategory", "subcategory", maxLength = 30)
    containsOne("amount", description="Amount", type = cashAmount)
    string("memo", "what i sit", maxLength = 1000)
    string("category", "e.g. coffee", maxLength = 200)
    string("frequency", "e.g. monthly", maxLength = 200)
    string("sourceCategory", "e.g. creditcard", maxLength = 200)
    bool(name="categorized", "has the transaction been catogorized")
}

val transactionGroup0 = Entity(name="TransactionGroup0", description="Transaction Group" ) {
    string("fromInclusiveDate", "From Inclusive Date", maxLength = 10)
    string("toInclusiveDate", "To Inclusive Date", maxLength = 10)
    containsOne("rawTransactionFile", "Raw Transaction File", type = rawTransactionFile)
    containsOne("transactionFile", "Transaction File", type = transactionFile)
    //containsOne("rawTransactionFile", "Raw Transaction File", type = file)
    containsMany("transactions", "transactions", type= transaction)
    // containsOne("categorizedTransactionFile", "Categorized Transaction File", type = file)
}

// TODO new transaction group type ?
// migration strategy - or just restart ?
val pivotTable = Entity(name = "PivotTable", description = "PivotTable") {
    string("name", "Category By Month", maxLength = 200)
    containsOne("header", type=Entity(name="LabelColumn", description="LabelColumn") {
        containsOne(name = "labelTitle", type = StringType(maxLength=200))
        containsMany(name = "labels", type =  StringType(maxLength=200))
        containsOne(name = "footer", type = StringType(maxLength=200))
    })
    containsMany("valueColumns", type=Entity(name="ValueColumn", description="LabelColumn") {
        containsOne(name = "header", type = StringType(maxLength=200))
        containsMany(name = "values", type = cashAmount)
        containsOne(name = "footer", type = cashAmount)
    })
}


val transactionGroup3 = Entity(name="TransactionGroup", description="Transaction Group" ) {
    string("fromInclusiveDate", "From Inclusive Date", maxLength = 10)
    string("toInclusiveDate", "To Inclusive Date", maxLength = 10)
    containsOne("rawTransactionFile", "Raw Transaction File", type = rawTransactionFile)
    containsOne("transactionFile", "Transaction File", type = transactionFile)
    //containsOne("rawTransactionFile", "Raw Transaction File", type = file)
    containsMany("transactions", "transactions", type= transaction)
    // containsOne("categorizedTransactionFile", "Categorized Transaction File", type = file)
    containsMany("pivotTables", "pivotTables", type= pivotTable)
}

val account0 = Entity(name="Account0","accounts") {
    string("name", "file name", maxLength = 200)
    string("sortCode", "where in the s3", maxLength = 10)
    string("accountNumber", "what i sit", maxLength = 16)
    containsMany("transactionGroups", "Transaction Groups", type= transactionGroup0)
}

val account3 = Entity(name="Account","accounts") {
    string("name", "file name", maxLength = 200)
    string("sortCode", "where in the s3", maxLength = 10)
    string("accountNumber", "what i sit", maxLength = 16)
    containsMany("transactionGroups", "Transaction Groups", type= transactionGroup3)
    containsMany("pivotTables", "pivotTables", type= pivotTable)
}

val userInfo0 = Entity(name = "UserInfo0", description="UserInfo0") {
    containsMany(name = "loginEvents", type = StringType(maxLength=200))
    containsMany(name = "uploads", type = randomFile)
    containsMany(name= "accounts", type= account0)
}

val userInfo1 = Entity(name = "UserInfo1", description="UserInfo",
    version="1") {
    containsMany(name = "loginEvents", type = StringType(maxLength=200))
    containsMany(name = "uploads", type = randomFile)
    containsMany(name= "accounts", type= account0)
    containsMany(name="categories", type = StringType(maxLength=200))
}

val autoCatFilter = Entity(name = "AutoCatFilter", description = "AutoCatFilter") {
    string("name", "name", maxLength = 200)
    string("pattern", "pattern", maxLength = 400)
    string("type", "kotlin type", maxLength = 400)
    string("category", "e.g. coffee", maxLength = 200)
    string("frequency", "e.g. monthly", maxLength = 200)
    string("sourceCategory", "e.g. creditcard", maxLength = 200)
}

val userInfo2 = Entity(name = "UserInfo2", description="UserInfo",
    version="2") {
    containsMany(name = "loginEvents", type = StringType(maxLength=200))
    containsMany(name = "uploads", type = randomFile)
    containsMany(name= "accounts", type= account0)
    containsMany(name="categories", type = StringType(maxLength=200))
    containsMany(name="autoCatFilters", type = autoCatFilter)
}

val userInfo3 = Entity(name = "UserInfo", description="UserInfo",
    version="3") {
    containsMany(name = "loginEvents", type = StringType(maxLength=200))
    containsMany(name = "uploads", type = randomFile)
    containsMany(name= "accounts", type= account3)
    containsMany(name="categories", type = StringType(maxLength=200))
    containsMany(name="autoCatFilters", type = autoCatFilter)
}




