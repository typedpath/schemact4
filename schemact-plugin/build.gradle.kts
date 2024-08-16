plugins {
    id("java")
    kotlin("jvm") version "1.9.0"
    `java-gradle-plugin`
    `maven-publish`
}

group = "com.typedpath.schemact4"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("com.typedpath:schemact4:1.0-SNAPSHOT")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.10")
    implementation("com.amazonaws:aws-java-sdk-core:1.11.574")
    implementation("com.amazonaws:aws-java-sdk-s3:1.11.574")
    implementation("com.amazonaws:aws-java-sdk-cloudformation:1.11.574")

    implementation("software.amazon.awscdk:aws-cdk-lib:2.151.0")

}

tasks.test {
    useJUnitPlatform()
}

gradlePlugin {
    plugins {
        create("schemact-plugin") {
            id = "com.typedpath.schemact4.schemact-plugin"
            implementationClass = "schemact.gradleplugin.SchemactPlugin"
        }
    }
}

/*publishing {
    publications {
        create<MavenPublication>("schemactplugin") {
            from(components["java"])
        }
        repositories {
            mavenLocal()
            //maven(url = "build/repository")
        }
    }
}
*/


