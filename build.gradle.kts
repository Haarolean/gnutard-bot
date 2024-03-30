group = "dev.haarolean.gnutardbot"
version = "0.0.1-SNAPSHOT"
description = "GnuTardBot"
java.sourceCompatibility = JavaVersion.VERSION_17

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

plugins {
    `java-library`
    `maven-publish`
    id("org.springframework.boot") version "3.2.4"
    id("io.spring.dependency-management") version "1.1.4"
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(libs.org.springframework.boot.spring.boot.starter.actuator)
    implementation(libs.org.springframework.boot.spring.boot.starter.validation)
    implementation(libs.org.springframework.boot.spring.boot.starter.web)

    compileOnly(libs.org.projectlombok.lombok)
    annotationProcessor(libs.org.projectlombok.lombok)

    implementation(libs.org.telegram.telegrambots.spring.boot.starter)
    implementation(libs.org.telegram.telegrambots.abilities)

    testImplementation(libs.org.springframework.boot.spring.boot.starter.test)
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc>() {
    options.encoding = "UTF-8"
}
