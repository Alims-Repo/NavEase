package io.github.alimsrepo.navease.runtime.presentation

// ── Backward-compatibility shims — classes moved to runtime.graph ─────────────
// Update your imports to io.github.alimsrepo.navease.runtime.graph.*

@Deprecated(
    message = "NavEaseEntry has moved to io.github.alimsrepo.navease.runtime.graph.",
    replaceWith = ReplaceWith("NavEaseEntry", "io.github.alimsrepo.navease.runtime.graph.NavEaseEntry"),
    level = DeprecationLevel.WARNING,
)
typealias NavEaseEntry<K> = io.github.alimsrepo.navease.runtime.graph.NavEaseEntry<K>

@Deprecated(
    message = "NavEaseGraph has moved to io.github.alimsrepo.navease.runtime.graph.",
    replaceWith = ReplaceWith("NavEaseGraph", "io.github.alimsrepo.navease.runtime.graph.NavEaseGraph"),
    level = DeprecationLevel.WARNING,
)
typealias NavEaseGraph = io.github.alimsrepo.navease.runtime.graph.NavEaseGraph

@Deprecated(
    message = "NavEaseGraphBuilder has moved to io.github.alimsrepo.navease.runtime.graph.",
    replaceWith = ReplaceWith("NavEaseGraphBuilder", "io.github.alimsrepo.navease.runtime.graph.NavEaseGraphBuilder"),
    level = DeprecationLevel.WARNING,
)
typealias NavEaseGraphBuilder = io.github.alimsrepo.navease.runtime.graph.NavEaseGraphBuilder

// navEaseGraph() inline fun cannot be typealiased — import directly from:
// io.github.alimsrepo.navease.runtime.graph.navEaseGraph
