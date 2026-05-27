import org.gradle.kotlin.dsl.project
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ksp)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    jvm()

    js {
        browser()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    androidLibrary {
       namespace = "com.alim.navease.shared"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()

       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       androidResources {
           enable = true
       }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)

            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            // NavEase: runtime is a KMP library — use 'api' to expose NavEaseKey,
            // NavEaseController etc. transitively to androidApp consumers.
            api(project(":navease-runtime"))
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jsMain.dependencies {
            implementation(libs.wrappers.browser)
        }
    }
}

// ── KSP ───────────────────────────────────────────────────────────────────────
// ksp(...) cannot be used inside sourceSets.commonMain.dependencies {}.
// KSP configurations are module-level and platform-specific.
// The processor still SEES all commonMain symbols because KSP runs during
// each platform's compilation and has access to the full source tree.
dependencies {
    // ── NavEase KSP ────────────────────────────────────────────────────────────
    // ksp(...) cannot be declared inside sourceSets.commonMain.dependencies {}.
    // KSP configurations are module-level and platform-specific.
    //
    // "kspAndroid" targets the androidMain source set (+ commonMain which is visible
    // to every Android compilation). This is what produces the NavEaseGeneratedFactory
    // and the NavEaseHost composable used by MainActivity.
    add("kspAndroid", project(":navease-ksp"))

    // JVM / Desktop target — generates its own factory for desktop apps.
    add("kspJvm", project(":navease-ksp"))

    androidRuntimeClasspath(libs.compose.uiTooling)
}