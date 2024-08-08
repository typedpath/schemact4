package domain

class Schemact(val domains: List<Domain> = mutableListOf(),
               val entities: MutableList<Entity> = mutableListOf(),
               val staticWebsites: MutableList<StaticWebsite> = mutableListOf(),
               val functions: MutableList<Function> = mutableListOf(),
               init: Schemact.() -> Unit = {}
) {
    fun staticWebsite(name: String, description: String) {
        staticWebsites.add(StaticWebsite(name=name, description=description))
    }

    init { init() }

}