package io.github.alimsrepo.navease.internal.runtime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.compose.LocalSavedStateRegistryOwner

/**
 * Returns a [SaveableStateHolderNavEntryDecorator] that is remembered across recompositions.
 *
 * @param saveableStateHolder the [SaveableStateHolder] that scopes the returned NavEntryDecorator
 */
@Composable
public fun <T : Any> rememberSaveableStateHolderNavEntryDecorator(
    saveableStateHolder: SaveableStateHolder = rememberSaveableStateHolder()
): SaveableStateHolderNavEntryDecorator<T> =
    remember(saveableStateHolder) { SaveableStateHolderNavEntryDecorator(saveableStateHolder) }

/**
 * Wraps the content of a [NavEntry] with a [SaveableStateHolder.SaveableStateProvider] to ensure
 * that calls to [rememberSaveable] within the content work properly and that state can be saved.
 * Also provides the content of a [NavEntry] with a [SavedStateRegistryOwner] which can be accessed
 * in the content with [LocalSavedStateRegistryOwner].
 *
 * This [NavEntryDecorator] is the only one that is **required** as saving state is considered a
 * non-optional feature.
 *
 * @param saveableStateHolder the [SaveableStateHolder] that holds the state defined with
 *   [rememberSaveable]. A saved state can only be restored from the [SaveableStateHolder] that it
 *   was saved with.
 */
public class SaveableStateHolderNavEntryDecorator<T : Any>(
    saveableStateHolder: SaveableStateHolder
) :
    NavEntryDecorator<T>(
        onPop = { contentKey -> saveableStateHolder.removeState(contentKey) },
        decorate = { entry ->
            saveableStateHolder.SaveableStateProvider(entry.contentKey) { entry.Content() }
        },
    )
