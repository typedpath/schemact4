
// created by functionTypescriptClientTemplate
import axios, { AxiosResponse } from "axios";

//namespace org.testedsoftware.accountview {

const urlPath = "/functions/uploadFile" 

export default async function uploadFile(Authorization_in: string, file_in: File) : Promise<AxiosResponse<any, any>> { // TODO map to specified return type
    let url = urlPath
    if (window.location.href.indexOf("localhost")>=0) {
      url = 'https://accountview.testedsoftware.org' + urlPath
    }
    const headers: {[key: string]: string} = { 'Content-Type': 'text/plain',    }
    
        headers['Authorization']=Authorization_in;

       
    headers['Content-Type'] = 'multipart/form-data';     
    const body = new FormData();
       body.append('file', file_in);

    
       let res = await axios.post(url, body, {headers : headers,
       params: { }

     });
        return res;
     }       




//}
