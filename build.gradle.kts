import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "dev.haarolean.gnutardbot"
version = "1.0.0-SNAPSHOT"
description = "GnuTardBot"

java.sourceCompatibility = JavaVersion.VERSION_17

kotlin {
    jvmToolchain(17)
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

plugins {
    id("org.springframework.boot") version "3.2.4"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.spring") version "1.9.23"
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(libs.org.springframework.boot.spring.boot.starter.actuator)
    implementation(libs.org.springframework.boot.spring.boot.starter.validation)
    implementation(libs.org.springframework.boot.spring.boot.starter.web)

    implementation(libs.org.telegram.telegrambots.spring.boot.starter)
    implementation(libs.org.telegram.telegrambots.abilities)

//    runtimeOnly(libs.io.micrometer.micrometer.registry.prometheus)
    testImplementation(libs.org.springframework.boot.spring.boot.starter.test)

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    implementation("io.github.oshai:kotlin-logging-jvm:5.1.0")

}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}

tasks.bootJar {
    archiveFileName.set("gnutardbot.jar")
}

// Tests

tasks.withType<Test> {
    useJUnitPlatform()
}
