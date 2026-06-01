package io.github.alimsrepo.navease.runtime.presentation

// ── Backward-compatibility shim ───────────────────────────────────────────────
// NavEaseAutoRegistry has moved to io.github.alimsrepo.navease.runtime.registry
// navEaseInit() has moved to io.github.alimsrepo.navease.runtime.registry.navEaseInit

@Deprecated(
    message = "NavEaseAutoRegistry has moved to io.github.alimsrepo.navease.runtime.registry.",
    replaceWith = ReplaceWith(
        "NavEaseAutoRegistry",
        "io.github.alimsrepo.navease.runtime.registry.NavEaseAutoRegistry"
    ),
    level = DeprecationLevel.WARNING,
)
typealias NavEaseAutoRegistry = io.github.alimsrepo.navease.runtime.registry.NavEaseAutoRegistry

@Deprecated(
    message = "NavEaseScreenScope has moved to io.github.alimsrepo.navease.runtime.graph.",
    replaceWith = ReplaceWith(
        "NavEaseScreenScope",
        "io.github.alimsrepo.navease.runtime.graph.NavEaseScreenScope"
    ),
    level = DeprecationLevel.WARNING,
)
typealias NavEaseScreenScope<Root> = io.github.alimsrepo.navease.runtime.graph.NavEaseScreenScope<Root>

@Deprecated(
    message = "navEaseInit() has moved to io.github.alimsrepo.navease.runtime.registry.",
    replaceWith = ReplaceWith(
        "navEaseInit()",
        "io.github.alimsrepo.navease.runtime.registry.navEaseInit"
    ),
    level = DeprecationLevel.WARNING,
)
fun navEaseInit() = io.github.alimsrepo.navease.runtime.registry.navEaseInit()
