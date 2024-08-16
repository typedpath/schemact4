package schemact.gradleplugin

import schemact.domain.Function
import schemact.domain.Schemact
import java.io.File

open class SchemactPluginConfig {
   lateinit var schemact:Schemact
   //TODO make this a generic list of functions
   var idToFunctionJars:Map<String, File>?=null
   var uiCodeLocation: String?=null
   var functions: List<Function>?=null
  // var codeGenerationTargetDirectory: File?=null
}