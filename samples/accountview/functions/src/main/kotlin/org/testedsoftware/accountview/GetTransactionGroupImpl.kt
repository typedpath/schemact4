
package org.testedsoftware.accountview
// created by template functionSampleImpl
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.testedsoftware.accountview.DynamoDbUtil.getUserData
import org.testedsoftware.accountview.pivot.PivotCalc
import schemact.aws.CognitoClientDetails
import schemact.aws.InjectablesFactory
import schemact.aws.ReadUserDataPrivateBucket
import schemact.aws.UpdateUserInfo
import schemact.aws.VerifyCognito.verifyCognitoJwt
import schemact.aws.WriteToUserToDataPrivateBucket


class GetTransactionGroupImpl {
    // created from template  functionSampleImpl at 2025-05-13T16:21:52.217063500       

    fun getTransactionGroup(userTableName: String, privateBucketName: String, Authorization: String, cognitoDetails: CognitoClientDetails, fromInclusiveDate: String, toInclusiveDate: String, accountNumber: String) : TransactionGroup {

        val secureUserInjectables: InjectablesFactory.SecureUserInjectables =  InjectablesFactory.create(Authorization=Authorization,
            privateBucketName=privateBucketName, cognitoDetails =  cognitoDetails, userTableName =  userTableName)
        return getTransactionGroup(readUserDataPrivateBucket = secureUserInjectables.readUserDataPrivateBucket!!,
            updateUserInfo =  secureUserInjectables.updateUserInfo,
            fromInclusiveDate = fromInclusiveDate,
            toInclusiveDate = toInclusiveDate,
            accountNumber = accountNumber,
            )

    }

    // TODO autogenerate everything before this
    fun getTransactionGroup(readUserDataPrivateBucket: ReadUserDataPrivateBucket,
                            updateUserInfo: UpdateUserInfo,
                            fromInclusiveDate: String, toInclusiveDate: String, accountNumber: String) : TransactionGroup {

        var result: TransactionGroup? = null

        updateUserInfo(
            {
               existingData ->
                val account = existingData?.accounts?.find { accountNumber.equals(it.accountNumber)  }?:
                throw Exception("Account Number not found $accountNumber ")
                val transactionGroup = account.transactionGroups.find { it.fromInclusiveDate == fromInclusiveDate && it.toInclusiveDate == toInclusiveDate }?:throw Exception("Transaction Group not found $fromInclusiveDate to $toInclusiveDate for accountNumber $accountNumber")
                val key = "${transactionGroup.transactionFile.location}/${transactionGroup.transactionFile.filename}"
                val content = readUserDataPrivateBucket(key)
                val transactions: List<Transaction> = ObjectMapper().readValue(content,  object : TypeReference<List<Transaction>>() {} )
                result = TransactionGroup(
                        fromInclusiveDate = fromInclusiveDate,
                toInclusiveDate = toInclusiveDate,
                rawTransactionFile = transactionGroup.rawTransactionFile,
                transactionFile = transactionGroup.transactionFile,
                transactions = transactions.onEach { if (!it.categorized) AutoCat.autoCat(it, existingData.autoCatFilters) }
                    .map { Transaction(date=it.date, subcategory = it.subcategory, amount = it.amount,
                        memo=it.memo, category = it.category, categorized =  it.categorized, frequency = it.frequency, sourceCategory = it.sourceCategory) }
                    .toMutableList(),
                pivotTables = listOf(PivotCalc.pivotCategories(transactions)).toMutableList()
                )
                existingData
            }
        )
        return result!!
    }
}
