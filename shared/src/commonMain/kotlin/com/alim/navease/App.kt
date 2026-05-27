package com.alim.navease

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Common app composable used by non-Android platforms (iOS, Web, Desktop).
 *
 * On Android, [MainActivity] calls the KSP-generated `NavEaseHost` directly, so this
 * composable is only used by other platform entry points.
 *
 * For those platforms you can wire up `NavEaseHostInternal` with a platform-specific
 * factory, or replace this with your own cross-platform navigation setup.
 */
@Composable
fun App() {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("NavEase — see the Android app for the full navigation demo.")
        }
    }
}