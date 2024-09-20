import golambda.schemact.externalTestModule
import golambda.schemact.golambda

plugins {
    id("java")
    kotlin("jvm")
    id("com.typedpath.schemact4.schemact-plugin") version "1.0-SNAPSHOT"
    kotlin("plugin.serialization") version "2.0.20"
}

group = "schemact.examples.springboot"
version = "0.0.1-SNAPSHOT"

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

schemactConfig {
    schemact = golambda
    module = externalTestModule
}
