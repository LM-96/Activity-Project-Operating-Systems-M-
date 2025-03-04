plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinxSerialization)
}

group = "unibo.apos.ktntv"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

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
        nativeMain.dependencies {

            // COROUTINES
            // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-core
            implementation(libs.kotlinxCoroutinesCore)

            // DATETIME
            // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-datetime
            implementation(libs.kotlinxDatetime)

            // IO
            // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-io-core
            implementation(libs.kotlinxIO)

            // CLIKT
            // https://mvnrepository.com/artifact/com.github.ajalt.clikt/clikt
            implementation(libs.clikt)

            // LOGGING
            // https://mvnrepository.com/artifact/ch.qos.logback/logback-classic
            implementation(libs.logback)
            // https://mvnrepository.com/artifact/io.github.oshai/kotlin-logging-jvm
            implementation(libs.kotlinLogging)
        }
    }
}
