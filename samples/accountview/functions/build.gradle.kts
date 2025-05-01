import samples.accountview.accountview
import samples.accountview.functionsModule
import samples.accountview.mainPage

plugins {
    kotlin("jvm") version "2.0.0"
    id("com.typedpath.schemact4.schemact-plugin") version "1.0.1-SNAPSHOT"
}

group = "com.typedpath"
version = "1.0-SNAPSHOT"

schemactConfig {
    schemact= accountview
    module = functionsModule
    staticWebSiteToSourceRoot =  mapOf ( mainPage to File("${project.projectDir}/../ui/accountview/src"))
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}