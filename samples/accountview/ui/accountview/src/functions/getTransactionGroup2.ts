
// created by functionTypescriptClientTemplate
import axios, { AxiosResponse } from "axios";
import { TransactionGroup } from './TransactionGroup'; 

//namespace org.testedsoftware.accountview {

const urlPath = "/functions/getTransactionGroup2" 

export default async function getTransactionGroup2(fromInclusiveDate_in: string, toInclusiveDate_in: string, accountNumber_in: string, token_in: string) : Promise<AxiosResponse<TransactionGroup, any>> { // TODO map to specified return type
    let url = urlPath
    if (window.location.href.indexOf("localhost")>=0) {
      url = 'https://accountview.testedsoftware.org' + urlPath
    }
    const headers: {[key: string]: string} = { 'Content-Type': 'text/plain',    }
    
        headers['token']=token_in;

      
    let body = {}; 

    let accountNumber = accountNumber_in;
    let toInclusiveDate = toInclusiveDate_in;
    let fromInclusiveDate = fromInclusiveDate_in;    
       let res = await axios.post(url, body, {headers : headers,
       params: { accountNumber, toInclusiveDate, fromInclusiveDate}

     });
        console.log('res:', res)
        return res;
     }       




//}
