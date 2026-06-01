package com.alim.navease.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.alimsrepo.navease.runtime.annotations.AutoRegister
import io.github.alimsrepo.navease.runtime.navigation.NavEaseController
import io.github.alimsrepo.navease.runtime.screen.ActivityScreen
import io.github.alimsrepo.navease.runtime.transition.NavTransition

private data class TransitionEntry(
    val name: String,
    val emoji: String,
    val tagline: String,
    val description: String,
    val transition: NavTransition,
    val colorIndex: Int,
)

private val transitions = listOf(
    TransitionEntry("Push",    "↔", "iOS-style horizontal slide",
        "Destination slides in from the trailing edge. Source translates ¼-width toward the leading edge — a subtle parallax that implies depth. Back is the exact mirror.",
        NavTransition.Push, 0),
    TransitionEntry("Fade",    "◎", "Symmetric cross-dissolve",
        "Outgoing screen fades out as incoming fades in. No spatial motion — ideal when screens are associatively related rather than hierarchically nested.",
        NavTransition.Fade, 1),
    TransitionEntry("Rise",    "↑", "Vertical slide from below",
        "Destination slides in from the bottom. Implies upward progress — each step feels like building on the previous. Back reverses: screen drops back down.",
        NavTransition.Rise, 2),
    TransitionEntry("Zoom",    "⊕", "Scale and fade drill-down",
        "Destination scales in from 86% while fading — as if it grows out of the source. Pairs especially well with shared-element transitions for a 'drilling in' feel.",
        NavTransition.Zoom, 3),
    TransitionEntry("Depth",   "◉", "Material 3 shared-axis Z-motion",
        "Forward: destination scales up from 80% as source recedes past 100% and fades — creating a convincing sense of physical layering. Back reverses this.",
        NavTransition.Depth, 4),
    TransitionEntry("Instant", "⚡", "Zero animation",
        "Screens swap immediately with zero delay. Useful for UI tests, deep-stack resets, or completed-flow returns where motion would feel redundant.",
        NavTransition.Instant, 5),
)

@AutoRegister
class NavEaseDemoScreen : ActivityScreen<AppScreens.Transitions>() {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content(navKey: AppScreens.Transitions, navEaseController: NavEaseController) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("NavTransition", fontWeight = FontWeight.SemiBold)
                            Text(
                                "6 built-in animation styles",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                    navigationIcon = {
                        NavBackButton(onClick = { navEaseController.back() })
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                )
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Text(
                            text = "Tap a card — the selected NavTransition animates the transition to the preview screen, with zero inner animations so you see only the nav motion.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        )
                    }
                }

                items(transitions) { t ->
                    val container   = containerColorAt(t.colorIndex)
                    val onContainer = onContainerColorAt(t.colorIndex)
                    val accent      = accentColorAt(t.colorIndex)
                    val onAccent    = onAccentColorAt(t.colorIndex)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = container),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(accent.copy(alpha = 0.18f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(t.emoji, style = MaterialTheme.typography.titleLarge, color = accent)
                                }
                                Column {
                                    Text(
                                        "NavTransition.${t.name}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = onContainer,
                                    )
                                    Text(
                                        t.tagline,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = onContainer.copy(alpha = 0.70f),
                                    )
                                }
                            }

                            Spacer(Modifier.height(10.dp))

                            Text(
                                text = t.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = onContainer.copy(alpha = 0.80f),
                            )

                            Spacer(Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    navEaseController.navigate(
                                        AppScreens.TransitionPreview(
                                            transitionName = t.name,
                                            tagline = t.tagline,
                                        ),
                                        navTransition = t.transition,
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = accent,
                                    contentColor = onAccent,
                                ),
                            ) {
                                Text("Try ${t.name} →", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}
