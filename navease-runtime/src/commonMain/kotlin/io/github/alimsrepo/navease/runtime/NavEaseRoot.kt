package io.github.alimsrepo.navease.runtime

import io.github.alimsrepo.navease.internal.runtime.NavKey

/**
 * Marker interface for NavEase sealed key hierarchies.
 *
 * Extend your sealed screen-key class from [NavEaseRoot] instead of [NavKey].
 * NavEase handles [NavKey] conformance and serialization internally —
 * no `@Serializable` annotation is required on the sealed class or its subclasses.
 *
 * ```kotlin
 * // Before:
 * @Serializable
 * sealed class AppScreens : NavKey {
 *     @Serializable data object Splash : AppScreens()
 *     @Serializable data class Detail(val id: String) : AppScreens()
 * }
 *
 * // After:
 * sealed class AppScreens : NavEaseRoot {
 *     data object Splash : AppScreens()
 *     data class Detail(val id: String) : AppScreens()
 * }
 * ```
 *
 * The NavEase KSP processor generates type-safe [KSerializer][kotlinx.serialization.KSerializer]
 * implementations for every subclass automatically, and registers them in the
 * [SerializersModule][kotlinx.serialization.modules.SerializersModule] used by nav3.
 *
 * **Constraints:**
 * - Constructor parameters of subclasses must be serializable types
 *   (Kotlin primitives, `String`, or classes annotated with `@Serializable`).
 * - Every `@AutoRegister` screen must reference a subtype of [NavEaseRoot].
 *
 * **Note:** The NavEase Gradle plugin automatically adds the required navigation3
 * dependency to your project, so [NavKey] is available at compile time without
 * being exposed in the public API of published artifacts.
 */
interface NavEaseRoot : NavKey

