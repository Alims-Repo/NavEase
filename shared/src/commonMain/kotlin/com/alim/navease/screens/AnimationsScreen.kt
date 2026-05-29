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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import io.github.alimsrepo.navease.generated.navigateToDetail
import io.github.alimsrepo.navease.runtime.annotations.NavEaseScreen
import io.github.alimsrepo.navease.runtime.domain.NavScreen
import io.github.alimsrepo.navease.runtime.navigation.NavController
import io.github.alimsrepo.navease.runtime.presentation.NavTransition

// ── Transition showcase data ──────────────────────────────────────────────────

private data class TransitionShowcase(
    val name: String,
    val emoji: String,
    val tagline: String,
    val preview: String,
    val fullDescription: String,
    val bestFor: String,
    val transition: NavTransition,
    val colorIndex: Int,
)

private val transitionShowcases = listOf(
    TransitionShowcase(
        name = "Push",
        emoji = "↔",
        tagline = "iOS-style horizontal slide",
        preview = "→ slides in · ← slides back",
        fullDescription = "Push is the default NavEase transition, inspired by the iOS navigation stack. The destination slides in from the trailing edge while the source translates slightly toward the leading edge — a subtle parallax that creates a sense of depth without Z-axis tricks. Back navigation is the exact mirror image.",
        bestFor = "Hierarchical flows, detail pages, settings sub-screens",
        transition = NavTransition.Push,
        colorIndex = 0,
    ),
    TransitionShowcase(
        name = "Fade",
        emoji = "◎",
        tagline = "Symmetric cross-dissolve",
        preview = "fades out ↔ fades in",
        fullDescription = "Fade is a symmetric cross-dissolve — the outgoing screen fades out as the incoming screen fades in. There is zero spatial motion, so the transition carries no directional meaning. It is calm and unobtrusive.",
        bestFor = "Tab switches, mode toggles (edit/view), unrelated screens",
        transition = NavTransition.Fade,
        colorIndex = 1,
    ),
    TransitionShowcase(
        name = "Rise",
        emoji = "↑",
        tagline = "Vertical slide from below",
        preview = "↑ rises up · ↓ drops back",
        fullDescription = "Rise slides the destination in from the bottom of the screen while the source nudges upward slightly. The motion implies upward progress — perfect for flows where each step feels like 'building on' the previous one. Back navigation drops the screen back down smoothly.",
        bestFor = "Step-by-step flows, onboarding, checkout, form wizards",
        transition = NavTransition.Rise,
        colorIndex = 2,
    ),
    TransitionShowcase(
        name = "Zoom",
        emoji = "⊕",
        tagline = "Scale and fade drill-down",
        preview = "86%→100% expands in · shrinks back",
        fullDescription = "Zoom scales the destination in from 86% while fading it in — as if it grows out of the source. The source fades out without scaling. Back: the destination shrinks away while the source reappears. Gives a 'drilling in' feel that pairs especially well with shared element transitions.",
        bestFor = "Detail views, photo viewers, card expansions",
        transition = NavTransition.Zoom,
        colorIndex = 3,
    ),
    TransitionShowcase(
        name = "Depth",
        emoji = "◉",
        tagline = "Material 3 Z-axis motion",
        preview = "source recedes · destination surfaces",
        fullDescription = "Depth implements the Material Design 3 shared-axis Z-axis motion. Forward: the destination scales up from 80% while the source scales past 100% and fades — it 'recedes' into the background. Back reverses this, giving you a convincing sense of physical layering.",
        bestFor = "Dashboards, feed items, notification-driven navigation",
        transition = NavTransition.Depth,
        colorIndex = 0,
    ),
    TransitionShowcase(
        name = "Instant",
        emoji = "⚡",
        tagline = "Zero animation",
        preview = "swaps immediately with no motion",
        fullDescription = "Instant removes all screen-transition animation. Screens replace each other with zero delay or motion — completely synchronous. This is useful for UI automation tests (animations interfere with test synchronization), deep navigation resets, or flows where motion would feel redundant.",
        bestFor = "UI tests, deep stack resets, completed-flow returns",
        transition = NavTransition.Instant,
        colorIndex = 1,
    ),
)

// ── Screen ────────────────────────────────────────────────────────��───────────

@NavEaseScreen(route = "Animations")
class AnimationsScreen : NavScreen() {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content(navKey: NavKey, navController: NavController) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Transition Gallery", fontWeight = FontWeight.SemiBold) },
                    navigationIcon = {
                        TextButton(onClick = { navController.back() }) { Text("← Back") }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ── Intro card ─────────────────────────────────────────────────
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
                                "NavTransition — 6 Built-in Styles",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Tap any card to navigate to a detail screen using that style. " +
                                "Back to see it reverse. Every navigateToXxx() accepts an optional navTransition.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.80f)
                            )
                            Spacer(Modifier.height(12.dp))
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
                            )
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = "navController.navigateToDetail(\n" +
                                       "    featureName = \"...\",\n" +
                                       "    navTransition = NavTransition.Zoom,\n)",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
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

                // ── One card per transition ────────────────────────────────��───
                items(transitionShowcases) { showcase ->
                    TransitionCard(
                        showcase = showcase,
                        onTry = {
                            navController.navigateToDetail(
                                featureName = "${showcase.name} — ${showcase.tagline}",
                                description = "${showcase.fullDescription}\n\n" +
                                    "Best for: ${showcase.bestFor}\n\n" +
                                    "You used NavTransition.${showcase.name} to open this screen. " +
                                    "Press ← Back to watch the reverse animation.",
                                navTransition = showcase.transition,
                            )
                        }
                    )
                }

                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}

@Composable
private fun TransitionCard(showcase: TransitionShowcase, onTry: () -> Unit) {
    val containerColor = itemContainerColor(showcase.colorIndex)
    val onContainer = itemOnContainerColor(showcase.colorIndex)
    val accent = itemAccentColor(showcase.colorIndex)
    val onAccent = itemOnAccentColor(showcase.colorIndex)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(accent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = showcase.emoji,
                        style = MaterialTheme.typography.titleLarge,
                        color = onAccent
                    )
                }
                Column {
                    Text(
                        text = showcase.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = onContainer
                    )
                    Text(
                        text = showcase.tagline,
                        style = MaterialTheme.typography.bodySmall,
                        color = onContainer.copy(alpha = 0.70f)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Preview chip
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = accent.copy(alpha = 0.18f)
            ) {
                Text(
                    text = showcase.preview,
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    color = accent,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = showcase.fullDescription,
                style = MaterialTheme.typography.bodySmall,
                color = onContainer.copy(alpha = 0.80f)
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "✓  ${showcase.bestFor}",
                style = MaterialTheme.typography.labelSmall,
                color = accent,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(14.dp))

            Button(
                onClick = onTry,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    contentColor = onAccent
                )
            ) {
                Text("Try ${showcase.name} Transition →", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

