package schemact.react
// dataClass topLevelTypes: CognitoClientDetails-details to connect to cognito-1655624171, UserInfo-UserInfo-570884259, File-Random Uploaded File-168937600, Account-accounts-1739174897, TransactionGroup-Transaction Group-1931216742, VerifiedCognitoUser-user details from cognito or whatever-1761874878, TransactionUpdate-Transaction Update-1834320854, AutoCatFilter-AutoCatFilter-1204388790, Transaction-Transaction-1803833630, PivotTable-PivotTable-293438309
import com.fasterxml.jackson.annotation.JsonProperty


    // dataClassSanPackage File visited: CognitoClientDetails,UserInfo,Account,TransactionGroup,VerifiedCognitoUser,TransactionUpdate,AutoCatFilter,Transaction,PivotTable,File
// create from template DataClassTemplate
// version= 0    
data class File(@JsonProperty("filename") var filename:  String, @JsonProperty("content") var content:  ByteArray, @JsonProperty("contentType") var contentType:  String)  
    