package io.github.alimsrepo.navease.internal.navigation.ui

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import io.github.alimsrepo.navease.internal.runtime.NavEntry

/**
 * Local provider of [AnimatedContentScope] to [NavEntry.Content].
 *
 * This does not have a default value since the AnimatedContentScope is provided at runtime by
 * AnimatedContent.
 *
 * @sample androidx.navigation3.ui.samples.SceneNavSharedElementSample
 */
public val LocalNavAnimatedContentScope: ProvidableCompositionLocal<AnimatedContentScope> =
    compositionLocalOf {
        // no default, we need an AnimatedContent to get the AnimatedContentScope
        throw IllegalStateException(
            "Unexpected access to LocalNavAnimatedContentScope. You should only " +
                "access LocalNavAnimatedContentScope inside a NavEntry passed " +
                "to NavDisplay. AnimatedContentScope in OverlayScenes are no-op."
        )
    }
