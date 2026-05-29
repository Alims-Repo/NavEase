package com.alim.navease.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import io.github.alimsrepo.navease.generated.backWithDetailResult
import io.github.alimsrepo.navease.generated.detailArgs
import io.github.alimsrepo.navease.runtime.annotations.NavEaseArgs
import io.github.alimsrepo.navease.runtime.annotations.NavEaseResult
import io.github.alimsrepo.navease.runtime.annotations.NavEaseScreen
import io.github.alimsrepo.navease.runtime.domain.NavScreen
import io.github.alimsrepo.navease.runtime.navigation.NavController
import io.github.alimsrepo.navease.runtime.presentation.LocalNavEaseSharedTransitionScope

// Helper to parse sections from the description string
private fun parseDescriptionSections(raw: String): Triple<String, String, String> {
    val parts = raw.split("\n\n---\n")
    val body = parts.getOrNull(0) ?: raw
    val meta = parts.getOrNull(1) ?: ""
    // Extract code example after "Code example:\n"
    val codeStart = meta.indexOf("Code example:\n")
    val codeSnippet = if (codeStart >= 0) meta.substring(codeStart + "Code example:\n".length) else ""
    val metaClean = if (codeStart >= 0) meta.substring(0, codeStart).trim() else meta.trim()
    return Triple(body.trim(), metaClean, codeSnippet.trim())
}

@NavEaseScreen(route = "Detail")
class DetailScreen : NavScreen() {

    @NavEaseArgs
    data class Args(val featureName: String, val description: String)

    @NavEaseResult
    data class Result(val liked: Boolean)

    @OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
    @Composable
    override fun Content(navKey: NavKey, navController: NavController) {
        val args = navKey.detailArgs()

        val sharedTransitionScope = LocalNavEaseSharedTransitionScope.current
        val animatedContentScope = LocalNavAnimatedContentScope.current

        // For transition showcase pages, skip inner entrance animation so the nav
        // transition itself is the only motion the user sees. For library pages,
        // a subtle slide-up complements the shared-element morph.
        var contentVisible by remember { mutableStateOf(true) }
        LaunchedEffect(Unit) { contentVisible = true }

        // Detect if this is a transition showcase or a library page
        val isTransitionShowcase = args.featureName.contains("Transition")

        val (bodyText, metaText, codeText) = remember(args.description) {
            parseDescriptionSections(args.description)
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = args.featureName,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            if (!isTransitionShowcase) {
                                Text(
                                    text = "Library Detail",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        TextButton(onClick = { navController.back() }) { Text("← Back") }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AnimatedVisibility(
                    visible = contentVisible,
                    // Transition-showcase screens start already visible (no inner motion)
                    // so only library-detail pages get the subtle slide-up entrance.
                    enter = if (isTransitionShowcase) EnterTransition.None
                            else fadeIn() + slideInVertically(initialOffsetY = { it / 3 })
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                        // ── Hero header (shared bounds from home card) ──────────
                        val heroSharedModifier = if (sharedTransitionScope != null && animatedContentScope != null) {
                            with(sharedTransitionScope) {
                                Modifier.sharedBounds(
                                    sharedContentState = rememberSharedContentState(
                                        key = "lib_card_${args.featureName}"
                                    ),
                                    animatedVisibilityScope = animatedContentScope,
                                    enter = SharedEnterFade,
                                    exit = SharedExitFade,
                                    boundsTransform = CardMorphBoundsTransform,
                                )
                            }
                        } else Modifier

                        Surface(
                            modifier = heroSharedModifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            color = if (isTransitionShowcase) MaterialTheme.colorScheme.tertiaryContainer
                                    else MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(
                                                if (isTransitionShowcase) MaterialTheme.colorScheme.tertiary
                                                else MaterialTheme.colorScheme.primary
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (isTransitionShowcase) "🎬" else "📦",
                                            style = MaterialTheme.typography.headlineMedium
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        // Type chip
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = (if (isTransitionShowcase) MaterialTheme.colorScheme.tertiary
                                                     else MaterialTheme.colorScheme.primary).copy(alpha = 0.22f)
                                        ) {
                                            Text(
                                                text = if (isTransitionShowcase) "ANIMATION" else "KMP LIBRARY",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isTransitionShowcase) MaterialTheme.colorScheme.tertiary
                                                        else MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = args.featureName,
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isTransitionShowcase)
                                                MaterialTheme.colorScheme.onTertiaryContainer
                                            else MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }

                                // Meta row (author, version, stars) — only for library pages
                                if (metaText.isNotBlank()) {
                                    Spacer(Modifier.height(16.dp))
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    metaText.lines().filter { it.isNotBlank() }.forEach { line ->
                                        Text(
                                            text = line,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.80f)
                                        )
                                    }
                                }
                            }
                        }

                        // ── Description / About ────────────────────────────────
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = if (isTransitionShowcase) "HOW IT WORKS" else "ABOUT THIS LIBRARY",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    text = bodyText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // ── Code snippet ───────────────────────────────────────
                        if (codeText.isNotBlank()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.07f)
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Text(
                                        text = "CODE EXAMPLE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    Spacer(Modifier.height(10.dp))
                                    Text(
                                        text = codeText,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.80f)
                                    )
                                }
                            }
                        }

                        // ── Shared transition note (for library pages) ─────────
                        if (!isTransitionShowcase) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Text(
                                        text = "SHARED ELEMENT — CARD MORPH",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = "The hero card above shares its bounds with the library card on the Home screen. " +
                                               "Both use key \"lib_card_${args.featureName}\" — Compose morphs them seamlessly. " +
                                               "NavEase exposes the SharedTransitionScope via " +
                                               "LocalNavEaseSharedTransitionScope so any screen can join the transition.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(10.dp))
                                    Text(
                                        text = "Modifier.sharedBounds(\n" +
                                               "    key = \"lib_card_${args.featureName}\",\n" +
                                               "    animatedVisibilityScope = animatedContentScope\n)",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.70f)
                                    )
                                }
                            }
                        }

                        // ── Transition note (for showcase pages) ───────────────
                        if (isTransitionShowcase) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f)
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Text(
                                        "PER-SCREEN navTransition",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "This screen was opened with a specific NavTransition passed inline — " +
                                        "the global default from NavEaseHost is overridden just for this navigation event. " +
                                        "Press ← Back to watch the reverse animation.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(10.dp))
                                    Text(
                                        text = "navController.navigateToDetail(\n" +
                                               "    featureName = \"...\",\n" +
                                               "    navTransition = NavTransition.${args.featureName.substringBefore(" ")},\n)",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                                    )
                                }
                            }
                        }

                        // ── Back-with-result ───────────────────────────────────
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "BACK WITH RESULT",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = if (isTransitionShowcase)
                                        "Was this animation smooth? Your choice returns as a typed Result to Home."
                                    else
                                        "Did you find this library useful? Your choice returns as a typed Result to Home.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(16.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                Spacer(Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = { navController.backWithDetailResult(liked = true) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.secondary
                                        )
                                    ) { Text(if (isTransitionShowcase) "👍  Smooth!" else "👍  Liked") }
                                    OutlinedButton(
                                        onClick = { navController.backWithDetailResult(liked = false) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) { Text(if (isTransitionShowcase) "👎  Meh" else "👎  Skip") }
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}
