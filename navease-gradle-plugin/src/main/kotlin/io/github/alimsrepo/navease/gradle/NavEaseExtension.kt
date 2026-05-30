package io.github.alimsrepo.navease.gradle

/**
 * Configuration block for the NavEase Gradle plugin.
 *
 * Usage in `build.gradle.kts`:
 * ```kotlin
 * navease {
 *     // Pin a specific version instead of the plugin's bundled default
 *     version = "0.0.3"
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
     * When `true` (default) the plugin adds `navease-runtime` to the `commonMain`
     * dependency block automatically. Set to `false` to manage the runtime dependency yourself.
     */
    var addRuntimeDependency: Boolean = true

    /**
     * The Kotlin package into which KSP generates NavEase files (`AppScreens`, `ScreenFactory`,
     * `NavEaseExtensions`, `NavEaseResults`, `NavEaseHost`).
     *
     * Leave blank to keep the default: `io.github.alimsrepo.navease.generated`.
     *
     * Setting this value forwards the `navease.generatedPackage` argument to the KSP processor
     * and also adjusts the `srcDir` path accordingly — no extra config needed.
     */
    var generatedPackage: String = ""

    // ── Internal helpers ───────────────────────────────────────────────────

    internal fun resolvedVersion(): String =
        version.trim().ifBlank { NavEaseVersion.VERSION }

    internal fun resolvedRuntimeCoordinate(): String =
        "${NavEaseVersion.GROUP}:${NavEaseVersion.RUNTIME_ARTIFACT}:${resolvedVersion()}"

    internal fun resolvedKspCoordinate(): String =
        "${NavEaseVersion.GROUP}:${NavEaseVersion.KSP_ARTIFACT}:${resolvedVersion()}"
}

