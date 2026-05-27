package com.alim.navease

import io.github.alimsrepo.navease.NavEaseKey
import kotlinx.serialization.Serializable

/**
 * All screens in the NavEase sample application.
 *
 * This sealed class acts as the single source of truth for every route.
 * Each sub-type can carry typed arguments (no stringly-typed bundles needed).
 *
 * Note: implement [NavEaseKey] so the NavEase runtime can recognise it.
 */
@Serializable
sealed class AppScreens : NavEaseKey {

    /** Splash / loading screen shown on cold start. */
    @Serializable
    data object Splash : AppScreens()

    /** Home feed / dashboard. */
    @Serializable
    data object Home : AppScreens()

    /**
     * Item detail screen.
     *
     * @property itemId The unique identifier of the item to display.
     * @property title  A human-readable item title passed from the previous screen.
     */
    @Serializable
    data class Detail(val itemId: Int, val title: String) : AppScreens()

    /**
     * Settings screen — reachable from Home via the top bar.
     */
    @Serializable
    data object Settings : AppScreens()
}

