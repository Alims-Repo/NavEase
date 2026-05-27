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
import io.github.alimsrepo.navease.generated.galleryDetailResult
import io.github.alimsrepo.navease.generated.galleryArgs
import io.github.alimsrepo.navease.generated.navigateToGalleryDetail
import io.github.alimsrepo.navease.runtime.annotations.NavEaseArgs
import io.github.alimsrepo.navease.runtime.annotations.NavEaseScreen
import io.github.alimsrepo.navease.runtime.domain.NavScreen
import io.github.alimsrepo.navease.runtime.navigation.NavController
import io.github.alimsrepo.navease.runtime.presentation.LocalNavEaseSharedTransitionScope

data class TechItem(
    val id: Int,
    val title: String,
    val tag: String,
    val description: String,
    val emoji: String,
    val colorIndex: Int,   // 0-3 → primary/secondary/tertiary/error palette
)

val techItems = listOf(
    TechItem(0, "Jetpack Compose", "Declarative UI", "Build beautiful UIs with Kotlin—no XML, no boilerplate. Compose recomposes only what changed, keeping your UI fast and reactive.", "🎨", 0),
    TechItem(1, "KSP", "Code Generation", "Kotlin Symbol Processing runs at compile time to generate type-safe navigation code. Zero reflection, zero runtime overhead.", "⚙️", 1),
    TechItem(2, "Coroutines", "Async / Concurrent", "Structured concurrency built into the Kotlin standard library. Launch, async, and Flow make async code as readable as sequential code.", "🔀", 2),
    TechItem(3, "Navigation 3", "Type-Safe Routing", "Jetpack Navigation3 introduces a backstack API driven by serializable NavKeys—type-safe and fully testable.", "🗺️", 3),
    TechItem(4, "Serialization", "Data Layer", "kotlinx.serialization encodes your screen arguments to a SavedState internally, so deep links and process death are handled automatically.", "📦", 1),
    TechItem(5, "Kotlin Multiplatform", "KMP", "Share 100 % of your business logic and navigation code across Android, iOS, Desktop, and Web from a single Kotlin codebase.", "🌐", 0),
    TechItem(6, "Material 3", "Design System", "MD3 provides adaptive, dynamic color theming and expressive components that feel native on every platform.", "💎", 2),
    TechItem(7, "Shared Transitions", "Animation", "SharedTransitionLayout lets composables animate seamlessly between screens—elements morph in place rather than blinking in and out.", "✨", 3),
)

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
                    title = { Text(args.title, fontWeight = FontWeight.SemiBold) },
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
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
                                Text(if (res.bookmarked) "🔖" else "✖️",
                                     style = MaterialTheme.typography.titleLarge)
                                Column {
                                    Text(
                                        text = if (res.bookmarked) "Bookmarked!" else "Not bookmarked",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (res.bookmarked) MaterialTheme.colorScheme.onSecondaryContainer
                                                else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Result returned from GalleryDetailScreen",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (res.bookmarked) MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "TAP A CARD — WATCH THE SHARED TRANSITION",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                items(techItems, key = { it.id }) { item ->
                    val cardSharedModifier = if (sharedTransitionScope != null && animatedContentScope != null) {
                        with(sharedTransitionScope) {
                            Modifier.sharedBounds(
                                sharedContentState = rememberSharedContentState(key = "gallery_card_${item.id}"),
                                animatedVisibilityScope = animatedContentScope,
                            )
                        }
                    } else Modifier

                    GalleryItemCard(
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

@Composable
fun GalleryItemCard(
    item: TechItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = itemContainerColor(item.colorIndex)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
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
                    color = itemOnContainerColor(item.colorIndex).copy(alpha = 0.7f)
                )
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = itemAccentColor(item.colorIndex).copy(alpha = 0.2f)
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

