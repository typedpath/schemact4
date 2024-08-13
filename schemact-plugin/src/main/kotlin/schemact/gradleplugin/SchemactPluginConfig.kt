package schemact.gradleplugin

import schemact.domain.Schemact
import java.io.File

open class SchemactPluginConfig {
   lateinit var schemact:Schemact
   //TODO make this a generic list of functions
   lateinit var idToFunctionJars:Map<String, File>
   lateinit var uiCodeLocation: String
}