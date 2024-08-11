package schemact.domain

class Schemact(val domains: List<Domain> = mutableListOf(),
               val entities: MutableList<Entity> = mutableListOf(),
               val staticWebsites: MutableList<StaticWebsite> = mutableListOf(),
               val functions: MutableList<Function> = mutableListOf(),
               val userKeyedDatabase: UserKeyedDatabase?=null,
               init: Schemact.() -> Unit = {}
) {
    fun staticWebsite(name: String, description: String) : StaticWebsite {
        val sw = StaticWebsite(name=name, description=description)
        staticWebsites.add(sw)
        return sw
    }

    fun function(function: Function) {
        functions.add(function)
    }

    init { init() }

}