package io.github.alimsrepo.navease.runtime.registry

import androidx.navigation3.runtime.NavKey
import io.github.alimsrepo.navease.runtime.screen.ActivityScreen
import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass

/**
 * Global registry populated by NavEase KSP-generated code.
 *
 * Holds all screens annotated with `@AutoRegister` and the app's start destination.
 * Used by the zero-configuration [NavEaseHost][io.github.alimsrepo.navease.runtime.host.NavEaseHost] overload.
 *
 * **You do not normally interact with this object directly.**
 * It is populated automatically by the KSP-generated `NavEaseAutoInit` initializer.
 *
 * Bootstraps automatically on all platforms — no manual call required in your code:
 *
 * - **Android / JVM / Desktop**: [NavEaseHost][io.github.alimsrepo.navease.runtime.host.NavEaseHost]
 *   loads the generated class via `Class.forName` on first composition, which triggers
 *   `NavEaseAutoInit.init {}`.
 * - **iOS / Native**: the generated `_navEaseAutoInit` property is annotated with
 *   `@EagerInitialization`, so it runs when the Kotlin framework is loaded.
 * - **JS / WasmJS**: the generated module-level property is initialised when the
 *   JS module is first loaded by the runtime.
 *
 * The previously required `navEaseBootstrap()` call from platform entry points is
 * **no longer needed**. Existing calls are safe to remove.
 */
object NavEaseAutoRegistry {

    /**
     * Fully-qualified class name of the KSP-generated initializer file.
     *
     * Defaults to `io.github.alimsrepo.navease.generated.AutoRegisterScreensKt`.
     * Override only when using a custom `generatedPackage` KSP option.
     * Set it **before** first composition — e.g. inside `Application.onCreate()`.
     */
    var generatedClassHint: String =
        "io.github.alimsrepo.navease.generated.AutoRegisterScreensKt"

    internal val entries: MutableList<RegistryEntry> = mutableListOf()

    internal var startKey: NavKey? = null
        private set

    /** `true` once [entries] has been populated and [startKey] is set. */
    val isInitialized: Boolean get() = startKey != null

    /**
     * A registered screen entry produced by [addEntry].
     *
     * @property keyClass   Runtime [KClass] of the [NavKey] subtype handled by [screen].
     * @property serializer [KSerializer] for the [NavKey] subtype.
     * @property screen     [ActivityScreen] instance registered for this key.
     */
    data class RegistryEntry(
        val keyClass: KClass<*>,
        val serializer: KSerializer<*>,
        val screen: ActivityScreen<*>,
    )

    /**
     * Registers one screen into this registry.
     *
     * Called directly from the KSP-generated `NavEaseAutoInit.init {}` block.
     *
     * @param screen       Screen instance.
     * @param keyClass     Runtime [KClass] of [K].
     * @param serializer   [KSerializer] for [K].
     * @param startNavKey  Non-null to designate this entry's key as the start destination.
     */
    @Suppress("UNCHECKED_CAST")
    fun <K : NavKey> addEntry(
        screen: ActivityScreen<K>,
        keyClass: KClass<K>,
        serializer: KSerializer<K>,
        startNavKey: K? = null,
    ) {
        entries.add(
            RegistryEntry(
                keyClass   = keyClass   as KClass<*>,
                serializer = serializer as KSerializer<*>,
                screen     = screen,
            )
        )
        if (startNavKey != null) startKey = startNavKey
    }

    /**
     * Ensures the registry is populated on JVM/Android via class-loading.
     *
     * On iOS/Native this is a no-op — the registry is populated when the Kotlin
     * framework is loaded via `@EagerInitialization`.
     */
    internal fun ensureInitialized() {
        if (isInitialized) return
        navEaseAutoTriggerInit()
    }
}

// ── Platform hook ─────────────────────────────────────────────────────────────

/**
 * Platform-specific trigger for auto-initialising the KSP-generated registry.
 *
 * - **JVM / Android**: loads [NavEaseAutoRegistry.generatedClassHint] via `Class.forName`.
 * - **iOS / Native / JS / Wasm**: no-op — initialisation is handled by the runtime.
 */
internal expect fun navEaseAutoTriggerInit()

// ── Public initialisation helpers ─────────────────────────────────────────────

/**
 * Triggers the NavEase screen registry initialisation (JVM/Android only convenience).
 *
 * **No longer required** — NavEase bootstraps automatically on all platforms.
 * This function is kept for backward compatibility; calling it is safe but unnecessary.
 */
fun navEaseInit() = NavEaseAutoRegistry.ensureInitialized()

