package io.github.alimsrepo.navease.internal.runtime.deeplink

import kotlin.jvm.JvmStatic

/**
 * Represents a requested deep link.
 *
 * @param uri The URI for the deep link.
 * @param mimeType The mime type for the deep link.
 * @param action The action for the deep link.
 */
public class DeepLinkRequest
internal constructor(
    public val uri: DeepLinkUri?,
    public val mimeType: String?,
    public val action: String?,
) {
    public override fun toString(): String {
        return buildString {
            append("DeepLinkRequest")
            append("{")
            if (uri != null) {
                append(" uri=")
                append(uri.toString())
            }
            if (action != null) {
                append(" action=")
                append(action)
            }
            if (mimeType != null) {
                append(" mimetype=")
                append(mimeType)
            }
            append(" }")
        }
    }

    public companion object {
        /**
         * Creates a [DeepLinkRequest] with a [DeepLinkUri].
         *
         * @param uri The URI for the deep link.
         * @param mimeType The mime type for the deep link.
         * @param action The action for the deep link.
         * @return a [DeepLinkRequest] instance
         */
        @JvmStatic
        public fun fromUri(
            uri: DeepLinkUri,
            mimeType: String? = null,
            action: String? = null,
        ): DeepLinkRequest = DeepLinkRequest(uri, mimeType, action)

        /**
         * Creates a [DeepLinkRequest] with a string uri.
         *
         * @param uri The URI for the deep link.
         * @param mimeType The mime type for the deep link.
         * @param action The action for the deep link.
         * @return a [DeepLinkRequest] instance
         */
        @JvmStatic
        public fun fromUriString(
            uri: String,
            mimeType: String? = null,
            action: String? = null,
        ): DeepLinkRequest = DeepLinkRequest(DeepLinkUri(uri), mimeType, action)

        /**
         * Creates a [DeepLinkRequest with an action.
         *
         * @param uri The URI for the deep link.
         * @param mimeType The mime type for the deep link.
         * @param action The action for the deep link.
         * @return a [DeepLinkRequest] instance
         * @throws IllegalArgumentException if the action is empty.
         */
        @JvmStatic
        public fun fromAction(
            action: String,
            uri: DeepLinkUri? = null,
            mimeType: String? = null,
        ): DeepLinkRequest {
            require(action.isNotEmpty()) { "Cannot create DeepLinkRequest from an empty action." }
            return DeepLinkRequest(uri, mimeType, action)
        }

        /**
         * Creates a [DeepLinkRequest] with a mimeType.
         *
         * @param uri The URI for the deep link.
         * @param mimeType The mime type for the deep link.
         * @param action The action for the deep link.
         * @return a [DeepLinkRequest] instance
         * @throws IllegalArgumentException if the mimeType is empty.
         */
        @JvmStatic
        public fun fromMimeType(
            mimeType: String,
            uri: DeepLinkUri? = null,
            action: String? = null,
        ): DeepLinkRequest {
            require(mimeType.isNotEmpty()) {
                "Cannot create DeepLinkRequest from an empty mimeType."
            }
            return DeepLinkRequest(uri, mimeType, action)
        }
    }
}
