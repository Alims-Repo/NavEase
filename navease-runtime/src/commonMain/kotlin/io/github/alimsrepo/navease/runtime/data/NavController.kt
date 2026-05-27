package io.github.alimsrepo.navease.runtime.data

/**
 * Deprecated: [NavController] has moved to [io.github.alimsrepo.navease.runtime.navigation].
 *
 * Update your import:
 * ```
 * import io.github.alimsrepo.navease.runtime.navigation.NavController
 * ```
 */
@Deprecated(
    message = "NavController has moved to io.github.alimsrepo.navease.runtime.navigation. " +
              "Update your import to: io.github.alimsrepo.navease.runtime.navigation.NavController",
    replaceWith = ReplaceWith(
        expression = "NavController",
        imports = ["io.github.alimsrepo.navease.runtime.navigation.NavController"]
    ),
    level = DeprecationLevel.WARNING
)
typealias NavController = io.github.alimsrepo.navease.runtime.navigation.NavController
