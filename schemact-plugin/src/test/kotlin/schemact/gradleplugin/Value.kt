package schemact.gradleplugin

import schemact.domain.Connection

class Value (val connectionFrom: Connection, var requirements: List<Value.Requirement>? = null,
             var renderer: schemact.gradleplugin.Renderer? = null) {
    var varName = connectionFrom.name
    class Requirement(val name: String, val match:  (Value)-> Boolean, val creator: ()->Value)
}