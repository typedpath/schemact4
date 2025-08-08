package schemact.gradleplugin.aws.functiontemplates

import schemact.domain.*
import schemact.domain.Function
import schemact.gradleplugin.injection.ParameterDependencyGraph

object FunctionTypescriptClientInjectedTemplate {
    fun functionTypescriptClientInjectedTemplate(
        packageName: String, module: Module, function: Function, parameterDependencyGraph: ParameterDependencyGraph,
        defaultLocalServerDomain: String?
    ): String {
        val argsFromHeader = parameterDependencyGraph.restHeaderRequirements.map { it.connectionFrom }.toList()
        val argsFromMultiPart = parameterDependencyGraph.restMultiPartBodyParamRequirements.map { it.connectionFrom }.toList()
        val argsFromBody = parameterDependencyGraph.restBodyParamRequirements.map { it.connectionFrom }.toList()
        val argsFromParams = parameterDependencyGraph.restUrlParamRequirements.map { it.connectionFrom }.toList()
        val useMultiPart = argsFromMultiPart.size > 0
        val allArgs: List<Connection> = argsFromParams
            .plus ( argsFromBody )
            .plus ( argsFromMultiPart)
            .plus( argsFromHeader)
        val allArgsInOrder = function.paramType.connections.filter{allArgs.contains(it)}.plus(allArgs.filter {!function.paramType.connections.contains(it)})
        return """
// created by functionTypescriptClientTemplate
import axios, { AxiosResponse } from "axios";
${if (!function.returnType.isValueType) "import { ${function.returnType.name} } from './${function.returnType.name}';" else ""} 

//namespace $packageName {

const urlPath = "/${module.name}/${function.name}" 

export default async function ${function.name}(${
            allArgsInOrder.joinToString(", ") {
                "${functionArgName(it)}: ${
                    typescriptType(
                        it
                    )
                }"
            }
        }) : Promise<AxiosResponse<${function.returnType.name}, any>> { // TODO map to specified return type
    let url = urlPath
    if (window.location.href.indexOf("localhost")>=0) {
      url = 'https://${if (defaultLocalServerDomain == null) "specifydefaultLocalClientDomain" else defaultLocalServerDomain}' + urlPath
    }
    const headers: {[key: string]: string} = { 'Content-Type': 'text/plain',    }
    
    ${
            argsFromHeader.map {
                """    headers['${it.name}']=${functionArgName(it)};"""
            }.joinToString(System.lineSeparator())
        }

      ${
            if (useMultiPart) {
                """ 
    headers['Content-Type'] = 'multipart/form-data';     
    const body = new FormData();
      ${
                    argsFromMultiPart.map {
                        " body.append('${it.name}', ${
                            functionArgName(
                                it
                            )
                        });"
                    }.joinToString(System.lineSeparator())
                }
"""
            } else {
                """
    let body = {${argsFromBody.joinToString(",") { "${it.name}: ${functionArgName(it)}" }}}; 
"""
            }
        }
${
            argsFromParams.joinToString(System.lineSeparator()) {
                """    let ${it.name} = ${functionArgName(it)};"""
            }
        }    
       let res = await axios.post(url, body, {headers : headers,
       params: { ${argsFromParams.joinToString(", ") { it.name }}}

     });
        console.log('res:', res)
        return res;
     }       

${
            allArgs.filter { it.entity2 !is PrimitiveType && it.entity2 !is ReactJsInjectables.File }
                .joinToString(System.lineSeparator()) { interfaceDef(it.entity2) }
        }


//}
"""
    }

    private fun typescriptType(connection: Connection): String =
        "${typescriptType(connection.entity2)}${if (connection.cardinality== Cardinality.OneToMany)"[]" else ""}"

    private fun typescriptType(entity: Entity): String =
        if (entity is PrimitiveType) {
            entity.typescriptName
        } else {
           entity.name
        }

    fun interfaceDef(entity: Entity) =
"""
export interface ${entity.name}  ${interfaceFieldsDef(entity, "    ")}
"""
    private fun interfaceFieldsDef(entity: Entity, indent: String) = " {${System.lineSeparator()} ${
        entity.connections.joinToString(System.lineSeparator()) { "$indent${it.name}: ${propertyTypeDef(it, indent)}" }}${System.lineSeparator()}${indent} } "

    private fun entityTypeDef(entity: Entity, indent: String) : String {
        return if (entity is PrimitiveType) {
            entity.typescriptName
        } else {
            interfaceFieldsDef(entity, "$indent    ")
        }
    }

    private fun propertyTypeDef(connection: Connection, indent: String): String =
          "${entityTypeDef(connection.entity2, indent)}${if (connection.cardinality==Cardinality.OneToMany)"[]" else ""}"


private fun functionArgName(connection: Connection) = "${connection.name}_in"

}


