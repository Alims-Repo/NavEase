package com.alim.navease

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme

/**
 * Sample application entry point.
 *
 * Screens ([AppScreens]) and their composables live in :shared/commonMain.
 * KSP runs in :shared and generates the factory + NavEaseHost for the Android target.
 * This Activity simply calls the generated host.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }
}
