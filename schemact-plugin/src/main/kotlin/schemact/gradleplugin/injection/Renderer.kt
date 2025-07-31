package schemact.gradleplugin.injection

abstract class Renderer {
    abstract fun renderKotlin(value: Value, dependencies: Map<String, Value>) : String// = "val $name = ${transformFunction}(${dependencyNames.joinToString (",")})"
    open fun requiredImports() : List<String> = listOf()// = "val $name = ${transformFunction}(${dependencyNames.joinToString (",")})"
    open fun renderKotlinDependencies() : Map<String, String> = mapOf()
}