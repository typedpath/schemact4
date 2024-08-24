package schemact.gradleplugin.aws.functionTemplates

import schemact.domain.Connection
import schemact.domain.Function
import java.util.Date

fun functionTypescriptClientTemplate(packageName: String, function: Function, argsFromParams: List<Connection>,
                                     argsFromBody: List<Connection>) =
"""
// created by functionTypescriptClient at ${Date()}
namespace $packageName {

}    
""".trimIndent()