package io.github.alimsrepo.navease.internal.runtime.result

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

/**
 * An Effect to provide a result event between different screens
 *
 * The trailing lambda provides the result from a flow of results.
 *
 * @param T the type of the result that should be retrieved from this effect
 * @param resultEventBus the ResultEventBus to retrieve the result from. The default value is read
 *   from the `LocalResultEventBus` composition local.
 * @param onResult the callback to invoke when a result is received
 */
@Composable
public inline fun <reified T> ResultEffect(
    resultEventBus: ResultEventBus = LocalResultEventBus.current,
    noinline onResult: suspend (T) -> Unit,
) {
    ResultEffect(T::class.toString(), resultEventBus, onResult)
}

/**
 * An Effect to provide a result event between different screens
 *
 * The trailing lambda provides the result from a flow of results.
 *
 * @param resultKey the key that should be associated with this effect
 * @param resultEventBus the ResultEventBus to retrieve the result from. The default value is read
 *   from the `LocalResultEventBus` composition local.
 * @param onResult the callback to invoke when a result is received
 */
@Composable
public fun <T> ResultEffect(
    resultKey: String,
    resultEventBus: ResultEventBus = LocalResultEventBus.current,
    onResult: suspend (T) -> Unit,
) {
    LaunchedEffect(resultKey, resultEventBus.channelMap[resultKey]) {
        resultEventBus.getResultFlow(resultKey)?.collect { result ->
            @Suppress("UNCHECKED_CAST") onResult.invoke(result as T)
        }
    }
}
