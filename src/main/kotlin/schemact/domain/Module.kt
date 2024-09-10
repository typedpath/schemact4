package schemact.domain


class Module(val name: String, val version:String,
             val type: Type = Type.StandaloneFunction,
             val functions: MutableList<Function> = mutableListOf()) {
    enum class Type {
        StandaloneFunction, SpringBootApplication
    }
}