package io.github.alimsrepo.navease.runtime.annotations

/**
 * Annotate a nested data class inside a [NavEaseScreen] class to declare
 * route arguments. KSP will generate a `data class` route instead of a `data object`.
 *
 * Example:
 * ```
 * @NavEaseScreen(route = "Profile")
 * class ProfileScreen : NavScreen<AppScreens.Profile>() {
 *
 *     @NavEaseArgs
 *     data class Args(val userId: String, val age: Int)
 * }
 * ```
 * Generates: `@Serializable data class Profile(val userId: String, val age: Int) : AppScreens()`
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class NavEaseArgs