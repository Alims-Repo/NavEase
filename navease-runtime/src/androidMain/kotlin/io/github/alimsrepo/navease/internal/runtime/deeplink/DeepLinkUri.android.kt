package io.github.alimsrepo.navease.internal.runtime.deeplink

import android.net.Uri
import androidx.core.net.toUri

@Suppress("TypealiasDefinition")
public actual typealias DeepLinkUri = Uri

internal actual object DeepLinkUriUtils {
    actual fun encode(s: String, allow: String?): String = Uri.encode(s, allow)

    actual fun decode(s: String): String = Uri.decode(s)

    actual fun parse(uriString: String): Uri = uriString.toUri()
}
