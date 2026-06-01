@file:Suppress("DEPRECATION")

package io.github.alimsrepo.navease.runtime.presentation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import io.github.alimsrepo.navease.runtime.domain.NavScreen
import io.github.alimsrepo.navease.runtime.transition.NavTransition

// ── Backward-compatibility shims — moved to io.github.alimsrepo.navease.runtime.host ──

/** @suppress */
@Deprecated(
    message = "NavEaseNavGraph has moved to io.github.alimsrepo.navease.runtime.host.",
    replaceWith = ReplaceWith(
        "NavEaseNavGraph(initialScreen, savedStateConfig, screenFactory, onExitRequest, enableSharedTransitions, navTransition)",
        "io.github.alimsrepo.navease.runtime.host.NavEaseNavGraph"
    ),
    level = DeprecationLevel.WARNING,
)
@Composable
fun NavEaseNavGraph(
    initialScreen: NavKey,
    savedStateConfig: SavedStateConfiguration,
    screenFactory: (NavKey) -> NavScreen,
    onExitRequest: () -> Unit = {},
    enableSharedTransitions: Boolean = false,
    navTransition: NavTransition = NavTransition.Push,
) = io.github.alimsrepo.navease.runtime.host.NavEaseNavGraph(
    initialScreen = initialScreen,
    savedStateConfig = savedStateConfig,
    screenFactory = screenFactory,
    onExitRequest = onExitRequest,
    enableSharedTransitions = enableSharedTransitions,
    navTransition = navTransition.toNew(),
)

/** @suppress */
@Deprecated(
    message = "NavEaseNavGraph has moved to io.github.alimsrepo.navease.runtime.host.",
    replaceWith = ReplaceWith(
        "NavEaseNavGraph(graph, onExitRequest, enableSharedTransitions, navTransition)",
        "io.github.alimsrepo.navease.runtime.host.NavEaseNavGraph"
    ),
    level = DeprecationLevel.WARNING,
)
@Composable
fun NavEaseNavGraph(
    graph: NavEaseGraph,
    onExitRequest: () -> Unit = {},
    enableSharedTransitions: Boolean = false,
    navTransition: NavTransition = NavTransition.Push,
) = io.github.alimsrepo.navease.runtime.host.NavEaseNavGraph(
    graph = graph,
    onExitRequest = onExitRequest,
    enableSharedTransitions = enableSharedTransitions,
    navTransition = navTransition.toNew(),
)

// NavTransition typealias means NavTransition here == io.github.alimsrepo.navease.runtime.transition.NavTransition
// so no conversion needed — this helper is just for clarity
@Suppress("NOTHING_TO_INLINE")
private inline fun NavTransition.toNew() = this
