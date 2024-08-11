plugins {
    kotlin("jvm") version "1.9.0"
    `maven-publish`
}

group = "com.typedpath"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}

publishing {
    publications {
        create<MavenPublication>("schemact4") {
            from(components["java"])
        }
        repositories {
            mavenLocal()
            //maven(url = "build/repository")
        }
    }
}
