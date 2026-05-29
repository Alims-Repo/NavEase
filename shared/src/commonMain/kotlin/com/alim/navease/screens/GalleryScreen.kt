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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import io.github.alimsrepo.navease.generated.galleryArgs
import io.github.alimsrepo.navease.generated.galleryDetailResult
import io.github.alimsrepo.navease.generated.navigateToGalleryDetail
import io.github.alimsrepo.navease.runtime.annotations.NavEaseArgs
import io.github.alimsrepo.navease.runtime.annotations.NavEaseScreen
import io.github.alimsrepo.navease.runtime.domain.NavScreen
import io.github.alimsrepo.navease.runtime.navigation.NavController
import io.github.alimsrepo.navease.runtime.presentation.LocalNavEaseSharedTransitionScope
import io.github.alimsrepo.navease.runtime.presentation.NavTransition

// ── Category data ─────────────────────────────────────────────────────────────

data class TechItem(
    val id: Int,
    val title: String,
    val tag: String,
    val description: String,
    val emoji: String,
    val colorIndex: Int,
    val transition: NavTransition = NavTransition.Push,
    val libraryCount: Int = 5,
)

val techItems = listOf(
    TechItem(
        id = 0,
        title = "Networking",
        tag = "HTTP · WebSockets · REST",
        description = "Async HTTP clients, server frameworks, REST & GraphQL APIs for Kotlin Multiplatform. " +
            "Libraries in this category cover everything from simple GET requests to full-duplex WebSocket connections.",
        emoji = "🌐",
        colorIndex = 0,
        transition = NavTransition.Push,
        libraryCount = 6,
    ),
    TechItem(
        id = 1,
        title = "UI Frameworks",
        tag = "Compose · Components · Theming",
        description = "Declarative UI toolkits, component libraries, and design system implementations for KMP. " +
            "Share your UI code across Android, iOS, Desktop, and Web with a single Compose-based codebase.",
        emoji = "🎨",
        colorIndex = 1,
        transition = NavTransition.Zoom,
        libraryCount = 7,
    ),
    TechItem(
        id = 2,
        title = "Dependency Injection",
        tag = "IoC · DI · Service Locator",
        description = "Inversion-of-control containers and dependency injection frameworks designed for KMP. " +
            "Pure Kotlin, zero reflection, compile-time safety — inject your way to cleaner architecture.",
        emoji = "💉",
        colorIndex = 2,
        transition = NavTransition.Rise,
        libraryCount = 4,
    ),
    TechItem(
        id = 3,
        title = "Database & Storage",
        tag = "SQL · NoSQL · KV Store",
        description = "Type-safe SQL, embedded NoSQL, and key-value storage solutions that run on all KMP targets. " +
            "From SQLite-backed persistence to encrypted preferences — fully multiplatform.",
        emoji = "🗄️",
        colorIndex = 3,
        transition = NavTransition.Fade,
        libraryCount = 5,
    ),
    TechItem(
        id = 4,
        title = "Async & Concurrency",
        tag = "Coroutines · Flow · Channels",
        description = "Structured concurrency tools built around Kotlin Coroutines and Flow. " +
            "Reactive streams, cancellation, error propagation, and backpressure handling — all multiplatform.",
        emoji = "⚡",
        colorIndex = 2,
        transition = NavTransition.Depth,
        libraryCount = 5,
    ),
    TechItem(
        id = 5,
        title = "Serialization",
        tag = "JSON · CBOR · Protobuf",
        description = "Format-agnostic, compile-time-safe serialization for Kotlin data classes. " +
            "JSON, CBOR, Protobuf, and more — with full multiplatform support and no runtime reflection.",
        emoji = "📦",
        colorIndex = 1,
        transition = NavTransition.Push,
        libraryCount = 4,
    ),
    TechItem(
        id = 6,
        title = "Testing",
        tag = "Unit · Mocking · UI Tests",
        description = "Testing frameworks, assertion libraries, mocking tools and UI testing utilities for KMP. " +
            "Write tests once, run them everywhere — with great IDE integration and coroutine test support.",
        emoji = "🧪",
        colorIndex = 0,
        transition = NavTransition.Zoom,
        libraryCount = 5,
    ),
    TechItem(
        id = 7,
        title = "Architecture",
        tag = "MVI · MVVM · State Mgmt",
        description = "Architecture patterns, state management, and navigation libraries for scalable KMP apps. " +
            "From lightweight unidirectional data flow to full-featured decomposed navigation stacks.",
        emoji = "🏗️",
        colorIndex = 3,
        transition = NavTransition.Rise,
        libraryCount = 6,
    ),
)

// ── Color helpers ─────────────────────────────────────────────────────────────

@Composable
internal fun itemContainerColor(colorIndex: Int) = when (colorIndex % 4) {
    0 -> MaterialTheme.colorScheme.primaryContainer
    1 -> MaterialTheme.colorScheme.secondaryContainer
    2 -> MaterialTheme.colorScheme.tertiaryContainer
    else -> MaterialTheme.colorScheme.errorContainer
}

