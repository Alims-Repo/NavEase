package io.github.alimsrepo.navease.runtime.presentation

/**
 * JVM (Desktop) implementation of the auto-init trigger.
 *
 * Loads the KSP-generated class via `Class.forName`, which triggers the JVM static
 * initialiser (`<clinit>`), initialising `_navEaseAutoInit` and therefore running
 * `NavEaseAutoInit.init {}` — which sets [NavEaseAutoRegistry.registrar].
 */
internal actual fun navEaseAutoTriggerInit() {
    runCatching {
        Class.forName(NavEaseAutoRegistry.generatedClassHint)
    }
}

