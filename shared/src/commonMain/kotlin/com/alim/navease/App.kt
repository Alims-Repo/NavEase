package com.alim.navease

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Shared placeholder composable.
 *
 * The Android sample app no longer uses this — it calls the generated [NavEaseHost] directly.
 * This file exists so the shared module compiles across all platforms (iOS, Web, Desktop).
 */
@Composable
fun App() {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("NavEase — run the Android app to see the full demo.")
        }
    }
}