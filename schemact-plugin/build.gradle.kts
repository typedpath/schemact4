plugins {
    id("java")
    kotlin("jvm") version "1.9.0"
    `java-gradle-plugin`
    `maven-publish`
}

group = "com.typedpath.schemact4"
version = "1.0.2-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("com.typedpath:schemact4:1.0.1-SNAPSHOT")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.10")
    implementation("com.amazonaws:aws-java-sdk-core:1.11.574")
    implementation("com.amazonaws:aws-java-sdk-s3:1.11.574")
    implementation("com.amazonaws:aws-java-sdk-cloudformation:1.11.574")

    implementation("software.amazon.awscdk:aws-cdk-lib:2.151.0")

    // these are copied from AwsDependencies.kt
    implementation("com.amazonaws:aws-lambda-java-core:1.2.1")
    implementation("com.amazonaws:aws-lambda-java-events:3.11.0")
    implementation("com.amazonaws:aws-java-sdk-s3:1.11.574")
    implementation("com.amazonaws:aws-java-sdk-dynamodb:1.12.777")
    /*
    Recommended Dependency: Add software.amazon.awssdk:cognito-jwt-verifier
                      (or its predecessor com.amazonaws:aws-jwt-verify for compatibility with your older SDK dependencies).
     */
    //,"software.amazon.awssdk:cognito-jwt-verifier:2.21.0" // Latest compatible with AWS SDK v2
    implementation("com.auth0:java-jwt:4.4.0")
    implementation("com.auth0:jwks-rsa:0.22.1")
    implementation("commons-fileupload:commons-fileupload:1.5")


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


