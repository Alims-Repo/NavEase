package io.github.alimsrepo.navease.runtime.registry

/**
 * Kotlin/Wasm implementation — no-op.
 *
 * In Kotlin/Wasm the generated `_navEaseAutoInit` top-level property is initialised
 * during module startup.
 */
internal actual fun navEaseAutoTriggerInit() { /* no-op — module-level init handles WasmJS */ }

