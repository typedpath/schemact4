import golambda.schemact.golambda

plugins {
	kotlin("jvm") version "1.9.25"
	id("com.typedpath.schemact4.schemact-plugin") version "1.0-SNAPSHOT"
}

group = "schemact.examples.springboot"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

schemactConfig {
  schemact = golambda
}