@Composable
internal fun itemOnContainerColor(colorIndex: Int) = when (colorIndex % 4) {
    0 -> MaterialTheme.colorScheme.onPrimaryContainer
    1 -> MaterialTheme.colorScheme.onSecondaryContainer
    2 -> MaterialTheme.colorScheme.onTertiaryContainer
    else -> MaterialTheme.colorScheme.onErrorContainer
}

@Composable
internal fun itemAccentColor(colorIndex: Int) = when (colorIndex % 4) {
    0 -> MaterialTheme.colorScheme.primary
    1 -> MaterialTheme.colorScheme.secondary
    2 -> MaterialTheme.colorScheme.tertiary
    else -> MaterialTheme.colorScheme.error
}

@Composable
internal fun itemOnAccentColor(colorIndex: Int) = when (colorIndex % 4) {
    0 -> MaterialTheme.colorScheme.onPrimary
    1 -> MaterialTheme.colorScheme.onSecondary
    2 -> MaterialTheme.colorScheme.onTertiary
    else -> MaterialTheme.colorScheme.onError
}

// ── Screen ────────────────────────────────────────────────────────────────────

@NavEaseScreen(route = "Gallery")
class GalleryScreen : NavScreen() {

    @NavEaseArgs
    data class Args(val title: String)

    @OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
    @Composable
    override fun Content(navKey: NavKey, navController: NavController) {
        val args = navKey.galleryArgs()
        val detailResult by navController.galleryDetailResult()

        val sharedTransitionScope = LocalNavEaseSharedTransitionScope.current
        val animatedContentScope = LocalNavAnimatedContentScope.current

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(args.title, fontWeight = FontWeight.SemiBold)
                            Text(
                                "Tap a category to see its libraries",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ── Bookmarked result banner ────────────────────────────────────
                detailResult?.let { res ->
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = if (res.bookmarked) MaterialTheme.colorScheme.secondaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    if (res.bookmarked) "🔖" else "✖️",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Column {
                                    Text(
                                        text = if (res.bookmarked) "Library bookmarked!" else "Library skipped",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (res.bookmarked) MaterialTheme.colorScheme.onSecondaryContainer
                                                else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Result returned from Category Detail via typed NavEase result",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (res.bookmarked)
                                            MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.70f)
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f)
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Intro hint card ─────────────────────────────────────────────
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                "🎬  Per-Screen NavTransition",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Each category below uses a different NavTransition. " +
                                "Networking → Push, UI → Zoom, DI → Rise, DB → Fade, Async → Depth …",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f)
                            )
                        }
                    }
                }

                item {
                    Text(
                        "TAP A CATEGORY — WATCH THE TRANSITION",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                // ── Category cards ──────────────────────────────────────────────
                items(techItems, key = { it.id }) { item ->
                    val cardSharedModifier = if (sharedTransitionScope != null && animatedContentScope != null) {
                        with(sharedTransitionScope) {
                            Modifier.sharedBounds(
                                sharedContentState = rememberSharedContentState(
                                    key = "gallery_card_${item.id}"
                                ),
                                animatedVisibilityScope = animatedContentScope,
                                enter = GallerySharedEnterFade,
                                exit = SharedExitFade,
                                boundsTransform = GalleryHeroBoundsTransform,
                            )
                        }
                    } else Modifier

                    CategoryCard(
                        item = item,
                        modifier = cardSharedModifier,
                        onClick = {
                            navController.navigateToGalleryDetail(
                                itemId = item.id,
                                itemTitle = item.title,
                                itemTag = item.tag,
                                description = item.description,
                                emoji = item.emoji,
                                colorIndex = item.colorIndex,
                                navTransition = item.transition,
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
private fun CategoryCard(item: TechItem, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = itemContainerColor(item.colorIndex)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(itemAccentColor(item.colorIndex)),
                contentAlignment = Alignment.Center
            ) {
                Text(item.emoji, style = MaterialTheme.typography.titleLarge)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = itemOnContainerColor(item.colorIndex)
                )
                Text(
                    text = item.tag,
                    style = MaterialTheme.typography.bodySmall,
                    color = itemOnContainerColor(item.colorIndex).copy(alpha = 0.70f)
                )
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = itemAccentColor(item.colorIndex).copy(alpha = 0.18f)
                    ) {
                        Text(
                            text = "${item.libraryCount} libraries",
                            style = MaterialTheme.typography.labelSmall,
                            color = itemAccentColor(item.colorIndex),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                    val transitionName = when (item.transition) {
                        NavTransition.Push -> "Push →"
                        NavTransition.Zoom -> "Zoom ⊕"
                        NavTransition.Rise -> "Rise ↑"
                        NavTransition.Fade -> "Fade ◎"
                        NavTransition.Depth -> "Depth ◉"
                        else -> "Instant ⚡"
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = itemAccentColor(item.colorIndex).copy(alpha = 0.10f)
                    ) {
                        Text(
                            text = transitionName,
                            style = MaterialTheme.typography.labelSmall,
                            color = itemAccentColor(item.colorIndex),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = itemAccentColor(item.colorIndex).copy(alpha = 0.18f)
            ) {
                Text(
                    text = "→",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = itemAccentColor(item.colorIndex),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun GalleryItemCard(item: TechItem, modifier: Modifier = Modifier, onClick: () -> Unit) {
    CategoryCard(item = item, modifier = modifier, onClick = onClick)
}
