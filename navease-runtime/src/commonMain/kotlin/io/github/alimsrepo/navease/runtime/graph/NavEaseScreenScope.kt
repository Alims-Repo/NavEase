package io.github.alimsrepo.navease.runtime.graph

import androidx.navigation3.runtime.NavKey
import io.github.alimsrepo.navease.runtime.screen.ActivityScreen
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

/**
 * Receiver of the `screens` lambda inside
 * [NavEaseHost][io.github.alimsrepo.navease.runtime.host.NavEaseHost].
 *
 * Use [add] to register each [ActivityScreen]. The reified type parameter is inferred
 * automatically from the screen class:
 *
 * ```kotlin
 * NavEaseHost<AppScreens>(start = AppScreens.Home) {
 *     add(HomeScreen())    // K inferred as AppScreens.Home
 *     add(AboutScreen())   // K inferred as AppScreens.About
 *     add(DetailScreen())  // K inferred as AppScreens.Detail
 * }
 * ```
 */
class NavEaseScreenScope<Root : NavKey> @PublishedApi internal constructor() {

    @PublishedApi internal val screens  = mutableListOf<ActivityScreen<*>>()
    @PublishedApi internal val serPairs = mutableListOf<Pair<KClass<*>, KSerializer<*>>>()

    /**
     * The first screen added with [startWith] = `true`, or the first [add]-ed screen
     * if none is explicitly marked. Used by the start-inferring [NavEaseHost] overload.
     */
    @PublishedApi internal var inferredStart: NavKey? = null

    /**
     * Registers an [ActivityScreen] for the [NavKey] subclass [K].
     *
     * [K] is inferred from the screen's generic parameter — no explicit type argument needed:
     * ```kotlin
     * add(DetailScreen())   // K = AppScreens.Detail, inferred automatically
     * ```
     *
     * @param screen    An instance of your [ActivityScreen] subclass.
     * @param startWith When non-null, marks this key as the start destination for this host.
     *                  Only one screen per [NavEaseHost] should be marked as start.
     *                  If none is marked, use the explicit-start overload of [NavEaseHost].
     */
    inline fun <reified K : Root> add(screen: ActivityScreen<K>, startWith: K? = null) {
        screen._keyClass   = K::class
        screen._serializer = serializer<K>()
        screens  += screen
        serPairs += K::class to serializer<K>()
        if (startWith != null) {
            inferredStart = startWith
        }
    }

    /**
     * Non-inline variant used internally by
     * [NavEaseAutoRegistry][io.github.alimsrepo.navease.runtime.registry.NavEaseAutoRegistry]
     * to populate the scope from pre-resolved KSP registry entries (no reified type parameter needed).
     */
    internal fun addUnchecked(
        screen: ActivityScreen<*>,
        keyClass: KClass<*>,
        serializer: KSerializer<*>,
    ) {
        screen._keyClass   = keyClass
        screen._serializer = serializer
        screens  += screen
        serPairs += keyClass to serializer
    }
}

