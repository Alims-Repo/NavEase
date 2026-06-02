package io.github.alimsrepo.navease.internal.runtime

@DslMarker public annotation class MetadataDsl

/**
 * Provides a [MetadataScope] to build a Map<String, Any> of metadata.
 *
 * @param builder the DSL extension that provides a [MetadataScope] to build a Map<String, Any> of
 *   metadata
 */
public inline fun metadata(builder: MetadataScope.() -> Unit): Map<String, Any> =
    MetadataScope().apply(builder).build()

/** Scope provided to the metadata dsl builder. */
@MetadataDsl
public class MetadataScope {

    private val map: MutableMap<String, Any> = mutableMapOf()

    /**
     * Adds the key and value pair to the map of metadata.
     *
     * [T] the [value] type. [K] the key for a value of type [T].
     *
     * @param key the key associated with the [value]
     * @param value the data to be added to the map of metadata
     */
    public fun <K : NavMetadataKey<T>, T : Any> put(key: K, value: T) {
        map[key.toString()] = value as T
    }

    @PublishedApi internal fun build(): Map<String, Any> = map
}

/**
 * Returns the metadata value for a given [NavMetadataKey].
 *
 * [T] the value type.
 *
 * @param key the key associated with the value
 */
@Suppress("UNCHECKED_CAST")
public operator fun <T : Any> Map<String, Any>.get(key: NavMetadataKey<T>): T? =
    get(key.toString()) as? T

/**
 * Checks if the metadata contains a given key.
 *
 * [T] the value type.
 *
 * @param key the key to check for
 */
public operator fun <T : Any> Map<String, Any>.contains(key: NavMetadataKey<T>): Boolean =
    contains(key.toString())
