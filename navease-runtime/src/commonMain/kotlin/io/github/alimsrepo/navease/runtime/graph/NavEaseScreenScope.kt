package io.github.alimsrepo.navease.runtime.graph

import io.github.alimsrepo.navease.internal.runtime.NavKey
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
     * Registers an [ActivityScreen] for the [NavKey] subclass [K].
     *
     * [K] is inferred from the screen's generic parameter — no explicit type argument needed:
     * ```kotlin
     * add(DetailScreen())   // K = AppScreens.Detail, inferred automatically
     * ```
     *
     * @param screen An instance of your [ActivityScreen] subclass.
     */
    inline fun <reified K : Root> add(screen: ActivityScreen<K>) {
        screen._keyClass   = K::class
        screen._serializer = serializer<K>()
        screens  += screen
        serPairs += K::class to serializer<K>()
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

