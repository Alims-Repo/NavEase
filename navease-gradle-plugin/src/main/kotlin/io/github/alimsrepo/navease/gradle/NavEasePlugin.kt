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
 * tasks.configureEach {
 *     if (name != "kspCommonMainKotlinMetadata" && name.startsWith("ksp")) {
 *         dependsOn("kspCommonMainKotlinMetadata")
 *     }
 * }
 * ```
 *
 * **After (with this plugin — 1 line):**
 * ```kotlin
 * plugins {
 *     kotlin("multiplatform")
 *     id("io.github.alims-repo.navease") version "<version>"
 * }
 * // Done — KSP is applied automatically, navease-runtime, navease-ksp, srcDir,
 * // and task wiring are all handled. Zero boilerplate.
 * ```
 *
 * For local monorepo development, override dependencies via the `navease {}` extension:
 * ```kotlin
 * navease {
 *     kspProcessorDependency = project(":navease-ksp")
 *     runtimeDependency      = project(":navease-runtime")
 * }
 * ```
 */
class NavEasePlugin : Plugin<Project> {

    override fun apply(target: Project) {

        // Register the extension so users can configure it before afterEvaluate fires
        val extension = target.extensions.create("navease", NavEaseExtension::class.java)

        // ── Step 1: Apply the KSP plugin so users don't need to ────────────────
        // Guard: if the consumer already applied KSP (with their own version),
        // skip applying it again — using two different versions in the same
        // build causes a "Plugin already applied" / classpath conflict error.
        if (!target.pluginManager.hasPlugin("com.google.devtools.ksp")) {
            target.pluginManager.apply("com.google.devtools.ksp")
            target.logger.info("[NavEase] Applied com.google.devtools.ksp automatically.")
        } else {
            target.logger.info("[NavEase] com.google.devtools.ksp already present — skipping auto-apply.")
        }

        // ── Step 2: Wire task dependencies ─────────────────────────────────────
        // Two separate hooks are needed because KSP target tasks (e.g.
        // kspKotlinIosSimulatorArm64, kspKotlinAndroid) are KspAATask instances —
        // NOT KotlinCompilationTask — so they are missed by withType<KotlinCompilationTask>.
        // Both hooks must depend on kspCommonMainKotlinMetadata to avoid the
        // "implicit dependency" Gradle configuration error.

        // 2a. All Kotlin compilation tasks (compileKotlinIosArm64, etc.)
        target.tasks.withType(KotlinCompilationTask::class.java).configureEach { task ->
            if (task.name != "kspCommonMainKotlinMetadata") {
                task.dependsOn("kspCommonMainKotlinMetadata")
            }
        }

        // 2b. All KSP platform tasks (kspKotlin*, kspMetadata, etc.)
        target.tasks.configureEach { task ->
            if (task.name != "kspCommonMainKotlinMetadata" &&
                task.name.startsWith("ksp")
            ) {
                task.dependsOn("kspCommonMainKotlinMetadata")
            }
        }

        // ── Steps 3–6: deferred until after evaluation so extension values are set
        target.afterEvaluate { project ->
            val kmp = project.extensions.findByType(KotlinMultiplatformExtension::class.java)
            if (kmp == null) {
                project.logger.warn(
                    "[NavEase] KotlinMultiplatformExtension not found. " +
                    "Apply the Kotlin Multiplatform plugin before the NavEase plugin."
                )
                return@afterEvaluate
            }

            // ── Step 3: Add navease-ksp to the kspCommonMainMetadata configuration ──
            try {
                val kspDep = extension.effectiveKspDependency()
                project.dependencies.add("kspCommonMainMetadata", kspDep)
                project.logger.info("[NavEase] Added KSP processor: $kspDep")
            } catch (e: Exception) {
                project.logger.warn(
                    "[NavEase] Could not add kspCommonMainMetadata dependency: ${e.message}"
                )
            }

            // ── Step 4: Register generated-source directory with commonMain ──────
            val generatedSrcDir = project.layout.buildDirectory
                .dir("generated/ksp/metadata/commonMain/kotlin")
            kmp.sourceSets.findByName("commonMain")?.kotlin?.srcDir(generatedSrcDir)
            project.logger.info("[NavEase] Registered srcDir: $generatedSrcDir")

            // ── Step 5: Optionally add navease-runtime to commonMain ─────────────
            if (extension.addRuntimeDependency) {
                val runtimeDep = extension.effectiveRuntimeDependency()
                kmp.sourceSets.findByName("commonMain")?.dependencies {
                    implementation(runtimeDep)
                }
                project.logger.info("[NavEase] Added runtime: $runtimeDep")
            }

            // ── Step 6: Forward generatedPackage to the KSP processor arg ────────
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
