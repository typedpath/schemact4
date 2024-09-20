package schemact.domain

class Schemact(val name: String, val domains: List<Domain> = mutableListOf(),
               val entities: MutableList<Entity> = mutableListOf(),
               val staticWebsites: MutableList<StaticWebsite> = mutableListOf(),
               //TODO keep this ? - are all functions contained by modules ?
               val functions: MutableList<Function> = mutableListOf(),
               val modules: MutableList<Module> = mutableListOf(),
               val userKeyedDatabase: UserKeyedDatabase?=null,
               init: Schemact.() -> Unit = {}
) {
    fun staticWebsite(name: String, description: String, init: StaticWebsite.() -> Unit = {})
     : StaticWebsite {
        val sw = StaticWebsite(name=name, description=description, init=init)
        staticWebsites.add(sw)
        return sw
    }

    fun function(function: Function) {
        functions.add(function)
    }

    fun findModule(function: Function) : Module = modules.first { it.functions.contains(function) }

    init { init() }

}