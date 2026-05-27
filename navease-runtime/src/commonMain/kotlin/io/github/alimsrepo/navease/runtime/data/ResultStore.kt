package io.github.alimsrepo.navease.runtime.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.produceState
import kotlin.reflect.KClass

internal object ResultStore {
    val results = mutableStateMapOf<String, Any?>()
}

fun NavController.backWithResult(result: Any) {
    ResultStore.results[result::class.qualifiedName!!] = result
    back()
}

@Composable
fun <T : Any> NavController.resultOf(clazz: KClass<T>): State<T?> {
    val key = clazz.qualifiedName!!
    @Suppress("UNCHECKED_CAST")
    return produceState<T?>(initialValue = ResultStore.results[key] as? T) {
        // Consume immediately after reading
        value = ResultStore.results.remove(key) as? T
    }
}

