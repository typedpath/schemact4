plugins {
    kotlin("jvm") version "1.9.0"
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("com.typedpath:schemact4:1.0-SNAPSHOT")
}

tasks.test {
    useJUnitPlatform()
}

