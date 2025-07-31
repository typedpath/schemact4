package schemact.aws
// dataClass topLevelTypes: CognitoClientDetails-details to connect to cognito-314100910, UserInfo-UserInfo-1088447144, File-Random Uploaded File-1411563086, Account-accounts-1115918538, TransactionGroup-Transaction Group-378206600, VerifiedCognitoUser-user details from cognito or whatever-1430011091, TransactionUpdate-Transaction Update-338508020, AutoCatFilter-AutoCatFilter-1249312635, Transaction-Transaction-143763428, PivotTable-PivotTable-78259378
import com.fasterxml.jackson.annotation.JsonProperty


    // dataClassSanPackage VerifiedCognitoUser visited: CognitoClientDetails,UserInfo,File,Account,TransactionGroup,TransactionUpdate,AutoCatFilter,Transaction,PivotTable,VerifiedCognitoUser
// create from template DataClassTemplate
// version= 0    
data class VerifiedCognitoUser(@JsonProperty("sub") var sub:  String, @JsonProperty("email") var email:  String, @JsonProperty("username") var username:  String)  
    