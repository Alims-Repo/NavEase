package io.github.alimsrepo.navease.internal.runtime

import kotlinx.serialization.Serializable

/**
 * Marker interface for keys.
 *
 * Objects and classes that extend this class must be marked with the [Serializable] annotation in
 * order to be saved with by the [rememberNavBackStack] function.
 *
 * This class is required because [Serializable] is only an annotation and does not provide a way to
 * link classes marked with the annotation together and provide a serializable that works with all
 * of them, resulting it making it impossible to properly save and restore.
 */
public interface NavKey
