package schemact.domain


class Module(val name: String, val version:String,
             val type: Type = Type.StandaloneFunction,
             val functions: MutableList<Function> = mutableListOf(),
             val functionClients: List<FunctionClient> = mutableListOf(),
             init: Module.()->Unit = {}) {
    init { init(this) }

    enum class Type {
        StandaloneFunction, SpringBootApplication, GoStandaloneFunction
    }

    fun client(function: Function, language: Language) {
        (functionClients as MutableList).add(FunctionClient(function, language))
    }
}