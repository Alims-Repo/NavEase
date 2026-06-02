package io.github.alimsrepo.navease.internal.runtime.deeplink

import io.github.alimsrepo.navease.internal.runtime.fastForEachOrForEach

/**
 * Encompasses the logic to match a navigation key of type [T] against a [DeepLinkRequest].
 *
 * A navigation key can be associated with more than one [DeepLinkMatcher] if it supports different
 * forms of deep links.
 *
 * [T] The type of the navigation key associated with this deep link.
 *
 * @param filters an optional list of [Filter] to apply to the [DeepLinkRequest] during matching
 */
public abstract class DeepLinkMatcher<T : Any>(private val filters: List<Filter<*>> = emptyList()) {
    /**
     * Matches a [DeepLinkRequest] to a [DeepLinkMatcher].
     *
     * Returns [MatchResult] if it is a match, and returns null otherwise.
     *
     * @param request the [DeepLinkRequest] to match against
     */
    public fun match(request: DeepLinkRequest): MatchResult<T>? {
        // check filters first to avoid creating and discarding a key that doesn't match filters\
        filters.fastForEachOrForEach { filter ->
            val isMatch = filter.filterRequest(request)
            if (!isMatch) return null
        }
        return matchRequest(request)
    }

    /**
     * Matches a [DeepLinkRequest] to a [DeepLinkMatcher].
     *
     * Returns [MatchResult] if it is a match, and returns null otherwise.
     *
     * @param request the [DeepLinkRequest] to match against
     */
    protected abstract fun matchRequest(request: DeepLinkRequest): MatchResult<T>?

    /**
     * A filter for a deep link, such as a mimeType.
     *
     * Filters declared in a [DeepLinkMatcher] must be present in a [DeepLinkRequest]. On the other
     * hand, a matching [DeepLinkRequest] may contain more filter info than is required by a
     * [DeepLinkMatcher]
     *
     * @param filter the value to filter by
     */
    public abstract class Filter<K : Any>(private val filter: K) {
        /**
         * Matches a [DeepLinkRequest] to this [Filter].
         *
         * Returns true if they are a match, false otherwise.
         */
        public abstract fun filterRequest(request: DeepLinkRequest): Boolean
    }

    /**
     * The class that is returned when a [DeepLinkMatcher] matches with a [DeepLinkRequest]
     *
     * @param key the navigation key representing the deep link target
     */
    public open class MatchResult<T>(public val key: T) : Comparable<MatchResult<T>> {
        public override fun compareTo(other: MatchResult<T>): Int = 1
    }
}
