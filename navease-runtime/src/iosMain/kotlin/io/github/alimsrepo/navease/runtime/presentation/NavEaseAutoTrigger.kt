package io.github.alimsrepo.navease.runtime.presentation

/**
 * Kotlin/Native (iOS) implementation — no-op.
 *
 * On Kotlin/Native, `autoRegisterScreens()` accesses `_navEaseAutoInit` which triggers
 * `NavEaseAutoInit.init {}` lazily when called. For the zero-arg [NavEaseHost] to work
 * without an explicit `autoRegisterScreens()` call, invoke [navEaseInit] once in your
 * iOS entry point (e.g. `MainViewController.kt`).
 */
internal actual fun navEaseAutoTriggerInit() { /* no-op on Kotlin/Native */ }

