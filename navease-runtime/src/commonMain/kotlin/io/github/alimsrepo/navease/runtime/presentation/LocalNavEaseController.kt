package io.github.alimsrepo.navease.runtime.presentation

// Moved to io.github.alimsrepo.navease.runtime.composition.LocalNavEaseController
// Re-exported here for backward compatibility — update your import.
@Deprecated(
    message = "LocalNavEaseController has moved to io.github.alimsrepo.navease.runtime.composition.",
    replaceWith = ReplaceWith(
        "LocalNavEaseController",
        "io.github.alimsrepo.navease.runtime.composition.LocalNavEaseController"
    ),
    level = DeprecationLevel.WARNING,
)
val LocalNavEaseController get() = io.github.alimsrepo.navease.runtime.composition.LocalNavEaseController
