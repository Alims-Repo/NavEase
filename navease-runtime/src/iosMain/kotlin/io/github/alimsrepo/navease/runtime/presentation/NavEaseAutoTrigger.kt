package io.github.alimsrepo.navease.runtime.presentation

/**
 * Kotlin/Native (iOS) implementation — no-op.
 *
 * On Kotlin/Native the generated `_navEaseAutoInit` property is annotated with
 * `@EagerInitialization`, which causes `NavEaseAutoInit.init {}` to run automatically
 * when the Kotlin framework is loaded — before any composition starts.
 *
 * No manual call to `navEaseBootstrap()` from MainViewController.kt is needed.
 */
internal actual fun navEaseAutoTriggerInit() { /* no-op — @EagerInitialization handles iOS */ }

