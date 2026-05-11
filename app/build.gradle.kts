plugins {
    // Apply the shared build logic from a convention plugin.
    // The shared code is located in `buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts`.
    id("buildsrc.convention.kotlin-jvm")
    id("org.jetbrains.dokka-javadoc") version "2.1.0"
    kotlin("plugin.serialization") version "1.9.23"

    // Apply the Application plugin to add support for building an executable JVM application.
    application
}

group = "ru.qwuadrixx"
version = "1.0"

application {
    // Define the Fully Qualified Name for the application main class
    // (Note that Kotlin compiles `App.kt` to a class with FQN `com.example.app.AppKt`.)
    mainClass = "ru.qwuadrixx.app.AppKt"
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
    implementation("com.charleskorn.kaml:kaml:0.57.0")
    implementation("org.slf4j:slf4j-api:2.0.13")
    implementation("ch.qos.logback:logback-classic:1.5.6")
    implementation("io.insert-koin:koin-core:3.5.3")
    implementation(project(":common"))
}

tasks.register<Jar>("fatJar") {
    archiveClassifier.set("all")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes("Main-Class" to "ru.qwuadrixx.app.AppKt")
    }

    val dependencies = configurations.runtimeClasspath.get().map {
        if (it.isDirectory) it else zipTree(it)
    }
    from(dependencies)
    with(tasks.jar.get())
}
