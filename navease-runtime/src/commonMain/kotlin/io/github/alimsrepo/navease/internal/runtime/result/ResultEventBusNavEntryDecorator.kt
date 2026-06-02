package io.github.alimsrepo.navease.internal.runtime.result

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import io.github.alimsrepo.navease.internal.runtime.NavEntry
import io.github.alimsrepo.navease.internal.runtime.NavEntryDecorator

/** Returns a [ResultEventBusNavEntryDecorator] that is remembered across recompositions. */
@Composable
public fun <T : Any> rememberResultEventBusNavEntryDecorator(): ResultEventBusNavEntryDecorator<T> =
    remember {
        ResultEventBusNavEntryDecorator()
    }

/**
 * Wraps the content of a [NavEntry] with a [LocalResultEventBus] to provide the ability to pass
 * results to previous entries on the navigation backstack.
 */
public class ResultEventBusNavEntryDecorator<T : Any>(
    private val bus: ResultEventBus = ResultEventBus()
) :
    NavEntryDecorator<T>(
        onPop = {},
        decorate = { entry ->
            CompositionLocalProvider(LocalResultEventBus provides bus) { entry.Content() }
        },
    )
