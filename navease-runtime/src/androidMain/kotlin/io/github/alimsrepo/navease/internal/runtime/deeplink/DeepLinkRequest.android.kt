package io.github.alimsrepo.navease.internal.runtime.deeplink

import android.content.Intent

/**
 * Creates a [DeepLinkRequest] with an [Intent].
 *
 * @param intent The Intent with the metadata to add to the DeepLinkRequest
 * @return a [DeepLinkRequest] instance
 */
public fun DeepLinkRequest.Companion.fromIntent(intent: Intent): DeepLinkRequest =
    DeepLinkRequest(uri = intent.data, mimeType = intent.type, action = intent.action)
