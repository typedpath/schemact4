package schemact.gradleplugin

import schemact.domain.Function
import schemact.domain.Module
import schemact.domain.Schemact
import schemact.domain.StaticWebsite
import java.io.File

open class SchemactPluginConfig {
   lateinit var schemact:Schemact
   var uiCodeBuildLocation: String?=null
   var module:Module?=null
   var staticWebSiteToSourceRoot: Map<StaticWebsite, File>? = null
}