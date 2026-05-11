plugins {
    id("buildsrc.convention.kotlin-jvm")
    kotlin("plugin.serialization") version "1.9.23"
    application
}

group = "ru.qwuadrixx"
version = "1.0"

repositories {
    mavenCentral()
}

application {
    mainClass = "ru.qwuadrixx.balancer.MainKt"
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(project(":common"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.9.0")
    implementation("com.charleskorn.kaml:kaml:0.57.0")
    implementation("org.slf4j:slf4j-api:2.0.13")
    implementation("ch.qos.logback:logback-classic:1.5.6")
    implementation("io.insert-koin:koin-core:3.5.3")
}

tasks.register<Jar>("fatJar") {
    archiveClassifier.set("all")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes("Main-Class" to "ru.qwuadrixx.balancer.MainKt")
    }

    val dependencies = configurations.runtimeClasspath.get().map {
        if (it.isDirectory) it else zipTree(it)
    }
    from(dependencies)
    with(tasks.jar.get())
}