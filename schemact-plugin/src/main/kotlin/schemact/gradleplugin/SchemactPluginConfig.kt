package schemact.gradleplugin

import schemact.domain.Schemact

open class SchemactPluginConfig {
   var testMessage: String="hi"
   lateinit var schemact:Schemact
   //TODO make this a generic list of functions
   lateinit var thumbnailerJar:String
   lateinit var uiCodeLocation: String
}