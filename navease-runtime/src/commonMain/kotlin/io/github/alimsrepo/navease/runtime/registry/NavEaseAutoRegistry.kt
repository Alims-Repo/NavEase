package io.github.alimsrepo.navease.runtime.registry

import io.github.alimsrepo.navease.internal.runtime.NavKey
import io.github.alimsrepo.navease.runtime.screen.ActivityScreen
import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass

/**
 * Registry of every `@AutoRegister` screen, populated by KSP-generated code.
 *
 * You do not interact with this object directly. Each module's generated
 * `AutoRegisterScreens.kt` declares a `navEaseBootstrap()` function that fills it in,
 * and the generated `NavEaseHost` overloads call that function before composing a host.
 *
 * Registration is idempotent: calling `navEaseBootstrap()` more than once — from
 * several hosts, or from several modules — adds each key exactly once.
 */
public object NavEaseAutoRegistry {

    private val entries = LinkedHashMap<KClass<*>, RegistryEntry>()

    /** `true` once at least one screen has been registered. */
    public val isInitialized: Boolean get() = entries.isNotEmpty()

    /**
     * One registered screen.
     *
     * @property keyClass      The specific key type this screen handles, e.g. `AppScreens.Detail`.
     * @property rootKeyClass  The sealed root [keyClass] belongs to, e.g. `AppScreens`. Hosts
     *                         filter on this so nested graphs stay independent.
     * @property serializer    Serializer for [keyClass], used to restore the back stack.
     * @property screenFactory Creates the screen. Called once per host, never shared between hosts.
     */
    public class RegistryEntry(
        public val keyClass: KClass<*>,
        public val rootKeyClass: KClass<*>,
        public val serializer: KSerializer<*>,
        public val screenFactory: () -> ActivityScreen<*>,
    )

    /**
     * Registers one screen. Called from generated code only.
     *
     * Re-registering the same [keyClass] is a no-op, so a module whose bootstrap runs
     * twice does not duplicate entries. A *different* screen claiming a key that is
     * already taken is a programming error and throws — KSP rejects that case at build
     * time within a module, but two modules can only be caught here.
     *
     * @param keyClass      Runtime class of [K].
     * @param rootKeyClass  Runtime class of the sealed root [K] belongs to.
     * @param serializer    Serializer for [K].
     * @param screenFactory Creates the screen handling [K].
     */
    public fun <K : NavKey> addEntry(
        keyClass: KClass<K>,
        rootKeyClass: KClass<*>,
        serializer: KSerializer<K>,
        screenFactory: () -> ActivityScreen<K>,
    ) {
        val existing = entries[keyClass]
        if (existing != null) {
            check(existing.rootKeyClass == rootKeyClass) {
                "NavEase: key '${keyClass.simpleName}' is registered under two different roots " +
                    "('${existing.rootKeyClass.simpleName}' and '${rootKeyClass.simpleName}'). " +
                    "A key may belong to only one sealed root."
            }
            return
        }
        entries[keyClass] = RegistryEntry(keyClass, rootKeyClass, serializer, screenFactory)
    }

    /**
     * Builds the key-to-screen map for [rootClass], creating one screen instance per call
     * so that two hosts of the same root never share screen objects.
     *
     * @throws IllegalStateException if no screens are registered for [rootClass].
     */
    internal fun screensForRoot(rootClass: KClass<*>): Map<KClass<*>, ActivityScreen<*>> =
        entriesForRoot(rootClass).associate { it.keyClass to it.screenFactory() }

    /** Key-to-serializer pairs for [rootClass], for the host's `SavedStateConfiguration`. */
    internal fun serializersForRoot(rootClass: KClass<*>): List<Pair<KClass<*>, KSerializer<*>>> =
        entriesForRoot(rootClass).map { it.keyClass to it.serializer }

    private fun entriesForRoot(rootClass: KClass<*>): List<RegistryEntry> {
        check(isInitialized) {
            "NavEase: the screen registry is empty. Either the project has not been rebuilt " +
                "since the last change (run ./gradlew kspCommonMainKotlinMetadata), or this host " +
                "did not bind to the KSP-generated NavEaseHost overload. The generated overload " +
                "is what initialises the registry — check that the NavEase Gradle plugin is " +
                "applied to the module that declares your @AutoRegister screens."
        }
        val matching = entries.values.filter { it.rootKeyClass == rootClass }
        check(matching.isNotEmpty()) {
            val known = entries.values
                .map { it.rootKeyClass.simpleName }
                .distinct()
                .sortedBy { it ?: "" }
            "NavEase: no @AutoRegister screens found for root '${rootClass.simpleName}'. " +
                "Registered roots: ${known.joinToString()}. Screens are matched by the outermost " +
                "sealed class of their key, so check that your key really is a direct subtype of " +
                "'${rootClass.simpleName}'."
        }
        return matching
    }

    /** Removes every registration. Test-only. */
    internal fun clear() {
        entries.clear()
    }
}
