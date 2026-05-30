package io.github.alimsrepo.navease.gradle

/**
 * Configuration block for the NavEase Gradle plugin.
 *
 * ```kotlin
 * navease {
 *     // Pin a specific version instead of the plugin's bundled default
 *     version = "0.0.3"
 *
 *     // Override for local monorepo development (uses project reference instead of Maven)
 *     kspProcessorDependency = project(":navease-ksp")
 *     runtimeDependency      = project(":navease-runtime")
 *
 *     // Set false if you manage navease-runtime yourself
 *     addRuntimeDependency = true
 *
 *     // Change the generated-code package (matches navease.generatedPackage KSP arg)
 *     generatedPackage = "com.example.myapp.navigation"
 * }
 * ```
 */
open class NavEaseExtension {

    /**
     * The NavEase version used for both `navease-runtime` and `navease-ksp`.
     * Leave blank (default) to use the version bundled with this plugin.
     */
    var version: String = ""

    /**
     * When `true` (default) the plugin adds `navease-runtime` to `commonMain` automatically.
     * Set to `false` if you manage the runtime dependency yourself.
     */
    var addRuntimeDependency: Boolean = true

    /**
     * Override the KSP processor dependency added to `kspCommonMainMetadata`.
     *
     * - **Default (null):** uses `io.github.alims-repo:navease-ksp:<version>` from Maven Central.
     * - **Local monorepo:** set to `project(":navease-ksp")` to use the local build.
     */
    var kspProcessorDependency: Any? = null

    /**
     * Override the `navease-runtime` dependency added to `commonMain`.
     *
     * - **Default (null):** uses `io.github.alims-repo:navease-runtime:<version>` from Maven Central.
     * - **Local monorepo:** set to `project(":navease-runtime")` to use the local build.
     */
    var runtimeDependency: Any? = null

    /**
     * The Kotlin package into which KSP generates NavEase files.
     * Leave blank to keep the default: `io.github.alimsrepo.navease.generated`.
     */
    var generatedPackage: String = ""

    // ── Internal helpers ───────────────────────────────────────────────────

    internal fun resolvedVersion(): String =
        version.trim().ifBlank { NavEaseVersion.VERSION }

    internal fun resolvedRuntimeCoordinate(): String =
        "${NavEaseVersion.GROUP}:${NavEaseVersion.RUNTIME_ARTIFACT}:${resolvedVersion()}"

    internal fun resolvedKspCoordinate(): String =
        "${NavEaseVersion.GROUP}:${NavEaseVersion.KSP_ARTIFACT}:${resolvedVersion()}"

    internal fun effectiveKspDependency(): Any = kspProcessorDependency ?: resolvedKspCoordinate()
    internal fun effectiveRuntimeDependency(): Any = runtimeDependency ?: resolvedRuntimeCoordinate()
}



