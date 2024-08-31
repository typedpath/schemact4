package schemact.gradleplugin.aws.functiontemplates

import schemact.domain.Connection
import schemact.domain.Entity
import schemact.domain.Function
import schemact.domain.PrimitiveType

object FunctionTypescriptClientTemplate {
    fun functionTypescriptClientTemplate(
        packageName: String, function: Function, argsFromParams: List<Connection>,
        argsFromBody: List<Connection>
    ): String {
        val allArgs = argsFromParams.toMutableList()
        allArgs.addAll(argsFromBody)
        return """
// created by functionTypescriptClientTemplate
import axios from "axios";

//namespace $packageName {

const urlPath = "/functions/${function.name}" 

export default async function ${function.name}(${
            allArgs.joinToString(", ") {
                "${it.name}_in: ${
                    typescriptType(
                        it.entity2
                    )
                }"
            }
        }) : Promise<${typescriptType(function.returnType)}> {
    let url = urlPath
    if (window.location.href.indexOf("localhost")>=0) {
      url = 'https://mydevdomain' + urlPath
    }
    let body = {${argsFromBody.joinToString(",") { "${it.name}: ${it.name}_in" }}}; 
${
            argsFromParams.joinToString(System.lineSeparator()) {
                """    let ${it.name} = ${it.name}_in;"""
            }
        }    
       let res = await axios.post(url, body, {headers : {
         'Content-Type': 'text/plain',
       },
       params: { ${argsFromParams.joinToString(", ") { it.name }}}

     });
        return ""+res.data;
     }       

${allArgs.filter { it.entity2 !is PrimitiveType }.joinToString(System.lineSeparator()) { interfaceDef(it.entity2) }}


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

            /**
     * export interface OpenGraphTagging {
     *    title: string,
     *    image: {
     *            url: string, width: number
     *           }
     * }
     */

}


