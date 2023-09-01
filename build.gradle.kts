val ktor_version: String by project
val kotlin_version: String by project
val okio_version:String by project
val logback_version: String by project

plugins {
    kotlin("jvm") version "1.9.10"
    id("io.ktor.plugin") version "2.3.3"
    kotlin("plugin.serialization") version "1.9.0"
//    id("app.cash.sqldelight") version "2.0.0"
}

group = "one.tain"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    google()
}

application {
    mainClass.set("one.tain.demo.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}


//sqldelight {
//    databases {
//        create("Database") {
//            packageName.set("one.tain.lurker")
//        }
//    }
//}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-cio-jvm:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("com.squareup.okio:okio:$okio_version")
    implementation("net.peanuuutz:tomlkt:0.2.0")
//                implementation("app.cash.sqldelight:native-driver:2.0.0")
}
