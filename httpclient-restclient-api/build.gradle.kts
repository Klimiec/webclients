import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    implementation("org.apache.httpcomponents.client5:httpclient5:5.3")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
    implementation("io.micrometer:micrometer-registry-prometheus:1.11.4")
    implementation(project(mapOf("path" to ":httpclient-webclientinterface")))

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.1")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.github.tomakehurst:wiremock-jre8-standalone:2.35.1")
    testImplementation("io.kotest:kotest-assertions-core-jvm:5.8.0")
}

plugins {
    id("org.springframework.boot") version "3.2.1"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.spring") version "1.9.22"
    id("org.jlleitschuh.gradle.ktlint") version "11.6.1"
    id("org.jlleitschuh.gradle.ktlint-idea") version "11.6.1"
}

group = "com.dev.sandbox.restclient"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// https://docs.gradle.org/current/userguide/java_testing.html#sec:configuring_java_integration_tests
sourceSets {
    create("integration") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

val integrationImplementation by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}

configurations["integrationRuntimeOnly"].extendsFrom(configurations.runtimeOnly.get())

val integrationTest = task<Test>("integrationTest") {
    description = "Runs integration tests."
    group = "verification"

    testClassesDirs = sourceSets["integration"].output.classesDirs
    classpath = sourceSets["integration"].runtimeClasspath
    shouldRunAfter("test")

    useJUnitPlatform()

    testLogging {
        events("passed")
    }
}

val unitTest = task<Test>("unitTest") {
    useJUnitPlatform()
    testLogging {
        events("passed")
    }
}

tasks.check { dependsOn(integrationTest, unitTest) }

ktlint {
    version.set("0.47.1")
}
