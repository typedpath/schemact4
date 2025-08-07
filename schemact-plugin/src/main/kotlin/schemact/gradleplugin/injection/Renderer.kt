package schemact.gradleplugin.injection

abstract class Renderer {
    abstract fun renderKotlin(value: Value, dependencies: Map<String, Value>) : String// = "val $name = ${transformFunction}(${dependencyNames.joinToString (",")})"
}