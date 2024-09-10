rootProject.name = "springbootkotlin"


pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenLocal()
    }
}
include(":springbootapp")
findProject(":springbootapp")?.name = "springbootapp"
