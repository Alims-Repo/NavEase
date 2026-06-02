@file:JvmName("RememberNavBackStackKt")
@file:JvmMultifileClass

package io.github.alimsrepo.navease.internal.runtime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSerializable
import io.github.alimsrepo.navease.internal.runtime.serialization.NavBackStackSerializer
import io.github.alimsrepo.navease.internal.runtime.serialization.NavKeySerializer
import androidx.savedstate.serialization.SavedStateConfiguration

/**
 * Provides a [NavBackStack] that is automatically remembered in the Compose hierarchy across
 * process death and configuration changes.
 *
 * This overload **does not take a [SavedStateConfiguration]**. It relies on the platform default:
 * on **Android**, state is saved/restored using a **reflection-based serializer**; on **other
 * platforms this will fail at runtime**. If you target non-Android platforms, use the overload that
 * accepts a [SavedStateConfiguration] and register your serializers explicitly.
 *
 * ### When to use this overload
 * - You are on **Android only** and want a simple API that uses reflection under the hood.
 * - Your back stack elements use **closed polymorphism** (sealed hierarchies) or otherwise work
 *   with Android’s reflective serializer.
 *
 * ### Serialization requirements
 * - Each element placed in the [NavBackStack] must be `@Serializable`.
 * - For **closed polymorphism** (sealed hierarchies), the compiler knows all subtypes and generates
 *   serializers; Android’s reflection will also work.
 * - For **open polymorphism** (interfaces or non-sealed hierarchies):
 *     - On Android, the reflection path can handle subtypes without manual registration.
 *     - On non-Android, this overload is **unsupported**; use the configuration overload and
 *       register all subtypes of [NavKey] in a [kotlinx.serialization.modules.SerializersModule].
 *
 * @sample androidx.navigation3.runtime.samples.rememberNavBackStack_withReflection
 * @param elements The initial keys of this back stack.
 * @return A [NavBackStack] that survives process death and configuration changes on Android.
 * @see NavBackStackSerializer
 */
@Composable
public fun rememberNavBackStack(vararg elements: NavKey): NavBackStack<NavKey> {
    return rememberSerializable(
        serializer = NavBackStackSerializer(elementSerializer = NavKeySerializer())
    ) {
        NavBackStack(*elements)
    }
}
