package schemact.gradleplugin.injection.mappers
fun getResourceAsText(root: Any, path: String): String {
    return getResourceAsText(root.javaClass, path)
}

fun <T> getResourceAsText(rootClass: Class<T>, path: String): String {
    val resource = rootClass.getResource(path)
    return resource?.readText()?:throw Error("Resource '$path' not found")
}