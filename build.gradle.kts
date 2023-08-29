val ktor_version: String by project
val kotlin_version: String by project

plugins {
    kotlin("multiplatform") version "1.9.0"
    id("io.ktor.plugin") version "2.3.3"
//    id("app.cash.sqldelight") version "2.0.0"
}

group = "one.tain"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    google()
}

//sqldelight {
//    databases {
//        create("Database") {
//            packageName.set("one.tain.lurker")
//        }
//    }
//}

kotlin {
    val hostOs = System.getProperty("os.name")
    val isArm64 = System.getProperty("os.arch") == "aarch64"
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" && isArm64 -> macosArm64("native")
        hostOs == "Mac OS X" && !isArm64 -> macosX64("native")
        hostOs == "Linux" && isArm64 -> linuxArm64("native")
        hostOs == "Linux" && !isArm64 -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    nativeTarget.apply {
        binaries {
            executable {
                entryPoint = "main"
            }
        }
    }
    sourceSets {
        commonMain {
            dependencies {
                implementation("io.ktor:ktor-server-core:$ktor_version")
                implementation("io.ktor:ktor-server-cio:$ktor_version")
                implementation("io.ktor:ktor-network-tls:$ktor_version")
                implementation("app.cash.sqldelight:native-driver:2.0.0")
            }
        }

    }
}
