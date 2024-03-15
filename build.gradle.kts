import org.jetbrains.kotlin.gradle.plugin.mpp.Executable
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.HostManager

val ktor_version: String by project
val kotlin_version: String by project
val sqllin_version: String by project
val okio_version: String by project
val kotlin_logging_version: String by project
val taskGroupName = "lurker"

plugins {
    id("io.ktor.plugin") version "2.3.9"
    kotlin("multiplatform") version "1.9.23"
    kotlin("plugin.serialization") version "1.9.22"
    id("com.google.devtools.ksp") version "1.9.23-1.0.19"
}

group = "one.tain"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    google()
}


kotlin {
    applyDefaultHierarchyTemplate()


    linuxX64 {
        config()
        setupNativeConfig()
    }
//    mingwX64 {
//        config()
//    }
    linuxArm64 {
        config()
        setupNativeConfig()
    }
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
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
            dependencies {
                implementation("io.ktor:ktor-server-core:$ktor_version")
                implementation("io.ktor:ktor-network-tls:$ktor_version")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
                implementation("com.squareup.okio:okio:$okio_version")
                implementation("io.ktor:ktor-server-html-builder:$ktor_version")
                implementation("net.mamoe.yamlkt:yamlkt:0.13.0")
                implementation("net.peanuuutz.tomlkt:tomlkt:0.3.7")
                implementation("com.ctrip.kotlin:sqllin-dsl:$sqllin_version")
                implementation("com.ctrip.kotlin:sqllin-driver:$sqllin_version")
                implementation("io.github.oshai:kotlin-logging:$kotlin_logging_version")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
                implementation("com.benasher44:uuid:0.8.2")
                implementation("io.ktor:ktor-server-cio:$ktor_version")
            }
        }
        val nativeMain by getting {
            dependencies {
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("ch.qos.logback:logback-classic:1.5.3")
            }
        }
    }
}
// KSP dependencies
dependencies {
    // sqllin-processor
    add("kspCommonMainMetadata", "com.ctrip.kotlin:sqllin-processor:$sqllin_version")
}

fun KotlinNativeTarget.config(custom: Executable.() -> Unit = {}) {
    binaries {
        executable {
            entryPoint = "main"
            custom()
        }
    }
}

fun KotlinNativeTarget.setupNativeConfig() {
    binaries {
        all {
            linkerOpts += when {
                HostManager.hostIsLinux  -> listOf(
                    "-lsqlite3",
                    "-L$rootDir/libs/linux",
                    "-L/usr/lib/x86_64-linux-gnu",
                    "-L/usr/lib",
                    "-L/usr/lib64",
                    "--allow-shlib-undefined"
                )

                HostManager.hostIsMingw -> listOf(
                    "-Lc:\\msys64\\mingw64\\lib", "-L$rootDir\\libs\\windows", "-lsqlite3"
                )

                else -> listOf("-lsqlite3")
            }
        }
    }
}

tasks.register("github") {
    group = taskGroupName
    dependsOn(tasks.getByName("linuxX64Binaries"))
    dependsOn(tasks.getByName("jvmJar"))
}
