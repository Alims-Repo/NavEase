import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    // ── Android ──────────────────────────────────────────────────────────────
    androidLibrary {
        namespace = "io.github.alimsrepo.navease.runtime"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    // ── Desktop ───────────────────────────────────────────────────────────────
    jvm()

    // ── iOS ───────────────────────────────────────────────────────────────────
    iosArm64()
    iosSimulatorArm64()

    // ── Source sets ───────────────────────────────────────────────────────────
    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.animation)
            implementation(libs.compose.ui)
            implementation(libs.compose.material3)
            implementation(libs.kotlinx.serializationJson)
            implementation(libs.kotlin.stdlib)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}