
// created by functionTypescriptClientTemplate
import axios, { AxiosResponse } from "axios";
import { UserInfo } from './UserInfo'; 

//namespace org.testedsoftware.accountview {

const urlPath = "/functions/saveCategories" 

export default async function saveCategories(Authorization_in: string, categories_in: string[]) : Promise<AxiosResponse<UserInfo, any>> { // TODO map to specified return type
    let url = urlPath
    if (window.location.href.indexOf("localhost")>=0) {
      url = 'https://accountview.testedsoftware.org' + urlPath
    }
    const headers: {[key: string]: string} = { 'Content-Type': 'text/plain',    }
    
        headers['Authorization']=Authorization_in;

      
    let body = {categories: categories_in}; 

    
       let res = await axios.post(url, body, {headers : headers,
       params: { }

     });
        console.log('res:', res)
        return res;
     }       




//}
