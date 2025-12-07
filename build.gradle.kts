import org.gradle.kotlin.dsl.support.kotlinCompilerOptions

plugins {
	kotlin("jvm") version "2.2.0"
	kotlin("plugin.spring") version "2.2.0"
	id("org.springframework.boot") version "3.5.7"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.respiroc.greg"
version = "0.0.1-SNAPSHOT"
description = "FullstackDeveloperProject"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(24)
	}
}
kotlin {
    jvmToolchain(24)
}


repositories {
	mavenCentral()
}

dependencies {
	// Kotlin
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib")
	
	// Spring Boot
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("org.springframework.boot:spring-boot-starter-cache")
	
	// Jackson Kotlin module for Redis serialization
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
		
	// HTMX
	implementation("io.github.wimdeblauwe:htmx-spring-boot-thymeleaf:4.0.1")
		
	// Database
	implementation("org.flywaydb:flyway-core")
	implementation("org.flywaydb:flyway-database-postgresql")
	runtimeOnly("org.postgresql:postgresql")
		
	// Testing
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	// Spring Boot + Testcontainers integration
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	// Testcontainers for JUnit 5 and PostgreSQL
	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("org.testcontainers:postgresql")
	// Mockito Kotlin for unit tests
	testImplementation("org.mockito.kotlin:mockito-kotlin:5.3.1")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    //Redis testcontainer
    testImplementation("com.redis:testcontainers-redis")
}



tasks.withType<Test> {
	useJUnitPlatform()
}
