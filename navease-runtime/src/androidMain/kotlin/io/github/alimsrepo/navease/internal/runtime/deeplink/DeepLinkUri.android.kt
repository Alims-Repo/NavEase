/*
 * Copyright 2024 The Android Open Source Project
 * Copyright 2026 NavEase Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This file is derived from AndroidX Navigation 3 and has been modified for
 * NavEase: repackaged under io.github.alimsrepo.navease.internal, and trimmed
 * to what NavEase uses.
 */
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
