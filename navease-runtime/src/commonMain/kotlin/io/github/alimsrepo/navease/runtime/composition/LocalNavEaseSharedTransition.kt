@file:OptIn(ExperimentalSharedTransitionApi::class)

package io.github.alimsrepo.navease.runtime.composition

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf

/**
 * Provides the [SharedTransitionScope] when shared element transitions are enabled via
 * `NavEaseHost(enableSharedTransitions = true)`.
 *
 * Returns `null` when not enabled (default) — zero overhead and no experimental opt-in required
 * in screens that don't use shared elements.
 *
 * ## Usage in a screen
 *
 * ```kotlin
 * @OptIn(ExperimentalSharedTransitionApi::class)
 * class HeroScreen : ActivityScreen<AppScreens.Hero>() {
 *     @Composable
 *     override fun Content(navKey: AppScreens.Hero, navController: NavController) {
 *         val sharedScope = LocalNavEaseSharedTransitionScope.current ?: return
 *         val animScope   = LocalNavAnimatedContentScope.current   // from navigation3-ui
 *         with(sharedScope) {
 *             Box(
 *                 Modifier.sharedBounds(
 *                     sharedContentState      = rememberSharedContentState(key = "hero_card"),
 *                     animatedVisibilityScope = animScope,
 *                 )
 *             ) { /* … */ }
 *         }
 *     }
 * }
 * ```
 *
 * @see androidx.navigation3.ui.LocalNavAnimatedContentScope
 */
public val LocalNavEaseSharedTransitionScope: ProvidableCompositionLocal<SharedTransitionScope?> =
    compositionLocalOf { null }

