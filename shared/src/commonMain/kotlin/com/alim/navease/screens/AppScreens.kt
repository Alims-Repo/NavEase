package com.alim.navease.screens

import io.github.alimsrepo.navease.runtime.NavEaseRoot

/**
 * Root nav-key hierarchy for the NavEase sample app.
 * Every entry demonstrates one NavEase feature.
 *
 * No `@Serializable` annotation needed — NavEase KSP generates
 * [KSerializer][kotlinx.serialization.KSerializer] implementations internally.
 * No `: NavKey` needed — [NavEaseRoot] satisfies that contract transparently.
 */
sealed class AppScreens : NavEaseRoot {

    /** Animated splash — auto-navigates to [Home]. */
    data object Splash : AppScreens()

    /** Main hub screen with navigation options. */
    data object Home : AppScreens()

    /** User profile screen with edit capabilities. */
    data class Profile(val userId: String, val isEditable: Boolean = false) : AppScreens()

    /** Settings screen with various configuration options. */
    data object Settings : AppScreens()

    /** Gallery screen demonstrating list navigation. */
    data object Gallery : AppScreens()

    /** Generic detail screen with typed arguments. */
    data class Detail(val itemId: String, val title: String) : AppScreens()
}
