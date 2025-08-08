
// created by functionTypescriptClientTemplate
import axios, { AxiosResponse } from "axios";
import { UserInfo } from './UserInfo'; 

//namespace org.testedsoftware.accountview {

const urlPath = "/functions/uploadTransactionGroup" 

export default async function uploadTransactionGroup(Authorization_in: string, file_in: File, fromInclusiveDate_in: string, toInclusiveDate_in: string, accountNumber_in: string) : Promise<AxiosResponse<UserInfo, any>> { // TODO map to specified return type
    let url = urlPath
    if (window.location.href.indexOf("localhost")>=0) {
      url = 'https://accountview.testedsoftware.org' + urlPath
    }
    const headers: {[key: string]: string} = { 'Content-Type': 'text/plain',    }
    
        headers['Authorization']=Authorization_in;

       
    headers['Content-Type'] = 'multipart/form-data';     
    const body = new FormData();
       body.append('accountNumber', accountNumber_in);
 body.append('toInclusiveDate', toInclusiveDate_in);
 body.append('fromInclusiveDate', fromInclusiveDate_in);
 body.append('file', file_in);

    
       let res = await axios.post(url, body, {headers : headers,
       params: { }

     });
        console.log('res:', res)
        return res;
     }       




//}
