package com.alim.navease.screens

import androidx.compose.animation.AnimatedVisibility
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
import io.github.alimsrepo.navease.generated.backWithGalleryDetailResult
import io.github.alimsrepo.navease.generated.galleryDetailArgs
import io.github.alimsrepo.navease.runtime.annotations.NavEaseArgs
import io.github.alimsrepo.navease.runtime.annotations.NavEaseResult
import io.github.alimsrepo.navease.runtime.annotations.NavEaseScreen
import io.github.alimsrepo.navease.runtime.domain.NavScreen
import io.github.alimsrepo.navease.runtime.navigation.NavController
import io.github.alimsrepo.navease.runtime.presentation.LocalNavEaseSharedTransitionScope

@NavEaseScreen(route = "GalleryDetail")
class GalleryDetailScreen : NavScreen() {

    @NavEaseArgs
    data class Args(
        val itemId: Int,
        val itemTitle: String,
        val itemTag: String,
        val description: String,
        val emoji: String,
        val colorIndex: Int,
    )

    @NavEaseResult
    data class Result(val bookmarked: Boolean)

    @OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
    @Composable
    override fun Content(navKey: NavKey, navController: NavController) {
        val args = navKey.galleryDetailArgs()

        val sharedTransitionScope = LocalNavEaseSharedTransitionScope.current
        val animatedContentScope = LocalNavAnimatedContentScope.current

        var contentVisible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { contentVisible = true }

        // Shared bounds modifier — matches the card in GalleryScreen
        val headerSharedModifier = if (sharedTransitionScope != null && animatedContentScope != null) {
            with(sharedTransitionScope) {
                Modifier.sharedBounds(
                    sharedContentState = rememberSharedContentState(key = "gallery_card_${args.itemId}"),
                    animatedVisibilityScope = animatedContentScope,
                )
            }
        } else Modifier

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(args.itemTitle, fontWeight = FontWeight.SemiBold) },
                    navigationIcon = {
                        TextButton(onClick = { navController.back() }) { Text("← Back") }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
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
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 3 })
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                        // ── Hero card — shared bounds from GalleryScreen ────────────────
                        Surface(
                            modifier = headerSharedModifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            color = itemContainerColor(args.colorIndex)
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
                                            .background(itemAccentColor(args.colorIndex)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            args.emoji,
                                            style = MaterialTheme.typography.headlineMedium
                                        )
                                    }
                                    Column {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = itemAccentColor(args.colorIndex).copy(alpha = 0.25f)
                                        ) {
                                            Text(
                                                text = args.itemTag.uppercase(),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = itemAccentColor(args.colorIndex),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = args.itemTitle,
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = itemOnContainerColor(args.colorIndex)
                                        )
                                    }
                                }
                            }
                        }

                        // ── Description card ───────────────────────────────────────────
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "OVERVIEW",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    text = args.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // ── Shared transition explainer ────────────────────────────────
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = itemContainerColor(args.colorIndex).copy(alpha = 0.45f)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "HOW THE SHARED TRANSITION WORKS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    text = "The hero card above shares its bounds with the list card in GalleryScreen. " +
                                            "NavEase passes the SharedTransitionScope through " +
                                            "LocalNavEaseSharedTransitionScope so any screen can participate " +
                                            "without extra wiring.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(12.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    text = "// GalleryScreen — list item\nModifier.sharedBounds(\n" +
                                            "    key = \"gallery_card_\${item.id}\",\n" +
                                            "    animatedVisibilityScope = animatedContentScope\n)\n\n" +
                                            "// GalleryDetailScreen — hero\nModifier.sharedBounds(\n" +
                                            "    key = \"gallery_card_\${args.itemId}\",\n" +
                                            "    animatedVisibilityScope = animatedContentScope\n)",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                                )
                            }
                        }

                        // ── Bookmark result ────────────────────────────────────────────
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "BACK WITH RESULT",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Your choice is returned as a typed Result to GalleryScreen.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(16.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                Spacer(Modifier.height(16.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Button(
                                        onClick = { navController.backWithGalleryDetailResult(bookmarked = true) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = itemAccentColor(args.colorIndex),
                                            contentColor = itemOnAccentColor(args.colorIndex)
                                        )
                                    ) { Text("🔖  Bookmark") }
                                    OutlinedButton(
                                        onClick = { navController.backWithGalleryDetailResult(bookmarked = false) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) { Text("Skip") }
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

