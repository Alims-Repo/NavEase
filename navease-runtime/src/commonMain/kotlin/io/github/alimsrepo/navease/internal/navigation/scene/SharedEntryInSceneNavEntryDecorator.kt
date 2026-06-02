package io.github.alimsrepo.navease.internal.navigation.scene

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.github.alimsrepo.navease.internal.runtime.NavEntryDecorator
import io.github.alimsrepo.navease.internal.navigation.ui.LocalNavAnimatedContentScope

/** Returns a [SharedEntryInSceneNavEntryDecorator] that is remembered across recompositions. */
@Composable
internal fun <T : Any> rememberSharedEntryInSceneNavEntryDecorator(
    sharedTransitionScope: SharedTransitionScope
): SharedEntryInSceneNavEntryDecorator<T> =
    remember(sharedTransitionScope) { SharedEntryInSceneNavEntryDecorator(sharedTransitionScope) }

/**
 * A [NavEntryDecorator] that wraps each entry in a
 * [SharedTransitionScope.sharedElement] to allow nav displays to animate
 * arbitrarily place entries in different places in the composable call hierarchy.
 *
 * This should be wrapped around the [SceneSetupNavEntryDecorator].
 */
internal class SharedEntryInSceneNavEntryDecorator<T : Any>(
    sharedTransitionScope: SharedTransitionScope
) :
    NavEntryDecorator<T>(
        decorate = { entry ->
            val currentScene = LocalCurrentScene.current
            if (currentScene != null && currentScene !is OverlayScene<*>) {
                with(sharedTransitionScope) {
                    Box(
                        Modifier.sharedElement(
                            rememberSharedContentState(entry.contentKey),
                            animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                        )
                    ) {
                        entry.Content()
                    }
                }
            } else {
                entry.Content()
            }
        }
    )

internal val LocalCurrentScene: ProvidableCompositionLocal<Scene<*>?> = compositionLocalOf { null }
