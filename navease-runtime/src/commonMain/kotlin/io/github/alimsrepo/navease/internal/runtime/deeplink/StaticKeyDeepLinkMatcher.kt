package io.github.alimsrepo.navease.internal.runtime.deeplink

/**
 * A [DeepLinkMatcher] that matches based on a list of [Filter] and if all filters match, returns
 * the input [key] in the [MatchResult].
 *
 * [T] The Type of the navigation key associated with this deep link matcher.
 *
 * @param key the navigation key associated with this deep link matcher
 * @param filters the list of [Filter] to match with the [DeepLinkRequest]
 */
public class StaticKeyDeepLinkMatcher<T : Any>(public val key: T, filters: List<Filter<Any>>) :
    DeepLinkMatcher<T>(filters) {

    /**
     * Returns a [MatchResult] containing the [key] if all [filters] match the [DeepLinkRequest].
     */
    override fun matchRequest(request: DeepLinkRequest): MatchResult<T> = MatchResult(key)
}
