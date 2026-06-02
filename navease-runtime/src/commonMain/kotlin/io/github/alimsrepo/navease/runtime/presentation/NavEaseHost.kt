@file:Suppress("DEPRECATION")

package io.github.alimsrepo.navease.runtime.presentation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import io.github.alimsrepo.navease.runtime.NavEaseRoot
import io.github.alimsrepo.navease.runtime.transition.NavTransition

// ── Backward-compatibility shims — moved to io.github.alimsrepo.navease.runtime.host ──

/** @suppress */
@Deprecated(
    message = "NavEaseHost has moved to io.github.alimsrepo.navease.runtime.host.",
    replaceWith = ReplaceWith(
        "NavEaseHost(graph, onExitRequest, enableSharedTransitions, navTransition)",
        "io.github.alimsrepo.navease.runtime.host.NavEaseHost"
    ),
    level = DeprecationLevel.WARNING,
)
@Composable
fun NavEaseHost(
    graph: NavEaseGraph,
    onExitRequest: () -> Unit = {},
    enableSharedTransitions: Boolean = false,
    navTransition: NavTransition = NavTransition.Push,
) = io.github.alimsrepo.navease.runtime.host.NavEaseHost(
    graph = graph,
    onExitRequest = onExitRequest,
    enableSharedTransitions = enableSharedTransitions,
    navTransition = navTransition,
)

/** @suppress */
@Deprecated(
    message = "NavEaseHost has moved to io.github.alimsrepo.navease.runtime.host.",
    replaceWith = ReplaceWith(
        "NavEaseHost(start, onExitRequest, enableSharedTransitions, navTransition, screens)",
        "io.github.alimsrepo.navease.runtime.host.NavEaseHost"
    ),
    level = DeprecationLevel.WARNING,
)
@Composable
fun <Root : NavEaseRoot> NavEaseHost(
    start: Root,
    onExitRequest: () -> Unit = {},
    enableSharedTransitions: Boolean = false,
    navTransition: NavTransition = NavTransition.Push,
    screens: NavEaseScreenScope<Root>.() -> Unit,
) = io.github.alimsrepo.navease.runtime.host.NavEaseHost(
    start = start,
    onExitRequest = onExitRequest,
    enableSharedTransitions = enableSharedTransitions,
    navTransition = navTransition,
    screens = screens,
)

/** @suppress */
@Deprecated(
    message = "NavEaseHost has moved to io.github.alimsrepo.navease.runtime.host.",
    replaceWith = ReplaceWith(
        "NavEaseHost(start, onExitRequest, enableSharedTransitions, navTransition)",
        "io.github.alimsrepo.navease.runtime.host.NavEaseHost"
    ),
    level = DeprecationLevel.WARNING,
)
@Composable
fun NavEaseHost(
    start: NavKey,
    onExitRequest: () -> Unit = {},
    enableSharedTransitions: Boolean = false,
    navTransition: NavTransition = NavTransition.Push,
) = io.github.alimsrepo.navease.runtime.host.NavEaseHostForRoot(
    rootClass              = start::class,
    start                  = start,
    onExitRequest          = onExitRequest,
    enableSharedTransitions = enableSharedTransitions,
    navTransition          = navTransition,
)
