pluginManagement {
    repositories {
        gradlePluginPortal()
        maven {
            name = "schemact4"
            url = uri("https://schemact4code.typedpath.com/repository")
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}


rootProject.name = "accountview"
include("functions")
