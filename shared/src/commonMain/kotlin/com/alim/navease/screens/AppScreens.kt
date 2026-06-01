package com.alim.navease.screens

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Root nav-key hierarchy for the NavEase sample app.
 * Every entry demonstrates one NavEase feature.
 */
@Serializable
sealed class AppScreens : NavKey {

    /** Animated splash — auto-navigates to [Home]. */
    @Serializable data object Splash : AppScreens()

    /** Feature hub — entry point after the splash. */
    @Serializable data object Home : AppScreens()

    /** Demonstrates the 6 built-in NavTransition styles. */
    @Serializable data object Transitions : AppScreens()

    /**
     * Full-screen preview for a single transition.
     * @param transitionName Human-readable style name (e.g. "Push").
     * @param tagline        One-line motion description.
     */
    @Serializable data class TransitionPreview(
        val transitionName: String,
        val tagline: String,
    ) : AppScreens()

    /** Demonstrates nested NavEaseHost with its own start key and back-stack. */
    @Serializable data object NestedNavDemo : AppScreens()

    /**
     * Demonstrates typed NavKey arguments passed from [Home].
     * @param feature     Feature name shown as the title.
     * @param description Short description shown as the subtitle.
     */
    @Serializable data class TypedArgs(
        val feature: String,
        val description: String,
    ) : AppScreens()

    /** Demonstrates typed navigation results via [navEaseController.backWithResult]. */
    @Serializable data object TypedResult : AppScreens()
}

// ── Inner nav-key hierarchy for the nested NavEaseHost inside NestedNavDemoScreen ──

@Serializable
sealed class WizardStep : NavKey {
    @Serializable data object SelectRole : WizardStep()
    @Serializable data class SelectLevel(val role: String) : WizardStep()
    @Serializable data class SelectTech(val role: String, val level: String) : WizardStep()
    @Serializable data class Summary(val role: String, val level: String, val tech: String) : WizardStep()
}
