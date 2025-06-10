package samples.accountview

import schemact.domain.Entity
import schemact.domain.StringType
import schemact.domain.bool
import schemact.domain.int
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

val transaction = Entity(name="Transaction", description = "Transaction") {
    string("date", "date", maxLength = 10)
    string("subcategory", "subcategory", maxLength = 30)
    int("amount", "amount in pence")
    string("memo", "what i sit", maxLength = 1000)
    string("category", "e.g. coffee", maxLength = 200)
    string("frequency", "e.g. monthly", maxLength = 200)
    string("sourceCategory", "e.g. creditcard", maxLength = 200)
    bool(name="categorized", "has the transaction been catogorized")
}

val transactionGroup = Entity(name="TransactionGroup", description="Transaction Group" ) {
    string("fromInclusiveDate", "From Inclusive Date", maxLength = 10)
    string("toInclusiveDate", "To Inclusive Date", maxLength = 10)
    containsOne("rawTransactionFile", "Raw Transaction File", type = rawTransactionFile)
    containsOne("transactionFile", "Transaction File", type = transactionFile)
    //containsOne("rawTransactionFile", "Raw Transaction File", type = file)
    containsMany("transactions", "transactions", type= transaction)
    // containsOne("categorizedTransactionFile", "Categorized Transaction File", type = file)
}

val account = Entity(name="Account","accounts") {
    string("name", "file name", maxLength = 200)
    string("sortCode", "where in the s3", maxLength = 10)
    string("accountNumber", "what i sit", maxLength = 16)
    containsMany("transactionGroups", "Transaction Groups", type= transactionGroup)
}

val userInfo0 = Entity(name = "UserInfo0", description="UserInfo0") {
    containsMany(name = "loginEvents", type = StringType(maxLength=200))
    containsMany(name = "uploads", type = randomFile)
    containsMany(name= "accounts", type= account)
}

val userInfo1 = Entity(name = "UserInfo", description="UserInfo",
    version="1") {
    containsMany(name = "loginEvents", type = StringType(maxLength=200))
    containsMany(name = "uploads", type = randomFile)
    containsMany(name= "accounts", type= account)
    containsMany(name="categories", type = StringType(maxLength=200))
}
