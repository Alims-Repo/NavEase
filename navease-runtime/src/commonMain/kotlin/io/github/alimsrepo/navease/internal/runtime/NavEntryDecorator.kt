package io.github.alimsrepo.navease.internal.runtime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable

/**
 * Decorate the [NavEntry]s that are integrated with a [rememberDecoratedNavEntries].
 *
 * **HOW TO USE** Primary usages include but are not limited to:
 * 1. provide information to entries with [androidx.compose.runtime.CompositionLocal], i.e.
 *
 * ```
 * val decorator = NavEntryDecorator<Any> { entry ->
 *    ...
 *    CompositionLocalProvider(LocalMyStateProvider provides myState) {
 *        entry.Content()
 *    }
 * }
 * ```
 * 2. Wrap entry content with other composable content
 *
 * ```
 * val decorator = NavEntryDecorator<Any> { entry ->
 *    ...
 *    MyComposableFunction {
 *        entry.Content()
 *    }
 * }
 * ```
 *
 * **REUSABILITY** To enhance reusability, the NavEntryDecorator can be returned by a function or
 * subclassed:
 * ```
 * // a reusable function
 * fun <T : Any> myDecorator(val myState: MyState): NavEntryDecorator<T> =
 *     NavEntryDecorator(onPop = { contentKey ->  myState.clear(contentKey) }) { entry ->
 *           myState.storeState(entry.contentKey)
 *           entry.Content()
 *     }
 *
 * // or subclass NavEntryDecorator
 * class MyDecorator(
 *      val myState: MyState
 * ): NavEntryDecorator(
 *      onPop = { contentKey ->  myState.clear(contentKey) },
 *      decorate = { entry ->
 *          myState.storeState(entry.contentKey)
 *          entry.Content()
 *      }
 * )
 * ```
 *
 * @param T the type of the backStack key
 * @param onPop the callback to clean up the decorator state associated with a [NavEntry.contentKey]
 *   when the last [NavEntry] with that contentKey has been popped from the backStack. It provides
 *   the [NavEntry.contentKey] of the popped entry as input. This callback is invoked if and only if
 *   all these conditions are met:
 *     1. A [NavEntry] has been popped from the backStack
 *     2. The [NavEntry] that has been popped is the last entry on the backStack with that
 *        particular [NavEntry.contentKey]
 *     3. The [NavEntry.content] of the popped NavEntry has left composition
 *
 * @param [decorate] the composable function to decorate a [NavEntry]. Note that this function only
 *   gets invoked for NavEntries that are actually getting rendered (i.e. by invoking the
 *   [NavEntry.content].)
 * @see NavEntry.contentKey
 */
@Immutable
public open class NavEntryDecorator<T : Any>(
    internal val onPop: (key: Any) -> Unit = {},
    internal val decorate: @Composable (entry: NavEntry<T>) -> Unit,
)
