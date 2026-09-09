package io.github.alimsrepo.navease.gradle

import org.gradle.api.provider.Property

/**
 * Configuration for the NavEase Gradle plugin. Every value has a working default, so the
 * block is optional.
 *
 * ```kotlin
 * navease {
 *     // Pin the artifacts. Defaults to the plugin's own version.
 *     version = "0.2.0"
 *
 *     // Set false to declare navease-runtime yourself.
 *     addRuntimeDependency = false
 *
 *     // Where KSP writes AutoRegisterScreens.kt.
 *     // Defaults to io.github.alimsrepo.navease.generated.<module name>.
 *     generatedPackage = "com.example.app.navigation"
 *
 *     // Point at local builds when developing NavEase itself.
 *     kspProcessorDependency = project(":navease-ksp")
 *     runtimeDependency = project(":navease-runtime")
 * }
 * ```
 */
abstract class NavEaseExtension {

    /**
     * Version used for `navease-runtime` and `navease-ksp`.
     * Defaults to the version of the plugin itself, so the three always match.
     */
    abstract val version: Property<String>

    /**
     * Whether to add `navease-runtime` to `commonMain`. Defaults to `true`.
     * Set to `false` to declare the dependency yourself.
     */
    abstract val addRuntimeDependency: Property<Boolean>

    /**
     * Package for the KSP-generated `AutoRegisterScreens.kt`.
     *
     * Defaults to `io.github.alimsrepo.navease.generated.<module name>`. The module name is
     * part of the default because two modules generating into one package would produce
     * duplicate classes.
     */
    abstract val generatedPackage: Property<String>

    /**
     * Overrides the processor added to `kspCommonMainMetadata`.
     * Defaults to `io.github.alims-repo:navease-ksp:<version>`; set it to
     * `project(":navease-ksp")` when working inside this repository.
     */
    abstract val kspProcessorDependency: Property<Any>

    /**
     * Overrides the runtime added to `commonMain`.
     * Defaults to `io.github.alims-repo:navease-runtime:<version>`.
     */
    abstract val runtimeDependency: Property<Any>

    internal fun resolvedVersion(): String =
        version.orNull?.trim().orEmpty().ifBlank { NavEaseVersion.VERSION }

    internal fun resolvedRuntimeCoordinate(): String =
        "${NavEaseVersion.GROUP}:${NavEaseVersion.RUNTIME_ARTIFACT}:${resolvedVersion()}"

    internal fun resolvedKspCoordinate(): String =
        "${NavEaseVersion.GROUP}:${NavEaseVersion.KSP_ARTIFACT}:${resolvedVersion()}"

    internal fun effectiveKspDependency(): Any =
        kspProcessorDependency.orNull ?: resolvedKspCoordinate()

    internal fun effectiveRuntimeDependency(): Any =
        runtimeDependency.orNull ?: resolvedRuntimeCoordinate()
}
