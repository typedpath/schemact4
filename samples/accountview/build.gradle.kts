import samples.accountview.accountview

plugins {
    kotlin("jvm") version "2.0.0"
    id("com.typedpath.schemact4.schemact-plugin") version "1.0.1"
}

group = "com.typedpath"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
/*    maven {
        name = "schemact4"
        url = uri("https://schemact4code.typedpath.com/repository")
    }*/
}


dependencies {
    implementation("com.typedpath:schemact4:1.0.1-SNAPSHOT")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}

schemactConfig {
    schemact = accountview
}

