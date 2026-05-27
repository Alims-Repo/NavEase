package com.alim.navease.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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

private val sampleItems = (1..20).map { index -> "Item #$index" }

/**
 * Home feed screen demonstrating list → detail navigation.
 *
 * Registered with NavEase via [NavEaseScreen] with the default [Transition.SLIDE].
 */
@OptIn(ExperimentalMaterial3Api::class)
@NavEaseScreen(route = AppScreens.Home::class, transition = Transition.SLIDE)
@Composable
fun HomeScreen(navKey: AppScreens.Home, nav: NavEaseController) {
    // Demonstrate result retrieval from Detail screen
    var lastResult by remember { mutableStateOf<String?>(null) }
    val result = nav.getResult<String>()
    if (result != null) lastResult = result

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Home") },
                actions = {
                    TextButton(onClick = { nav.navigate(AppScreens.Settings) }) {
                        Text("Settings")
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
            if (lastResult != null) {
                Text(
                    text = "↩ Returned: $lastResult",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                itemsIndexed(sampleItems) { index, title ->
                    ItemCard(
                        title = title,
                        onClick = {
                            nav.navigate(AppScreens.Detail(itemId = index + 1, title = title))
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ItemCard(title: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tap to open detail →",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
