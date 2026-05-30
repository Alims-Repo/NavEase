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
import androidx.compose.material3.HorizontalDivider
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
import io.github.alimsrepo.navease.runtime.navigation.NavController
import io.github.alimsrepo.navease.runtime.presentation.ActivityScreen
import io.github.alimsrepo.navease.runtime.presentation.NavTransition

private data class TransitionEntry(
    val name: String,
    val emoji: String,
    val tagline: String,
    val motion: String,
    val description: String,
    val bestFor: String,
    val transition: NavTransition,
    val colorIndex: Int,
)

private val transitions = listOf(
    TransitionEntry("Push",    "↔", "iOS-style horizontal slide",
        "→ slides in  ·  ← slides back (¼ parallax)",
        "The default NavEase transition. Destination slides in from the trailing edge while source translates ¼ of the width toward the leading edge — a subtle parallax creating depth without any Z-axis tricks. Back navigation is the exact mirror image. 450 ms, no fade.",
        "Hierarchical flows, settings sub-screens, detail pages",
        NavTransition.Push, 0),
    TransitionEntry("Fade",    "◎", "Symmetric cross-dissolve",
        "fades out  ↔  fades in — no spatial motion",
        "A symmetric cross-dissolve with zero spatial motion. The outgoing screen fades out as the incoming fades in. Carries no directional meaning — calm and unobtrusive. Ideal when the relationship between screens is associative rather than hierarchical.",
        "Tab switches, mode toggles (view/edit), unrelated screens",
        NavTransition.Fade, 1),
    TransitionEntry("Rise",    "↑", "Vertical slide from below",
        "↑ rises up  ·  ↓ drops back",
        "Slides the destination in from the bottom while the source nudges slightly upward. Implies upward progress — perfect for flows where each step feels like building on the previous. Back reverses: destination drops back down and source reappears.",
        "Step-by-step flows, onboarding, checkout, form wizards",
        NavTransition.Rise, 2),
    TransitionEntry("Zoom",    "⊕", "Scale and fade drill-down",
        "86%→100% expands in  ·  shrinks back on pop",
        "Scales the destination in from 86% while fading — as if it grows out of the source. The source fades out without scaling. Back: destination shrinks away while source reappears. Pairs especially well with shared element transitions for a 'drilling in' feel.",
        "Detail views, photo viewers, card expansions",
        NavTransition.Zoom, 3),
    TransitionEntry("Depth",   "◉", "Material 3 shared-axis Z-motion",
        "source recedes 100%→115%  ·  destination surfaces 80%→100%",
        "Implements the Material Design 3 shared-axis Z-axis motion. Forward: destination scales up from 80% while source scales past 100% and fades — it 'recedes' into the background. Back reverses this, giving a convincing sense of physical layering.",
        "Dashboards, feed items, notification-driven navigation",
        NavTransition.Depth, 4),
    TransitionEntry("Instant", "⚡", "Zero animation",
        "screens swap immediately with no motion or delay",
        "Removes all screen-transition animation. Screens replace each other synchronously with zero delay. Useful for UI automation tests (animations block test synchronization), deep navigation resets, or completed-flow returns where motion would feel redundant.",
        "UI tests, deep stack resets, completed-flow returns",
        NavTransition.Instant, 5),
)

@AutoRegister
class NavEaseDemoScreen : ActivityScreen<AppScreens.NavEaseDemo>() {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content(navKey: AppScreens.NavEaseDemo, navController: NavController) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("NavTransition", fontWeight = FontWeight.SemiBold)
                            Text(
                                "6 built-in animation styles",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        NavBackButton(onClick = { navController.back() })
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                "Tap any card — the selected NavTransition opens the preview screen",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            )
                            Spacer(Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f))
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = "navController.navigateToXxx(\n    navTransition = NavTransition.Zoom,\n)",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.80f)
                            )
                        }
                    }
                }

                item {
                    Text(
                        "TAP TO EXPERIENCE EACH STYLE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                items(transitions) { t ->
                    val container = containerColorAt(t.colorIndex)
                    val onContainer = onContainerColorAt(t.colorIndex)
                    val accent = accentColorAt(t.colorIndex)
                    val onAccent = onAccentColorAt(t.colorIndex)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = container),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(accent.copy(alpha = 0.20f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(t.emoji, style = MaterialTheme.typography.titleLarge, color = accent)
                                }
                                Column {
                                    Text(t.name, style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold, color = onContainer)
                                    Text(t.tagline, style = MaterialTheme.typography.bodySmall,
                                        color = onContainer.copy(alpha = 0.70f))
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = accent.copy(alpha = 0.14f)
                            ) {
                                Text(
                                    text = t.motion,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Medium,
                                    color = accent,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = t.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = onContainer.copy(alpha = 0.80f)
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "✓  ${t.bestFor}",
                                style = MaterialTheme.typography.labelSmall,
                                color = accent, fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.height(14.dp))
                            Button(
                                onClick = {
                                    navController.navigate(
                                        AppScreens.TransitionPreview(
                                            transitionName = t.name,
                                            tagline = t.tagline
                                        ),
                                        navTransition = t.transition
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = accent, contentColor = onAccent
                                )
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

