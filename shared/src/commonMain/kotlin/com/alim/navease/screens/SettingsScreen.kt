package com.alim.navease.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.alimsrepo.navease.NavEaseController
import io.github.alimsrepo.navease.NavEaseScreen
import io.github.alimsrepo.navease.Transition
import com.alim.navease.AppScreens

/**
 * Settings screen demonstrating a secondary route with [Transition.FADE].
 */
@OptIn(ExperimentalMaterial3Api::class)
@NavEaseScreen(route = AppScreens.Settings::class, transition = Transition.FADE)
@Composable
fun SettingsScreen(navKey: AppScreens.Settings, nav: NavEaseController) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var darkModeEnabled by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    TextButton(onClick = { nav.back() }) {
                        Text("← Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Text(
                text = "Preferences",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
            ListItem(
                headlineContent = { Text("Notifications") },
                supportingContent = { Text("Receive push notifications") },
                trailingContent = {
                    Switch(checked = notificationsEnabled, onCheckedChange = { notificationsEnabled = it })
                },
            )
            HorizontalDivider()
            ListItem(
                headlineContent = { Text("Dark Mode") },
                supportingContent = { Text("Follow system theme") },
                trailingContent = {
                    Switch(checked = darkModeEnabled, onCheckedChange = { darkModeEnabled = it })
                },
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "NavEase v1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
    }
}
