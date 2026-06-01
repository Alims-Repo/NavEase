package io.github.alimsrepo.navease.runtime.registry

import androidx.navigation3.runtime.NavKey
import io.github.alimsrepo.navease.runtime.screen.ActivityScreen
import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass

/**
 * Global registry populated by NavEase KSP-generated code.
 *
 * Holds all screens annotated with `@AutoRegister`.
 * Used by the auto-discover [NavEaseHost][io.github.alimsrepo.navease.runtime.host.NavEaseHost] overload.
 *
 * **You do not normally interact with this object directly.**
 * It is populated automatically by the KSP-generated `NavEaseAutoInit` initializer.
 *
 * The start destination is **not** stored here — it is declared explicitly at the host level
 * via the `start` parameter of [NavEaseHost][io.github.alimsrepo.navease.runtime.host.NavEaseHost].
 *
 * Initialization per platform:
 *
 * - **Android / JVM / Desktop**: automatic — [NavEaseHost][io.github.alimsrepo.navease.runtime.host.NavEaseHost]
 *   loads the generated class via `Class.forName` on first composition.
 * - **iOS / Native**: automatic via `@EagerInitialization` in the Gradle-plugin-generated glue file.
 * - **JS / WasmJS**: automatic — module-level property init runs at module load time.
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

    /** `true` once [entries] has been populated by the KSP-generated initializer. */
    val isInitialized: Boolean get() = entries.isNotEmpty()

    /**
     * Optional hook set by the KSP-generated `_navEaseAutoInit` property initializer.
     *
     * When the generated class is loaded (JVM) or `navEaseBootstrap()` is called,
     * this hook is registered so that future calls to [ensureInitialized] can re-trigger
     * initialization without another `Class.forName` lookup.
     */
    internal var bootstrapHook: (() -> Unit)? = null

    /**
     * A registered screen entry produced by [addEntry].
     *
     * @property keyClass     Runtime [KClass] of the specific [NavKey] subtype handled by [screen]
     *                        (e.g. `WizardStep.SelectRole::class`).
     * @property rootKeyClass Runtime [KClass] of the sealed root type that [keyClass] belongs to
     *                        (e.g. `WizardStep::class`). Used by the typed
     *                        [NavEaseHost][io.github.alimsrepo.navease.runtime.host.NavEaseHost]
     *                        overload to filter entries for a specific nav-graph root.
     * @property serializer   [KSerializer] for the [NavKey] subtype.
     * @property screen       [ActivityScreen] instance registered for this key.
     */
    data class RegistryEntry(
        val keyClass: KClass<*>,
        val rootKeyClass: KClass<*>,
        val serializer: KSerializer<*>,
        val screen: ActivityScreen<*>,
    )

    /**
     * Registers one screen into this registry.
     *
     * Called directly from the KSP-generated `NavEaseAutoInit.init {}` block.
     *
     * @param screen        Screen instance.
     * @param keyClass      Runtime [KClass] of [K] (the specific NavKey subtype).
     * @param rootKeyClass  Runtime [KClass] of the sealed root that [K] belongs to.
     *                      Used to filter entries in the typed [NavEaseHost] overload.
     * @param serializer    [KSerializer] for [K].
     */
    @Suppress("UNCHECKED_CAST")
    fun <K : NavKey> addEntry(
        screen: ActivityScreen<K>,
        keyClass: KClass<K>,
        rootKeyClass: KClass<*>,
        serializer: KSerializer<K>,
    ) {
        entries.add(
            RegistryEntry(
                keyClass     = keyClass     as KClass<*>,
                rootKeyClass = rootKeyClass as KClass<*>,
                serializer   = serializer   as KSerializer<*>,
                screen       = screen,
            )
        )
    }

    /**
     * Called by the KSP-generated `AutoRegisterScreens.kt` when `_navEaseAutoInit` is
     * first accessed, registering the lambda that can re-trigger `NavEaseAutoInit.init {}`.
     */
    fun registerBootstrapHook(hook: () -> Unit) {
        bootstrapHook = hook
    }

    /**
     * Ensures the registry is populated.
     *
     * - On **JVM/Android/Desktop**: triggers `Class.forName` via [navEaseAutoTriggerInit],
     *   which loads the generated class and runs `NavEaseAutoInit.init {}`.
     * - On **iOS/Native**: no-op — `@EagerInitialization` in the glue file handles this.
     * - On **JS/WasmJS**: module-level init already ran; this is a safety check.
     */
    internal fun ensureInitialized() {
        if (isInitialized) return
        bootstrapHook?.invoke()      // fast path if hook already registered
        if (!isInitialized) navEaseAutoTriggerInit()  // JVM Class.forName fallback
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
