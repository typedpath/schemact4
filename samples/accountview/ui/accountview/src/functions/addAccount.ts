
// created by functionTypescriptClientTemplate
import axios, { AxiosResponse } from "axios";
import { UserInfo } from './UserInfo'; 

//namespace org.testedsoftware.accountview {

const urlPath = "/functions/addAccount" 

export default async function addAccount(account_in: Account, Authorization_in: string) : Promise<AxiosResponse<UserInfo, any>> { // TODO map to specified return type
    let url = urlPath
    if (window.location.href.indexOf("localhost")>=0) {
      url = 'https://accountview.testedsoftware.org' + urlPath
    }
    const headers: {[key: string]: string} = { 'Content-Type': 'text/plain',    }
    
        headers['Authorization']=Authorization_in;

      
    let body = {account: account_in}; 

    
       let res = await axios.post(url, body, {headers : headers,
       params: { }

     });
        console.log('res:', res)
        return res;
     }       


export interface Account   {
     name: string
    sortCode: string
    accountNumber: string
    transactionGroups:  {
         fromInclusiveDate: string
        toInclusiveDate: string
        rawTransactionFile:  {
             filename: string
            location: string
            contentType: string
            uploadTime: string
             } 
        transactions:  {
             date: string
            subcategory: string
            amount: number
            memo: string
            category: string
            frequency: string
            sourceCategory: string
            categorized: boolean
             } []
         } []
     } 



//}
