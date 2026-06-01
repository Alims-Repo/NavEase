package io.github.alimsrepo.navease.runtime.presentation

/**
 * Kotlin/JS implementation — no-op.
 *
 * In Kotlin/JS the generated `_navEaseAutoInit` top-level property is initialised when
 * the module is first loaded. Use [navEaseInit] in your JS entry point if the zero-arg
 * [NavEaseHost] is needed without an explicit `autoRegisterScreens()` call.
 */
internal actual fun navEaseAutoTriggerInit() { /* no-op on Kotlin/JS */ }

