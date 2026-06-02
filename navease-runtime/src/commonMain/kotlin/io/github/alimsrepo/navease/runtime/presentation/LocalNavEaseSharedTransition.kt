@file:OptIn(ExperimentalSharedTransitionApi::class)

package io.github.alimsrepo.navease.runtime.presentation

import androidx.compose.animation.ExperimentalSharedTransitionApi

// Moved to io.github.alimsrepo.navease.runtime.composition.LocalNavEaseSharedTransitionScope
// Re-exported here for backward compatibility — update your import.
@Deprecated(
    message = "LocalNavEaseSharedTransitionScope has moved to io.github.alimsrepo.navease.runtime.composition.",
    replaceWith = ReplaceWith(
        "LocalNavEaseSharedTransitionScope",
        "io.github.alimsrepo.navease.runtime.composition.LocalNavEaseSharedTransitionScope"
    ),
    level = DeprecationLevel.WARNING,
)
val LocalNavEaseSharedTransitionScope
    get() = io.github.alimsrepo.navease.runtime.composition.LocalNavEaseSharedTransitionScope
