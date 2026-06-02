package io.github.alimsrepo.navease.runtime.graph

import androidx.compose.runtime.Composable
import io.github.alimsrepo.navease.internal.runtime.NavKey
import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass

/**
 * Binds a [NavKey] subclass [K] to its composable content and serializer.
 *
 * Created internally by [NavEaseGraphBuilder.screen]; not intended to be
 * instantiated directly.
 */
class NavEaseEntry<K : NavKey> @PublishedApi internal constructor(
    @PublishedApi internal val keyClass: KClass<K>,
    @PublishedApi internal val serializer: KSerializer<K>,
    @PublishedApi internal val content: @Composable (K) -> Unit,
)

