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
}
