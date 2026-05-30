package com.alim.navease.screens

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionDefaults
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import io.github.alimsrepo.navease.generated.libraryDetailResult
import io.github.alimsrepo.navease.generated.navigateToLibraryDetail
import io.github.alimsrepo.navease.runtime.annotations.NavEaseScreen
import io.github.alimsrepo.navease.runtime.domain.NavScreen
import io.github.alimsrepo.navease.runtime.navigation.NavController
import io.github.alimsrepo.navease.runtime.presentation.LocalNavEaseSharedTransitionScope

/**
 * Main hub screen — shows all libraries in the alims-repo catalogue.
 *
 * Demonstrates:
 * - [libraryDetailResult] — observes the starred result returned by [LibraryDetailScreen]
 * - Shared element transitions — each library card morphs into the hero card on [LibraryDetailScreen]
 * - Per-navigate [io.github.alimsrepo.navease.runtime.presentation.NavTransition] — each library
 *   carries its own preferred transition which is passed through [navigateToLibraryDetail]
 */
@NavEaseScreen(route = "Home")
class HomeScreen : NavScreen() {

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
    @Composable
    override fun Content(navKey: NavKey, navController: NavController) {

        // Observe the result posted by LibraryDetailScreen (one-shot — stays until leaves composition)
        val detailResult by navController.libraryDetailResult()

        val sharedScope   = LocalNavEaseSharedTransitionScope.current
        val animatedScope = LocalNavAnimatedContentScope.current

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "alims-repo",
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "${allLibraries.size} open-source libraries · KMP · CMP · Android",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // ── Result banner ──────────────────────────────────────────────
                detailResult?.let { result ->
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = if (result.starred)
                                MaterialTheme.colorScheme.secondaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = if (result.starred) "⭐  Library starred — thanks!" else "  Skipped",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (result.starred)
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)
                            )
                        }
                    }
                }

                // ── Library catalogue ──────────────────────────────────────────
                items(allLibraries, key = { it.id }) { lib ->
                    val accent = accentColorAt(lib.colorIndex)
                    val container = containerColorAt(lib.colorIndex)
                    val onContainer = onContainerColorAt(lib.colorIndex)

                    // Shared-bounds modifier — morphs into the hero card on LibraryDetailScreen
                    val sharedModifier = if (sharedScope != null && animatedScope != null) {
                        with(sharedScope) {
                            Modifier.sharedBounds(
                                sharedContentState = rememberSharedContentState(key = "lib_card_${lib.id}"),
                                animatedVisibilityScope = animatedScope,
                                resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
                                placeholderSize = SharedTransitionScope.PlaceholderSize.AnimatedSize,
                            )
                        }
                    } else Modifier

                    Card(
                        modifier = sharedModifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigateToLibraryDetail(
                                    libId   = lib.id,
                                    libName = lib.name,
                                    navTransition = lib.navTransition,
                                )
                            },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = container),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // Emoji icon
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(accent.copy(alpha = 0.18f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = lib.emoji,
                                        style = MaterialTheme.typography.headlineMedium
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    // Category chip
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = accent.copy(alpha = 0.16f)
                                    ) {
                                        Text(
                                            text = lib.category.label.uppercase(),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = accent,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(Modifier.height(3.dp))
                                    Text(
                                        text = lib.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = onContainer
                                    )
                                    Text(
                                        text = lib.tagline,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = onContainer.copy(alpha = 0.70f)
                                    )
                                }
                                // Right-arrow indicator
                                Text(
                                    text = "›",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Light,
                                    color = accent.copy(alpha = 0.60f)
                                )
                            }
                            Spacer(Modifier.height(10.dp))
                            // Platform chips
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                lib.platforms.forEach { platform ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = onContainer.copy(alpha = 0.08f)
                                    ) {
                                        Text(
                                            text = platform,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = onContainer.copy(alpha = 0.70f),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                                Spacer(Modifier.weight(1f))
                                // Version badge
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = accent.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "v${lib.version}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.SemiBold,
                                        color = accent,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}


