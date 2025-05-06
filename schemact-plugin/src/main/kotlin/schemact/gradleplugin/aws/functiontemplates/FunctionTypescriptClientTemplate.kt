package schemact.gradleplugin.aws.functiontemplates

import schemact.domain.*
import schemact.domain.Function
import schemact.gradleplugin.RestPolicy

object FunctionTypescriptClientTemplate {
    fun functionTypescriptClientTemplate(
        packageName: String, module: Module, function: Function, restPolicy: RestPolicy,
        defaultLocalServerDomain: String?
    ): String {
        val allArgs = restPolicy.argsFromParams.toMutableList()
        allArgs.addAll(restPolicy.argsFromBody)
        allArgs.addAll(restPolicy.argsFromHeader)
        allArgs.addAll(restPolicy.argsFromMultiPart)
        return """
// created by functionTypescriptClientTemplate
import axios, { AxiosResponse } from "axios";

//namespace $packageName {

const urlPath = "/${module.name}/${function.name}" 

export default async function ${function.name}(${
            allArgs.joinToString(", ") {
                "${functionArgName(it)}: ${
                    typescriptType(
                        it.entity2
                    )
                }"
            }
        }) : Promise<AxiosResponse<any, any>> { // TODO map to specified return type
    let url = urlPath
    if (window.location.href.indexOf("localhost")>=0) {
      url = 'https://${if (defaultLocalServerDomain==null) "specifydefaultLocalClientDomain" else defaultLocalServerDomain }' + urlPath
    }
    const headers: {[key: string]: string} = { 'Content-Type': 'text/plain',    }
    
    ${restPolicy.argsFromHeader.map { 
"""    headers['${it.name}']=${functionArgName(it)};""" }.joinToString(System.lineSeparator())}

      ${ if (restPolicy.useMultiPart) {
""" 
    headers['Content-Type'] = 'multipart/form-data';     
    const body = new FormData();
      ${ restPolicy.argsFromMultiPart.map {" body.append('${it.name}', ${functionArgName(it)});"}.joinToString (System.lineSeparator())}
"""            
        } else {"""
    let body = {${restPolicy.argsFromBody.joinToString(",") { "${it.name}: ${functionArgName(it)}" }}}; 
"""}}
${
            restPolicy.argsFromParams.joinToString(System.lineSeparator()) {
                """    let ${it.name} = ${functionArgName(it)};"""
            }
        }    
       let res = await axios.post(url, body, {headers : headers,
       params: { ${restPolicy.argsFromParams.joinToString(", ") { it.name }}}

     });
        return res;
     }       

${allArgs.filter { it.entity2 !is PrimitiveType && it.entity2 !is ReactJsInjectables.File }.joinToString(System.lineSeparator()) { interfaceDef(it.entity2) }}


//}
"""
    }


    private fun typescriptType(entity: Entity): String =
        if (entity is PrimitiveType) {
            entity.typescriptName
        } else {
           entity.name
        }

    private fun interfaceDef(entity: Entity) =
"""
export interface ${entity.name}  ${interfaceFieldsDef(entity, "    ")}
"""
    private fun interfaceFieldsDef(entity: Entity, indent: String) = " {${System.lineSeparator()} ${
        entity.connections.joinToString(System.lineSeparator()) { "$indent${it.name}: ${entityTypeDef(it.entity2, indent)}" }}${System.lineSeparator()}${indent} } "

    private fun entityTypeDef(entity: Entity, indent: String) : String {
        return if (entity is PrimitiveType) {
            entity.typescriptName
        } else {
            interfaceFieldsDef(entity, "$indent    ")
        }
    }

private fun functionArgName(connection: Connection) = "${connection.name}_in"

}


