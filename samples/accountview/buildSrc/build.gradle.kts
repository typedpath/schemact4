plugins {
    kotlin("jvm") version "1.9.0"
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
    /*maven {
        name = "schemact4"
        url = uri("https://schemact4code.typedpath.com/repository")
    }*/
}

dependencies {
    implementation("com.typedpath:schemact4:1.0.1")
}

tasks.test {
    useJUnitPlatform()
}

