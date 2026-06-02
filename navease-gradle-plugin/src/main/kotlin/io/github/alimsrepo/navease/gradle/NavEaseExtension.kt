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
 *     // Set false if you manage navigation3-ui yourself (e.g., to use a different version)
 *     addNavigation3Dependency = true
 *
 *     // Use a specific navigation3 version (if not already declared in your project)
 *     navigation3Dependency = "org.jetbrains.androidx.navigation3:navigation3-ui:1.2.0"
 *
 *     // Force NavEase's navigation3 version even if you have a different one
 *     forceNavigation3Version = true
 *
 *     // Change the generated-code package (matches navease.generatedPackage KSP arg)
 *     generatedPackage = "com.example.myapp.navigation"
 * }
 * ```
 *
 * **Handling Navigation3 Version Conflicts:**
 *
 * If you already have `navigation3-ui` declared:
 * ```kotlin
 * // Scenario 1: Let Gradle handle version resolution (default)
 * // NavEase detects existing navigation3-ui and skips auto-injection.
 * // Gradle uses standard resolution (typically highest version wins).
 * kotlin.sourceSets.commonMain.dependencies {
 *     implementation("org.jetbrains.androidx.navigation3:navigation3-ui:1.2.0")
 * }
 *
 * // Scenario 2: Force NavEase's bundled version
 * navease {
 *     forceNavigation3Version = true  // Enforces 1.1.1 (or your custom version)
 * }
 *
 * // Scenario 3: Use your version but let NavEase add it if missing
 * navease {
 *     navigation3Dependency = "org.jetbrains.androidx.navigation3:navigation3-ui:1.2.0"
 * }
 *
 * // Scenario 4: Manage navigation3 completely yourself
 * navease {
 *     addNavigation3Dependency = false
 * }
 * kotlin.sourceSets.commonMain.dependencies {
 *     implementation("org.jetbrains.androidx.navigation3:navigation3-ui:1.2.0")
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
     * When `true` (default) the plugin adds `androidx.navigation3:navigation3-ui` to `commonMain`
     * automatically. This is required because `NavEaseRoot` extends `NavKey` from navigation3.
     *
     * **Why this is needed:**
     * - `NavEaseRoot` extends `NavKey` internally for navigation framework integration
     * - To avoid compiler warnings about inaccessible supertypes, navigation3 must be available
     *   in consuming projects
     * - However, it's declared as `implementation` (not `api`) in navease-runtime to avoid
     *   polluting the public API of published libraries
     *
     * **Version conflict handling:**
     * - If navigation3-ui is already declared in your project, auto-injection is skipped
     * - Gradle will use its standard resolution strategy (typically highest version wins)
     * - Set `forceNavigation3Version = true` to enforce the NavEase-bundled version
     *
     * Set to `false` if you manage the navigation3 dependency yourself or use a different version.
     */
    var addNavigation3Dependency: Boolean = true

    /**
     * When `true`, forces the NavEase-bundled navigation3-ui version using Gradle's resolution strategy,
     * overriding any other version declared in the project. Use this if you experience version conflicts.
     *
     * Default: `false` (respects Gradle's standard resolution, typically highest version wins)
     */
    var forceNavigation3Version: Boolean = false

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
     * Override the `androidx.navigation3.navigation3-ui` dependency added to `commonMain`.
     *
     * - **Default (null):** uses the version bundled with NavEase (currently 1.1.1).
     * - **Custom version:** set to `"org.jetbrains.androidx.navigation3:navigation3-ui:X.Y.Z"`.
     */
    var navigation3Dependency: Any? = null

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

    internal fun effectiveNavigation3Dependency(): Any =
        navigation3Dependency ?: "org.jetbrains.androidx.navigation3:navigation3-ui:1.1.1"
}



