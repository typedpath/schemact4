package schemact.gradleplugin

import schemact.domain.Function
import schemact.domain.Schemact
import schemact.domain.StaticWebsite
import java.io.File

open class SchemactPluginConfig {
   lateinit var schemact:Schemact
   //TODO make this a generic list of functions
   var functionToFunctionJars: Map<Function, File>?=null
   var uiCodeBuildLocation: String?=null
   var functions: List<Function>?=null
   var staticWebSiteToSourceRoot: Map<StaticWebsite, File>? = null
}