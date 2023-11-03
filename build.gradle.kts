import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.mpp.Executable
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

val ktor_version: String by project
val kotlin_version: String by project
val sqllin_version: String by project
val okio_version:String by project
val kotlin_logging_version: String by project
val taskGroupName = "lurker"

plugins {
    kotlin("multiplatform") version "1.9.10"
    id("io.ktor.plugin") version "2.3.5"
    kotlin("plugin.serialization") version "1.9.0"
    id("com.google.devtools.ksp") version "1.9.20-1.0.14"
}

group = "one.tain"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    google()
}


@OptIn(ExperimentalKotlinGradlePluginApi::class)
kotlin {
    targetHierarchy.default()
    fun KotlinNativeTarget.config(custom: Executable.() -> Unit = {}) {
        binaries {
            executable {
                entryPoint = "main"
                custom()
            }
        }
    }

    linuxX64 {
        config()
    }
//    linuxArm64 {
//        config()
//    }
    jvm {
        withJava()
        val jvmJar by tasks.getting(org.gradle.jvm.tasks.Jar::class) {
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE
            doFirst {
                manifest {
                    attributes["Main-Class"] = "MainKt"
                }
                from(configurations.getByName("runtimeClasspath").map { if (it.isDirectory) it else zipTree(it) })
            }
        }
    }
    sourceSets {
        commonMain {
            dependencies {
                implementation("io.ktor:ktor-server-core:$ktor_version")
                implementation("io.ktor:ktor-network-tls:$ktor_version")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
                implementation("com.squareup.okio:okio:$okio_version")
                implementation("net.mamoe.yamlkt:yamlkt:0.13.0")
                implementation("net.peanuuutz.tomlkt:tomlkt:0.3.7")
                implementation("com.ctrip.kotlin:sqllin-dsl:$sqllin_version")
                implementation("com.ctrip.kotlin:sqllin-driver:$sqllin_version")
                implementation("io.github.oshai:kotlin-logging:$kotlin_logging_version")
            }
        }
        val linuxX64Main by getting{
            dependencies{
                implementation("io.ktor:ktor-server-cio:$ktor_version")
            }
        }
        val jvmMain by getting{
            dependencies{
                implementation("io.ktor:ktor-server-netty:$ktor_version")
                implementation("ch.qos.logback:logback-classic:1.4.7")
            }
        }

    }
}
// KSP dependencies
dependencies {
    // sqllin-processor
    add("kspCommonMainMetadata", "com.ctrip.kotlin:sqllin-processor:$sqllin_version")
}

tasks.register("github") {
    group = taskGroupName
    dependsOn(tasks.getByName("linuxX64Binaries"))
    dependsOn(tasks.getByName("jvmJar"))
}
