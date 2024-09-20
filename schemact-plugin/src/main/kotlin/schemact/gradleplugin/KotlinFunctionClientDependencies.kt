package schemact.gradleplugin

object KotlinFunctionClientDependencies {
    val ktor_version="2.3.12"
    val kotlinFunctionClientDependencies = listOf(
        "io.ktor:ktor-client-core:$ktor_version",
        "io.ktor:ktor-client-cio:$ktor_version",
        "io.ktor:ktor-client-content-negotiation:$ktor_version",
        "io.ktor:ktor-serialization-kotlinx-json:$ktor_version"
    )
}
