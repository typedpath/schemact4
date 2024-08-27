package schemact.gradleplugin.aws.functionTemplates

import schemact.domain.Connection
import schemact.domain.Entity
import schemact.domain.Function
import schemact.domain.PrimitiveType

fun functionTypescriptClientTemplate(packageName: String, function: Function, argsFromParams: List<Connection>,
                                     argsFromBody: List<Connection>) : String {
    val allArgs = argsFromParams.toMutableList()
    allArgs.addAll(argsFromBody)
    if (argsFromBody.size>1) {
          throw RuntimeException("TODO function:${function.name} deal with more than 1 arg for body")
    }
    val argFromBody = argsFromBody.firstOrNull()
   return  """
// created by functionTypescriptClientTemplate
import axios from "axios";

//namespace $packageName {

const urlPath = "/functions/${function.name}" 

export default async function ${function.name}(${allArgs.joinToString(", "){"${it.name}_in: ${expectTypescriptType(it.entity2)}"}}) : Promise<${expectTypescriptType(function.returnType)}> {
    let url = urlPath
    if (window.location.href.indexOf("localhost")>=0) {
      url = 'https://mydevdomain' + urlPath
    }
    let body = ${if (argFromBody!=null) (argFromBody.name+"_in") else ""}
${argsFromParams.joinToString(System.lineSeparator()) {
"""    let ${it.name} = ${it.name}_in;"""  }}    
       let res = await axios.post(url, body, {headers : {
         'Content-Type': 'text/plain',
       },
       params: { ${argsFromParams.joinToString(", ") { it.name }}}

     });
        return ""+res.data;
     }       

//}
"""
}



private fun expectTypescriptType( entity: Entity) : String{
    if (entity is PrimitiveType) {
        return entity.typescriptName
    } else {
        throw RuntimeException("functionTypescriptClientTemplate expectKotlinType only deals with primitive types")
    }
}


