@file:Suppress("DEPRECATION")

package io.github.alimsrepo.navease.runtime.presentation

/**
 * Moved to [io.github.alimsrepo.navease.runtime.transition.NavTransition].
 * Update your import to: `import io.github.alimsrepo.navease.runtime.transition.NavTransition`
 */
@Deprecated(
    message = "NavTransition has moved to io.github.alimsrepo.navease.runtime.transition. " +
              "Update your import to: io.github.alimsrepo.navease.runtime.transition.NavTransition",
    replaceWith = ReplaceWith(
        "NavTransition",
        "io.github.alimsrepo.navease.runtime.transition.NavTransition"
    ),
    level = DeprecationLevel.WARNING,
)
typealias NavTransition = io.github.alimsrepo.navease.runtime.transition.NavTransition
