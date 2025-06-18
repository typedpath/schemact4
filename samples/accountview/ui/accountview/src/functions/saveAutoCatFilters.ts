
// created by functionTypescriptClientTemplate
import axios, { AxiosResponse } from "axios";
import { UserInfo } from './UserInfo'; 

//namespace org.testedsoftware.accountview {

const urlPath = "/functions/saveAutoCatFilters" 

export default async function saveAutoCatFilters(autoCatFilters_in: AutoCatFilter[], Authorization_in: string) : Promise<AxiosResponse<UserInfo, any>> { // TODO map to specified return type
    let url = urlPath
    if (window.location.href.indexOf("localhost")>=0) {
      url = 'https://accountview.testedsoftware.org' + urlPath
    }
    const headers: {[key: string]: string} = { 'Content-Type': 'text/plain',    }
    
        headers['Authorization']=Authorization_in;

      
    let body = {autoCatFilters: autoCatFilters_in}; 

    
       let res = await axios.post(url, body, {headers : headers,
       params: { }

     });
        console.log('res:', res)
        return res;
     }       


export interface AutoCatFilter   {
     name: string
    pattern: string
    type: string
    category: string
    frequency: string
    sourceCategory: string
     } 



//}
