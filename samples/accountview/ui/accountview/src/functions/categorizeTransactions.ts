
// created by functionTypescriptClientTemplate
import axios, { AxiosResponse } from "axios";
import { TransactionGroup } from './TransactionGroup'; 

//namespace org.testedsoftware.accountview {

const urlPath = "/functions/categorizeTransactions" 

export default async function categorizeTransactions(fromInclusiveDate_in: string, toInclusiveDate_in: string, accountNumber_in: string, transactions_in: Transaction, Authorization_in: string) : Promise<AxiosResponse<TransactionGroup, any>> { // TODO map to specified return type
    let url = urlPath
    if (window.location.href.indexOf("localhost")>=0) {
      url = 'https://accountview.testedsoftware.org' + urlPath
    }
    const headers: {[key: string]: string} = { 'Content-Type': 'text/plain',    }
    
        headers['Authorization']=Authorization_in;

      
    let body = {transactions: transactions_in}; 

    let fromInclusiveDate = fromInclusiveDate_in;
    let toInclusiveDate = toInclusiveDate_in;
    let accountNumber = accountNumber_in;    
       let res = await axios.post(url, body, {headers : headers,
       params: { fromInclusiveDate, toInclusiveDate, accountNumber}

     });
        console.log('res:', res)
        return res;
     }       


export interface Transaction   {
     date: string
    subcategory: string
    amount: number
    memo: string
    category: string
    frequency: string
    sourceCategory: string
    categorized: boolean
     } 



//}
