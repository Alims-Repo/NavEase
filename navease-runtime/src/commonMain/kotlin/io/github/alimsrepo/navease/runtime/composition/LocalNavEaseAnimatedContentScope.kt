package io.github.alimsrepo.navease.runtime.composition

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.ProvidableCompositionLocal
import io.github.alimsrepo.navease.internal.navigation.ui.LocalNavAnimatedContentScope

/**
 * The [AnimatedContentScope] of the screen currently being animated, for pairing with
 * [LocalNavEaseSharedTransitionScope] on a shared element.
 *
 * Available inside any screen's `Content()`. Reading it outside a host throws, since there is
 * no animation to attach to.
 *
 * ```kotlin
 * val sharedScope = LocalNavEaseSharedTransitionScope.current ?: return
 * val animScope = LocalNavEaseAnimatedContentScope.current
 * with(sharedScope) {
 *     Box(
 *         Modifier.sharedBounds(
 *             sharedContentState = rememberSharedContentState(key = "hero"),
 *             animatedVisibilityScope = animScope,
 *         )
 *     )
 * }
 * ```
 *
 * This is an alias for the same composition local the display provides. It exists so screens
 * never import from the `internal` package, whose contents are not part of the supported API.
 */
public val LocalNavEaseAnimatedContentScope: ProvidableCompositionLocal<AnimatedContentScope>
    get() = LocalNavAnimatedContentScope
