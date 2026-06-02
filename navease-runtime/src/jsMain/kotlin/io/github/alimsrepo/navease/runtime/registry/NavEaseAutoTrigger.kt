package io.github.alimsrepo.navease.runtime.registry

/**
 * Kotlin/JS implementation — no-op.
 *
 * In Kotlin/JS the generated `_navEaseAutoInit` top-level property (declared `internal`
 * to survive DCE) is initialised when the JS module is first loaded by the runtime.
 */
internal actual fun navEaseAutoTriggerInit() { /* no-op — module-level init handles JS */ }

