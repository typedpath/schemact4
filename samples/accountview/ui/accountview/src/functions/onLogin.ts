
// created by functionTypescriptClientTemplate
import axios from "axios";

//namespace org.testedsoftware.accountview {

const urlPath = "/functions/onLogin" 

export default async function onLogin(Authorization_in: string) : Promise<string> {
    let url = urlPath
    if (window.location.href.indexOf("localhost")>=0) {
      url = 'https://accountview.testedsoftware.org' + urlPath
    }
    const headers: {[key: string]: string} = { 'Content-Type': 'text/plain',    }
    
        headers['Authorization']=Authorization_in;

    let body = {}; 
    
       let res = await axios.post(url, body, {headers : headers,
       params: { }

     });
        return ""+res.data;
     }       




//}
