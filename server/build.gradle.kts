plugins {
    id("buildsrc.convention.kotlin-jvm")
    kotlin("plugin.serialization") version "1.9.23"
    application
    id("nu.studer.jooq") version "9.0"
}

group = "ru.qwuadrixx"
version = "1.0"

repositories {
    mavenCentral()
}

application {
    mainClass = "ru.qwuadrixx.MainKt"
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    implementation(project(":common"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
    implementation("com.charleskorn.kaml:kaml:0.57.0")
    implementation("org.slf4j:slf4j-api:2.0.13")
    implementation("ch.qos.logback:logback-classic:1.5.6")
    implementation("io.insert-koin:koin-core:3.5.3")
    implementation("org.postgresql:postgresql:42.7.3")
    implementation("org.jooq:jooq:3.19.7")
    jooqGenerator("org.postgresql:postgresql:42.7.3")
}

tasks.test {
    useJUnitPlatform()
}

jooq {
    version.set("3.19.7")
    configurations {
        create("main") {
            generateSchemaSourceOnCompilation.set(false)
            jooqConfiguration.apply {
                jdbc.apply {
                    driver = "org.postgresql.Driver"
                    url = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/itmo"
                    user = System.getenv("DB_USER") ?: "qwuadrixx"
                    password = System.getenv("DB_PASSWORD") ?: "12345"
                }
                generator.apply {
                    name = "org.jooq.codegen.KotlinGenerator"
                    database.apply {
                        name = "org.jooq.meta.postgres.PostgresDatabase"
                        inputSchema = System.getenv("DB_SCHEMA") ?: "s507981"
                    }
                    generate.apply {
                        isDeprecated = false
                        isRecords = true
                        isImmutablePojos = true
                        isFluentSetters = true
                    }
                    target.apply {
                        packageName = "ru.qwuadrixx.generated"
                        directory = "build/generated-src/jooq/main"
                    }
                }
            }
        }
    }
}

tasks.register<Jar>("fatJar") {
    archiveClassifier.set("all")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes("Main-Class" to "ru.qwuadrixx.MainKt")
    }

    val dependencies = configurations.runtimeClasspath.get().map {
        if (it.isDirectory) it else zipTree(it)
    }
    from(dependencies)
    with(tasks.jar.get())

    val destPath = rootProject.projectDir.absolutePath
    doLast {
        val src = archiveFile.get().asFile
        val dest = File(destPath).resolve("server.jar")
        src.copyTo(dest, overwrite = true)
        logger.lifecycle("server.jar скопирован в ${dest.absolutePath}")
    }
}