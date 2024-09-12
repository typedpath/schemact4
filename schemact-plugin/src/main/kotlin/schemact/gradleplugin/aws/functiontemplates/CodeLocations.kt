package schemact.gradleplugin.aws.functiontemplates
import org.gradle.configurationcache.extensions.capitalized
import schemact.domain.Domain
import schemact.domain.Module
import schemact.domain.Schemact

object CodeLocations {
    fun handlerFullClassName(schemact: Schemact, module:Module, domain: Domain, id: String) =
        when (module.type) {
            Module.Type.StandaloneFunction -> "${handlerPackageName(schemact, domain)}.${handlerClassName(id)}"
            Module.Type.GoStandaloneFunction -> "bootstrap"
            Module.Type.SpringBootApplication -> throw RuntimeException("TODO module type ${module.type}")
        }

    fun handlerClassName(id: String) = "${id.capitalized()}Handler"
    fun interfaceClassName(id: String) = "${id.capitalized()}Handler"
    fun implClassName(id: String) = "${id.capitalized()}Impl"

    fun handlerPackageName(schemact: Schemact, domain: Domain) =
        "${packageTree(domain, schemact).joinToString (".")}"

    fun packageTree (domain: Domain, schemact: Schemact) : List<String> {
        val packageTree = domain.name.split(".").reversed().toMutableList()
        packageTree.add(schemact.name)
        return packageTree
    }

}