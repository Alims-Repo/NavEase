package io.github.alimsrepo.navease.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

/**
 * NavEase Gradle plugin — automatically wires KSP and the NavEase artifacts for
 * Kotlin Multiplatform projects so users need zero manual boilerplate.
 *
 * **Before (manual setup — 15+ lines):**
 * ```kotlin
 * // shared/build.gradle.kts
 * plugins {
 *     kotlin("multiplatform")
 *     id("com.google.devtools.ksp")
 * }
 * kotlin {
 *     sourceSets {
 *         commonMain {
 *             kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
 *             dependencies {
 *                 implementation("io.github.alims-repo:navease-runtime:<version>")
 *             }
 *         }
 *     }
 * }
 * dependencies {
 *     add("kspCommonMainMetadata", "io.github.alims-repo:navease-ksp:<version>")
 * }
 * tasks.withType<KotlinCompilationTask<*>>().configureEach {
 *     if (name != "kspCommonMainKotlinMetadata") dependsOn("kspCommonMainKotlinMetadata")
 * }
 * ```
 *
 * **After (with this plugin — 2 lines):**
 * ```kotlin
 * plugins {
 *     kotlin("multiplatform")
 *     id("com.google.devtools.ksp")
 *     id("io.github.alims-repo.navease") version "<version>"
 * }
 * // Done — navease-runtime, navease-ksp, srcDir, and task wiring are all handled.
 * ```
 *
 * Optional configuration via the `navease {}` extension block.
 */
class NavEasePlugin : Plugin<Project> {

    override fun apply(target: Project) {

        // Register the extension so users can configure it before afterEvaluate fires
        val extension = target.extensions.create("navease", NavEaseExtension::class.java)

        // ── Step 1: Wire task dependencies ─────────────────────────────────────
        // Registered eagerly so it captures tasks added by KSP after afterEvaluate.
        target.tasks.withType(KotlinCompilationTask::class.java).configureEach { task ->
            if (task.name != "kspCommonMainKotlinMetadata") {
                task.dependsOn("kspCommonMainKotlinMetadata")
            }
        }

        // ── Steps 2–5: deferred until after evaluation so extension values are set
        target.afterEvaluate { project ->
            val kmp = project.extensions.findByType(KotlinMultiplatformExtension::class.java)
            if (kmp == null) {
                project.logger.warn(
                    "[NavEase] KotlinMultiplatformExtension not found. " +
                    "Apply the Kotlin Multiplatform plugin before the NavEase plugin."
                )
                return@afterEvaluate
            }

            // ── Step 2: Add navease-ksp to the kspCommonMainMetadata configuration ──
            try {
                project.dependencies.add(
                    "kspCommonMainMetadata",
                    extension.resolvedKspCoordinate()
                )
                project.logger.info("[NavEase] Added KSP processor: ${extension.resolvedKspCoordinate()}")
            } catch (e: Exception) {
                project.logger.warn(
                    "[NavEase] Could not add kspCommonMainMetadata dependency — " +
                    "make sure the KSP plugin (com.google.devtools.ksp) is applied. ${e.message}"
                )
            }

            // ── Step 3: Register generated-source directory with commonMain ──────
            val generatedSrcDir = project.layout.buildDirectory
                .dir("generated/ksp/metadata/commonMain/kotlin")
            kmp.sourceSets.findByName("commonMain")?.kotlin?.srcDir(generatedSrcDir)
            project.logger.info("[NavEase] Registered srcDir: $generatedSrcDir")

            // ── Step 4: Optionally add navease-runtime to commonMain ─────────────
            if (extension.addRuntimeDependency) {
                kmp.sourceSets.findByName("commonMain")?.dependencies {
                    implementation(extension.resolvedRuntimeCoordinate())
                }
                project.logger.info("[NavEase] Added runtime: ${extension.resolvedRuntimeCoordinate()}")
            }

            // ── Step 5: Forward generatedPackage to the KSP processor arg ────────
            val customPackage = extension.generatedPackage.trim()
            if (customPackage.isNotBlank()) {
                forwardKspArg(project, "navease.generatedPackage", customPackage)
            }
        }
    }

    /**
     * Forwards a KSP argument to the `ksp {}` extension using reflection so that
     * we avoid a hard compile-time dependency on the KSP Gradle plugin API class.
     */
    private fun forwardKspArg(project: Project, key: String, value: String) {
        try {
            val kspExt = project.extensions.findByName("ksp") ?: run {
                project.logger.warn(
                    "[NavEase] Cannot set KSP arg '$key' — ksp extension not found. " +
                    "Apply the KSP plugin (com.google.devtools.ksp) before NavEase."
                )
                return
            }
            val argMethod = kspExt.javaClass.methods.find { m ->
                m.name == "arg" && m.parameterCount == 2 &&
                m.parameterTypes[0] == String::class.java &&
                m.parameterTypes[1] == String::class.java
            }
            if (argMethod != null) {
                argMethod.invoke(kspExt, key, value)
                project.logger.info("[NavEase] Set KSP arg: $key=$value")
            } else {
                project.logger.warn("[NavEase] ksp.arg(String, String) method not found — skipping '$key'.")
            }
        } catch (e: Exception) {
            project.logger.warn("[NavEase] Failed to set KSP arg '$key': ${e.message}")
        }
    }
}

