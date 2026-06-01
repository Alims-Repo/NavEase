package io.github.alimsrepo.navease.runtime.presentation

/**
 * Kotlin/Wasm implementation — no-op.
 *
 * In Kotlin/Wasm the generated `_navEaseAutoInit` top-level property is initialised
 * during module startup. Use [navEaseInit] in your Wasm entry point if the zero-arg
 * [NavEaseHost] is needed without an explicit `autoRegisterScreens()` call.
 */
internal actual fun navEaseAutoTriggerInit() { /* no-op on Kotlin/Wasm */ }

