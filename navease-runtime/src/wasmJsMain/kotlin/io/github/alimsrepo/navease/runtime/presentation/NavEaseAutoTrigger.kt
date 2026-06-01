package io.github.alimsrepo.navease.runtime.presentation

/**
 * Kotlin/Wasm implementation — no-op.
 *
 * In Kotlin/Wasm the generated `_navEaseAutoInit` top-level property (declared `internal`
 * to survive DCE) is initialised during module startup. No manual call to
 * `navEaseBootstrap()` is needed.
 */
internal actual fun navEaseAutoTriggerInit() { /* no-op — module-level init handles WasmJS */ }

