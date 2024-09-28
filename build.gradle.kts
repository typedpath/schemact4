import java.net.URI

plugins {
    kotlin("jvm") version "1.9.0"
    `maven-publish`
}

group = "com.typedpath"
version = "1.0.1-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("com.amazonaws:aws-java-sdk-core:1.11.574")
    implementation("com.amazonaws:aws-java-sdk-s3:1.11.574")
    implementation("com.amazonaws:aws-java-sdk-cloudformation:1.11.574")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("schemact4") {
            from(components["java"])
        }
        repositories {
            mavenLocal()
            maven {
                name = "GitHubPackages"
                url = URI("https://maven.pkg.github.com/typedpath/schemact4")
                credentials {
                    username = System.getenv("GITHUB_ACTOR")
                    password = System.getenv("GITHUB_TOKEN")
                }
            }
        }
    }
}
