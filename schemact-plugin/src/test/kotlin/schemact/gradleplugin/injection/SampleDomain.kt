package schemact.gradleplugin.injection

import schemact.domain.Entity
import schemact.domain.Function
import schemact.domain.InfrastructureInjectables
import schemact.domain.IntType
import schemact.domain.ReactJsInjectables
import schemact.domain.RestInjectables
import schemact.domain.StringType
import schemact.domain.bool
import schemact.domain.int
import schemact.domain.readUserPrivateBucketDataArg
import schemact.domain.string
import schemact.domain.updateUserDataArg
import schemact.domain.writeUserPrivateBucketDataArg

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


val userInfoLatest = Entity(name = "UserNameDetails", description = "UserNameDetails") {
    string("firstName", "firstNamee", maxLength = 10)
    string("middleName", "middleName", maxLength = 10)
    string("lastName", "lastName", maxLength = 20)
}

val getTransactionGroup2  = Function(
    "getTransactionGroup2",
    description = "gets a transactionGroup",
    paramType = Entity(name = "param", description = "Params") {
        writeUserPrivateBucketDataArg()
        string("fromInclusiveDate", "From Inclusive Date", maxLength = 10)
        updateUserDataArg(userInfoLatest)
        readUserPrivateBucketDataArg()
        string("fromInclusiveDate", "From Inclusive Date", maxLength = 10)
        string("toInclusiveDate", "To Inclusive Date", maxLength = 10)
        string("accountNumber", "AccountNumber", maxLength = 20)
    },
    returnType = Entity(name = "status", description = "Status") {
        int("code", "code")
        string("description", "Status Description", maxLength = 20)
    }
//        auth = auth
)

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

val transactionGroupLatest = transactionGroup3


val  categorizeTransactions = schemact.domain.Function(
    "categorizeTransactions",
    description = "gets a transactionGroup",
    paramType = Entity(name = "param", description = "Params") {
        containsOne(
            "userTableName",
            description = "bucketName",
            type = InfrastructureInjectables.DynamoDBTablenameType
        )
        containsOne(
            "privateBucketName",
            description = "bucketName",
            type = InfrastructureInjectables.PrivateBucketNameType
        )
        containsOne(
            "Authorization",
            description = "Authorization header",
            type = RestInjectables.AuthorizationHeaderType
        )
        containsOne(
            "cognitoDetails",
            description = "Cognito Details",
            type = InfrastructureInjectables.CognitoClientDetails.entity
        )
        string("fromInclusiveDate", "From Inclusive Date", maxLength = 10)
        string("toInclusiveDate", "To Inclusive Date", maxLength = 10)
        string("accountNumber", "AccountNumber", maxLength = 20)
        containsMany(
            "transactionUpdates",
            "transaction updates",
            type = Entity(name = "TransactionUpdate", description = "Transaction Update") {
                int("index", "index in file")
                containsOne("transaction", description = "Transaction", type = transaction)
            })
    },
    returnType = transactionGroupLatest,
    //auth = auth
)

val smallUploadFile = ReactJsInjectables.File(name ="File",
    description="Random Uploaded File", maxBytes=10000000)

val uploadFileFunction = schemact.domain.Function(
    "uploadFile",
    description = "uploads a file",
    paramType = Entity(name = "param", description = "Params") {
        containsOne(
            "userTableName",
            description = "bucketName",
            type = InfrastructureInjectables.DynamoDBTablenameType
        )
        containsOne(
            "privateBucketName",
            description = "bucketName",
            type = InfrastructureInjectables.PrivateBucketNameType
        )
        containsOne(
            "Authorization",
            description = "Authorization header",
            type = RestInjectables.AuthorizationHeaderType
        )
                containsOne(
                    "cognitoDetails", description = "Cognito Details",
                    type = InfrastructureInjectables.CognitoClientDetails.entity
                )
        // for debug / development
                containsOne(
                    "input", description = "native input details",
                    type = InfrastructureInjectables.APIGatewayV2HTTPEventEntity
                )
        containsOne(name = "file", "upload file", smallUploadFile)
    },
    returnType = userInfoLatest,
   // auth = auth
)
