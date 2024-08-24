package schemact.domain

class StaticWebsite(val name: String, val description: String, val functionClients: List<FunctionClient> = mutableListOf(),
                    init: StaticWebsite.() -> Unit = {}) {
    class BucketName : StringType(maxLength = 100) {
        init {
                isFromInfrastructure = true
        }
    }

    init {
        init()
    }

    fun client(function: Function, language: Language) {
        (functionClients as MutableList).add(FunctionClient(function, language))
    }

}

