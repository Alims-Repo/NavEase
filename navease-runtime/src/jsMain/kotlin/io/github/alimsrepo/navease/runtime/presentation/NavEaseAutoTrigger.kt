package io.github.alimsrepo.navease.runtime.presentation

/**
 * Kotlin/JS implementation — no-op.
 *
 * In Kotlin/JS the generated `_navEaseAutoInit` top-level property (declared `internal`
 * to survive DCE) is initialised when the JS module is first loaded by the runtime.
 * No manual call to `navEaseBootstrap()` is needed.
 */
internal actual fun navEaseAutoTriggerInit() { /* no-op — module-level init handles JS */ }

