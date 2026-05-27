import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.androidLint)

    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)

    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    android {
        namespace = "io.github.alimsrepo.navease.runtime"
        compileSdk {
            version = release(37)
        }
        minSdk = 24
//        consumerProguardFiles("consumer-rules.pro")
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "navease-runtimeKit"
            isStatic = true
        }
    }

    jvm()

    js { browser() }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.stdlib)

                implementation(compose.runtime)
                implementation(compose.animation)
                implementation(libs.compose.material3)
                // Navigation — exposed as api so consumers (e.g. :shared) can use NavKey directly
                api(libs.androidx.navigation3.ui)
            }
        }
    }
}
