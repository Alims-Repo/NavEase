package com.alim.navease.screens

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Sealed nav-key hierarchy for the demo app.
 *
 * Each subclass is a [NavKey] that identifies one screen.
 * Args are plain constructor properties — no @NavEaseArgs annotation needed.
 */
@Serializable
sealed class AppScreens : NavKey {

    /** Animated splash / loading screen — start destination. */
    @Serializable
    data object Splash : AppScreens()

    /** Main hub — shows all libraries. */
    @Serializable
    data object Home : AppScreens()

    /**
     * Library detail screen.
     * @param libId   Unique library identifier used to look up [LibraryData].
     * @param libName Display name (passed along to avoid a second lookup).
     */
    @Serializable
    data class LibraryDetail(val libId: String, val libName: String) : AppScreens()

    /** NavEase transition-styles demo. */
    @Serializable
    data object NavEaseDemo : AppScreens()

    /** SecureVault interactive API simulation. */
    @Serializable
    data object SecureVaultDemo : AppScreens()

    /** FlowTab live bottom-navigation demo. */
    @Serializable
    data object FlowTabDemo : AppScreens()

    /** PrayerTimes calculation demo. */
    @Serializable
    data object PrayerTimesDemo : AppScreens()

    /** CrashGuard crash-screen preview. */
    @Serializable
    data object CrashGuardDemo : AppScreens()

    /** Pdf Generator DSL demo. */
    @Serializable
    data object PdfDemo : AppScreens()

    /**
     * Minimal screen opened from [NavEaseDemo] to showcase a single [NavTransition].
     * @param transitionName  Human-readable name (e.g. "Push").
     * @param tagline         One-line description shown on screen.
     */
    @Serializable
    data class TransitionPreview(val transitionName: String, val tagline: String) : AppScreens()
}

