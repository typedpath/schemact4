import org.gradle.configurationcache.extensions.capitalized
import schemact.domain.Domain
import schemact.domain.Schemact

object CodeLocations {
    fun handlerFullClassName(schemact: Schemact, domain: Domain, id: String) =
        "${handlerPackageName(schemact, domain)}.${handlerClassName(id)}"

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