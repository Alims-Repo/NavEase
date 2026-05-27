package io.github.alimsrepo.navease

/**
 * Marker interface that every NavEase route key must implement.
 *
 * Route keys are serializable data objects or data classes that uniquely identify a screen.
 * They carry any arguments the screen needs.
 *
 * ### Example
 * ```kotlin
 * @Serializable
 * sealed class AppScreens : NavEaseKey {
 *     @Serializable data object Home : AppScreens()
 *     @Serializable data class Detail(val id: Int) : AppScreens()
 * }
 * ```
 */
interface NavEaseKey

