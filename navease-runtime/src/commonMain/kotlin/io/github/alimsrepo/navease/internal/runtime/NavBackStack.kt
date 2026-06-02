package io.github.alimsrepo.navease.internal.runtime

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.StateObject
import io.github.alimsrepo.navease.internal.runtime.serialization.NavBackStackSerializer
import kotlinx.serialization.Serializable

/**
 * A mutable back stack of [NavKey] elements that integrates with Compose state.
 *
 * This class wraps a [SnapshotStateList] so that updates to the stack automatically trigger
 * recomposition in any observing Composables. It also implements [StateObject], which allows it to
 * participate in Compose's snapshot system directly.
 *
 * Typically, you won’t construct a [NavBackStack] manually. Instead, prefer using
 * [rememberNavBackStack], which provides a stack that is automatically saved and restored across
 * process death and configuration changes.
 *
 * ### Example
 *
 * ```kotlin
 * val backStack = NavBackStack(Home("start"))
 * backStack += Details("item42") // pushes onto stack
 * backStack.removeLast()         // pops stack
 * ```
 *
 * @sample androidx.navigation3.runtime.samples.NavBackStack_OpenPolymorphism
 * @sample androidx.navigation3.runtime.samples.NavBackStack_ClosedPolymorphism
 * @constructor Creates a new back stack backed by the provided [SnapshotStateList].
 * @see rememberNavBackStack for lifecycle-aware persistence.
 */
@Serializable(with = NavBackStackSerializer::class)
public class NavBackStack<T : NavKey> public constructor(internal val base: SnapshotStateList<T>) :
    MutableList<T> by base, StateObject by base, RandomAccess by base {

    public constructor() : this(base = mutableStateListOf())

    public constructor(vararg elements: T) : this(base = mutableStateListOf(*elements))
}
