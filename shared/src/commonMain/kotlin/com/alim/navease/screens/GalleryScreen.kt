package com.alim.navease.screens

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.alimsrepo.navease.internal.navigation.ui.LocalNavAnimatedContentScope
import io.github.alimsrepo.navease.runtime.annotations.AutoRegister
import io.github.alimsrepo.navease.runtime.composition.LocalNavEaseSharedTransitionScope
import io.github.alimsrepo.navease.runtime.navigation.NavEaseController
import io.github.alimsrepo.navease.runtime.screen.ActivityScreen

private data class GalleryItem(
    val id: String,
    val emoji: String,
    val title: String,
    val category: String,
    val colorIndex: Int
)

@AutoRegister
class GalleryScreen : ActivityScreen<AppScreens.Gallery>() {

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
    @Composable
    override fun Content(navKey: AppScreens.Gallery, navEaseController: NavEaseController) {
        val sharedScope = LocalNavEaseSharedTransitionScope.current
        val animatedScope = LocalNavAnimatedContentScope.current

        val items = remember {
            listOf(
                GalleryItem("1", "🌄", "Mountain View", "Nature", 0),
                GalleryItem("2", "🌊", "Ocean Waves", "Nature", 1),
                GalleryItem("3", "🏙️", "City Lights", "Urban", 2),
                GalleryItem("4", "🌸", "Cherry Blossom", "Nature", 3),
                GalleryItem("5", "🌅", "Sunset Beach", "Nature", 4),
                GalleryItem("6", "🗻", "Snow Peak", "Nature", 0),
                GalleryItem("7", "🏖️", "Tropical Paradise", "Beach", 1),
                GalleryItem("8", "🌲", "Forest Path", "Nature", 2),
                GalleryItem("9", "🏰", "Ancient Castle", "Architecture", 3),
                GalleryItem("10", "🌃", "Night Skyline", "Urban", 4),
                GalleryItem("11", "🌺", "Tropical Flowers", "Nature", 0),
                GalleryItem("12", "🏔️", "Alpine Vista", "Nature", 1),
                GalleryItem("13", "🌈", "Rainbow Valley", "Nature", 2),
                GalleryItem("14", "🌙", "Moonlit Night", "Sky", 3),
                GalleryItem("15", "⭐", "Starry Sky", "Sky", 4),
                GalleryItem("16", "🔥", "Campfire", "Adventure", 0),
                GalleryItem("17", "🌪️", "Storm Clouds", "Weather", 1),
                GalleryItem("18", "❄️", "Winter Scene", "Season", 2)
            )
        }

        var selectedItem by remember { mutableStateOf<GalleryItem?>(null) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "Gallery",
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                "${items.size} items",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navEaseController.back() }) {
                            Text("←", style = MaterialTheme.typography.headlineMedium)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Stats Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatBadge(label = "Total", value = "${items.size}")
                        StatBadge(label = "Nature", value = "${items.count { it.category == "Nature" }}")
                        StatBadge(label = "Urban", value = "${items.count { it.category == "Urban" }}")
                        StatBadge(label = "Other", value = "${items.size - items.count { it.category == "Nature" || it.category == "Urban" }}")
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Gallery Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(items) { item ->
                        GalleryCard(
                            item = item,
                            isSelected = item == selectedItem,
                            sharedScope = sharedScope,
                            animatedScope = animatedScope,
                            onClick = {
                                selectedItem = item
                                navEaseController.navigate(
                                    AppScreens.Detail(
                                        itemId = item.id,
                                        title = item.title
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBadge(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun GalleryCard(
    item: GalleryItem,
    isSelected: Boolean,
    sharedScope: SharedTransitionScope?,
    animatedScope: androidx.compose.animation.AnimatedVisibilityScope?,
    onClick: () -> Unit
) {
    val colors = listOf(
        Color(0xFFE3F2FD),
        Color(0xFFF3E5F5),
        Color(0xFFFFF3E0),
        Color(0xFFE8F5E9),
        Color(0xFFFCE4EC)
    )

    val sharedModifier = if (sharedScope != null && animatedScope != null) {
        with(sharedScope) {
            Modifier.sharedBounds(
                sharedContentState = rememberSharedContentState(key = "gallery_item_${item.id}"),
                animatedVisibilityScope = animatedScope,
                resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
            )
        }
    } else Modifier

    Card(
        modifier = sharedModifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = colors[item.colorIndex % colors.size]
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = item.emoji,
                    style = MaterialTheme.typography.displaySmall
                )
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = item.category,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(20.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "✓",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

