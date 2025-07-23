package schemact.gradleplugin.injection

import schemact.domain.Connection

class Value (val connectionFrom: Connection, var requirements: List<Value.Requirement>? = null,
             var renderer: Renderer? = null) {
    var varName = connectionFrom.name
    class Requirement(val name: String, val match:  (Value)-> Boolean, val creator: ()->Value)
}