package io.github.alimsrepo.navease

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

/**
 * A [CompositionLocal] that provides the current [NavEaseController].
 *
 * Access it from any composable within the NavEase graph via
 * `LocalNavEaseController.current`.
 */
val LocalNavEaseController = compositionLocalOf<NavEaseController> {
    error("No NavEaseController found. Make sure NavEaseHost is in the composition hierarchy.")
}

/**
 * The internal NavEase host composable.
 *
 * This is the real implementation. It is wrapped by the KSP-generated `NavEaseHost` function
 * which automatically wires in the generated [NavEaseScreenFactory].
 *
 * You can also call this directly when you want full manual control over the factory
 * (e.g. in tests).
 *
 * @param startDestination The initial route key presented on first composition.
 * @param factory          The [NavEaseScreenFactory] that resolves keys to composable content.
 * @param modifier         Optional [Modifier] applied to the root container.
 * @param debugOverlay     When `true`, a translucent back-stack debug panel is rendered
 *                         on top of the content.  Enable only in debug builds.
 */
@Composable
fun NavEaseHostInternal(
    startDestination: NavEaseKey,
    factory: NavEaseScreenFactory,
    modifier: Modifier = Modifier,
    debugOverlay: Boolean = false,
) {
    val controller = remember(startDestination) { NavEaseControllerImpl(startDestination) }

    CompositionLocalProvider(LocalNavEaseController provides controller) {
        Box(modifier = modifier.fillMaxSize()) {
            val currentKey = controller.backStack.lastOrNull() ?: startDestination

            AnimatedContent(
                targetState = currentKey,
                transitionSpec = {
                    factory
                        .transitionFor(targetState)
                        .toContentTransform(reverse = controller.isNavigatingBack)
                },
                label = "NavEase:AnimatedContent",
            ) { key ->
                factory.Content(key = key, nav = controller)
            }

            if (debugOverlay) {
                DebugBackStackOverlay(backStack = controller.backStack)
            }
        }
    }
}

