package com.alim.navease.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.alimsrepo.navease.NavEaseController
import io.github.alimsrepo.navease.NavEaseScreen
import io.github.alimsrepo.navease.Transition
import com.alim.navease.AppScreens

/**
 * Detail screen demonstrating:
 * - Receiving typed arguments via the route key ([AppScreens.Detail.itemId], [AppScreens.Detail.title]).
 * - Passing results back to the previous screen via [NavEaseController.backWithResult].
 *
 * Registered with NavEase via [NavEaseScreen].
 */
@OptIn(ExperimentalMaterial3Api::class)
@NavEaseScreen(route = AppScreens.Detail::class, transition = Transition.SLIDE)
@Composable
fun DetailScreen(navKey: AppScreens.Detail, nav: NavEaseController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail — ${navKey.title}") },
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
                .padding(innerPadding)
                .padding(16.dp),
        ) {
            Text(
                text = "Item ID: ${navKey.itemId}",
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = navKey.title,
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Tap below to navigate back and pass a result to Home.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    // Pass a result back to HomeScreen via backWithResult
                    nav.backWithResult("Visited: ${navKey.title} (id=${navKey.itemId})")
                },
            ) {
                Text("Back with result")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(onClick = { nav.back() }) {
                Text("Back (no result)")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    // Jump straight to Home, clearing the entire back stack above it
                    nav.popUpTo(AppScreens.Home, inclusive = false)
                },
            ) {
                Text("Pop to Home")
            }
        }
    }
}



