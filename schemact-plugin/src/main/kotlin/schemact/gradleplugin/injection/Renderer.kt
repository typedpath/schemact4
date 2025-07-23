package schemact.gradleplugin.injection

abstract class Renderer {
    abstract fun renderKotlin(value: Value, dependencies: Map<String, Value>) : String// = "val $name = ${transformFunction}(${dependencyNames.joinToString (",")})"
    abstract fun requiredImports() : List<String>// = "val $name = ${transformFunction}(${dependencyNames.joinToString (",")})"
    fun transformFunction() : MapperFunction? =null
}