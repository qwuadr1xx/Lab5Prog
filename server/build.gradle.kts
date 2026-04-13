plugins {
    id("buildsrc.convention.kotlin-jvm")
    kotlin("plugin.serialization") version "1.9.23"
}

group = "ru.qwuadrixx"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(project(":common"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.9.0")
}

tasks.test {
    useJUnitPlatform()
}