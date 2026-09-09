package io.github.alimsrepo.navease.gradle

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Wires KSP and the NavEase artifacts into a Kotlin Multiplatform module.
 *
 * ```kotlin
 * plugins {
 *     kotlin("multiplatform")
 *     id("org.jetbrains.compose")
 *     id("org.jetbrains.kotlin.plugin.compose")
 *     id("io.github.alims-repo.navease") version "<version>"
 * }
 * ```
 *
 * The plugin:
 * - applies `com.google.devtools.ksp` and `org.jetbrains.kotlin.plugin.serialization`,
 *   unless the build already declares them;
 * - adds `navease-ksp` to `kspCommonMainMetadata` and `navease-runtime` to `commonMain`;
 * - registers `build/generated/ksp/metadata/commonMain/kotlin` as a `commonMain` source
 *   directory;
 * - makes every compilation depend on `kspCommonMainKotlinMetadata`;
 * - gives the module its own generated package, so several modules can use NavEase without
 *   generating colliding classes.
 *
 * Configure it through [NavEaseExtension].
 *
 * Kotlin Gradle plugin types are reached only through [KotlinMultiplatformWiring]; see that
 * class for why.
 */
class NavEasePlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val extension = target.extensions.create("navease", NavEaseExtension::class.java)
        extension.addRuntimeDependency.convention(true)

        // Everything is deferred until Kotlin Multiplatform is present. Applying KSP to a
        // project without a Kotlin plugin fails inside KSP with a NoClassDefFoundError, which
        // says nothing about what the build is missing; and the plugins {} block does not
        // guarantee that kotlin("multiplatform") is applied before this plugin.
        var multiplatformApplied = false
        target.pluginManager.withPlugin(KOTLIN_MULTIPLATFORM_ID) {
            multiplatformApplied = true
            applyCompilerPlugins(target)
            KotlinMultiplatformWiring.wireTaskOrdering(target, KSP_METADATA_TASK)
        }

        target.afterEvaluate { project ->
            if (!multiplatformApplied) {
                throw GradleException(
                    "[NavEase] The Kotlin Multiplatform plugin is required. Apply " +
                        "kotlin(\"multiplatform\") in the same module as " +
                        "io.github.alims-repo.navease.",
                )
            }

            addProcessor(project, extension)
            KotlinMultiplatformWiring.addGeneratedSourceDir(project, GENERATED_SOURCE_PATH)

            if (extension.addRuntimeDependency.getOrElse(true)) {
                KotlinMultiplatformWiring.addRuntimeDependency(
                    project,
                    extension.effectiveRuntimeDependency(),
                )
            } else {
                project.logger.info("[NavEase] addRuntimeDependency = false — runtime not added.")
            }

            configureGeneratedPackage(project, extension)
        }
    }

    private fun applyCompilerPlugins(project: Project) {
        // Both are skipped when already declared, so a build that pins its own KSP or
        // serialization version keeps it.
        listOf(
            "com.google.devtools.ksp",
            "org.jetbrains.kotlin.plugin.serialization",
        ).forEach { id ->
            if (project.pluginManager.hasPlugin(id)) {
                project.logger.info("[NavEase] $id already applied — skipping.")
            } else {
                project.pluginManager.apply(id)
                project.logger.info("[NavEase] Applied $id.")
            }
        }
    }

    private fun addProcessor(project: Project, extension: NavEaseExtension) {
        val dependency = extension.effectiveKspDependency()
        try {
            project.dependencies.add("kspCommonMainMetadata", dependency)
        } catch (e: Exception) {
            // Swallowing this would generate no screens at all, and the build would instead
            // fail much later with a confusing empty-registry error at runtime.
            throw GradleException(
                "[NavEase] Could not add '$dependency' to the kspCommonMainMetadata " +
                    "configuration. Check that the KSP plugin applied successfully.",
                e,
            )
        }
        project.logger.info("[NavEase] Added KSP processor: $dependency")
    }

    private fun configureGeneratedPackage(project: Project, extension: NavEaseExtension) {
        val configured = extension.generatedPackage.orNull?.trim().orEmpty()
        val packageName = configured.ifBlank {
            "$DEFAULT_GENERATED_PACKAGE.${project.name.toPackageSegment()}"
        }
        forwardKspArg(project, "navease.generatedPackage", packageName)
    }

    /**
     * Sets a KSP argument reflectively, so the plugin needs no compile-time dependency on the
     * KSP Gradle plugin's API.
     */
    private fun forwardKspArg(project: Project, key: String, value: String) {
        val kspExtension = project.extensions.findByName("ksp")
            ?: throw GradleException(
                "[NavEase] The 'ksp' extension is missing, so '$key' cannot be set. Apply " +
                    "com.google.devtools.ksp before io.github.alims-repo.navease.",
            )
        val argMethod = kspExtension.javaClass.methods.firstOrNull { method ->
            method.name == "arg" &&
                method.parameterCount == 2 &&
                method.parameterTypes.all { it == String::class.java }
        } ?: throw GradleException(
            "[NavEase] Could not find ksp.arg(String, String) on ${kspExtension.javaClass.name}. " +
                "This usually means an incompatible KSP version.",
        )
        argMethod.invoke(kspExtension, key, value)
        project.logger.info("[NavEase] Set KSP arg $key=$value")
    }

    internal companion object {
        const val KOTLIN_MULTIPLATFORM_ID = "org.jetbrains.kotlin.multiplatform"
        const val KSP_METADATA_TASK = "kspCommonMainKotlinMetadata"
        const val GENERATED_SOURCE_PATH = "generated/ksp/metadata/commonMain/kotlin"
        const val DEFAULT_GENERATED_PACKAGE = "io.github.alimsrepo.navease.generated"

        /**
         * Turns a Gradle module name into a legal package segment: `my-feature` becomes
         * `my_feature`, and a name starting with a digit is prefixed so it stays an identifier.
         */
        internal fun String.toPackageSegment(): String {
            val sanitized = replace(Regex("[^A-Za-z0-9_]"), "_").ifEmpty { "module" }
            return if (sanitized.first().isDigit()) "_$sanitized" else sanitized
        }
    }
}
