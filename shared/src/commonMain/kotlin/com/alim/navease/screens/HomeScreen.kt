package com.alim.navease.screens

import androidx.compose.animation.ExperimentalSharedTransitionApi
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import io.github.alimsrepo.navease.generated.navigateToLibraryDetail
import io.github.alimsrepo.navease.runtime.annotations.NavEaseScreen
import io.github.alimsrepo.navease.runtime.domain.NavScreen
import io.github.alimsrepo.navease.runtime.navigation.NavController
import io.github.alimsrepo.navease.runtime.presentation.LocalNavEaseSharedTransitionScope

@NavEaseScreen(route = "Home")
class HomeScreen : NavScreen() {

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
    @Composable
    override fun Content(navKey: NavKey, navController: NavController) {
        val sharedScope = LocalNavEaseSharedTransitionScope.current
        val animatedScope = LocalNavAnimatedContentScope.current

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "alims-repo",
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "Open Source · Maven Central",
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
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ── About card ─────────────────────────────────────────────────
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "AS",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Alim Sourav",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "KMP · CMP · Android Engineer",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                                    )
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
                            )
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = "Building open-source Kotlin Multiplatform libraries that work across Android, iOS, Desktop, and Web. " +
                                       "All packages published to Maven Central under io.github.alims-repo.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.80f)
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "PUBLISHED LIBRARIES — ${allLibraries.size} PACKAGES",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                    )
                }

                // ── Library cards ──────────────────────────────────────────────
                items(allLibraries) { lib ->
                    val cardSharedModifier = if (sharedScope != null && animatedScope != null) {
                        with(sharedScope) {
                            Modifier.sharedBounds(
                                sharedContentState = rememberSharedContentState(key = "lib_card_${lib.id}"),
                                animatedVisibilityScope = animatedScope,
                            )
                        }
                    } else Modifier

                    LibraryCard(
                        lib = lib,
                        sharedModifier = cardSharedModifier,
                        onClick = {
                            navController.navigateToLibraryDetail(
                                libId = lib.id,
                                libName = lib.name,
                                navTransition = lib.navTransition
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
private fun LibraryCard(
    lib: Library,
    sharedModifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val container = containerColorAt(lib.colorIndex)
    val onContainer = onContainerColorAt(lib.colorIndex)
    val accent = accentColorAt(lib.colorIndex)

    Card(
        modifier = sharedModifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
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
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(accent.copy(alpha = 0.20f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = lib.emoji, style = MaterialTheme.typography.titleLarge)
                }

                Column(modifier = Modifier.weight(1f)) {
                    // Category + version row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = accent.copy(alpha = 0.18f)
                        ) {
                            Text(
                                text = lib.category.label.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = accent,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = "v${lib.version}",
                            style = MaterialTheme.typography.labelSmall,
                            color = onContainer.copy(alpha = 0.55f)
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
            }

            Spacer(Modifier.height(12.dp))

            // Short description
            Text(
                text = lib.description.take(140).let { if (lib.description.length > 140) "$it…" else it },
                style = MaterialTheme.typography.bodySmall,
                color = onContainer.copy(alpha = 0.78f)
            )

            Spacer(Modifier.height(10.dp))

            // Platform chips
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                lib.platforms.forEach { platform ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = accent.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = platform,
                            style = MaterialTheme.typography.labelSmall,
                            color = accent,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    text = lib.artifactIds.joinToString(" + ") { "${lib.groupId}:$it" }.let {
                        if (it.length > 44) "${it.take(44)}…" else it
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = onContainer.copy(alpha = 0.45f),
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }
    }
}

