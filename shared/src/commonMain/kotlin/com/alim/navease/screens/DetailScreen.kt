package com.alim.navease.screens

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.alimsrepo.navease.internal.navigation.ui.LocalNavAnimatedContentScope
import io.github.alimsrepo.navease.runtime.annotations.AutoRegister
import io.github.alimsrepo.navease.runtime.composition.LocalNavEaseSharedTransitionScope
import io.github.alimsrepo.navease.runtime.navigation.NavEaseController
import io.github.alimsrepo.navease.runtime.navigation.backWithResult
import io.github.alimsrepo.navease.runtime.screen.ActivityScreen
import kotlinx.serialization.Serializable

@AutoRegister
class DetailScreen : ActivityScreen<AppScreens.Detail>() {

    @Serializable
    data class Result(
        val action: String,
        val itemId: String
    )

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
    @Composable
    override fun Content(navKey: AppScreens.Detail, navEaseController: NavEaseController) {
        val sharedScope = LocalNavEaseSharedTransitionScope.current
        val animatedScope = LocalNavAnimatedContentScope.current

        var isFavorited by remember { mutableStateOf(false) }
        var isExpanded by remember { mutableStateOf(false) }

        val sharedModifier = if (sharedScope != null) {
            with(sharedScope) {
                Modifier.sharedBounds(
                    sharedContentState = rememberSharedContentState(key = "gallery_item_${navKey.itemId}"),
                    animatedVisibilityScope = animatedScope,
                    resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
                )
            }
        } else Modifier

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Detail View",
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            navEaseController.backWithResult(
                                Result(action = "viewed", itemId = navKey.itemId)
                            )
                        }) {
                            Text("←", style = MaterialTheme.typography.headlineMedium)
                        }
                    },
                    actions = {
                        IconButton(onClick = { isFavorited = !isFavorited }) {
                            Text(
                                if (isFavorited) "❤️" else "🤍",
                                style = MaterialTheme.typography.headlineSmall
                            )
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
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Hero Card
                Card(
                    modifier = sharedModifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📄", style = MaterialTheme.typography.displayLarge)
                        }

                        Text(
                            navKey.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    "ID: ${navKey.itemId}",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            if (isFavorited) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        "❤️ Favorited",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                // Details Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .animateContentSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "About",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            "This is a detail view for \"${navKey.title}\". " +
                                    "NavEase makes it easy to pass typed arguments like the itemId " +
                                    "(${navKey.itemId}) and title through the navigation hierarchy.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                            maxLines = if (isExpanded) Int.MAX_VALUE else 3
                        )

                        if (isExpanded) {
                            Text(
                                "\n✨ Key Features:\n" +
                                        "• Type-safe navigation with sealed classes\n" +
                                        "• Automatic argument serialization\n" +
                                        "• Typed result handling\n" +
                                        "• Multiplatform support (iOS, Android, Desktop, Web)\n" +
                                        "• Shared element transitions\n" +
                                        "• Deep linking support",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }

                        OutlinedButton(
                            onClick = { isExpanded = !isExpanded },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(if (isExpanded) "Show Less ↑" else "Show More ↓")
                        }
                    }
                }

                // Metadata Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Metadata",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        MetadataRow("Item ID", navKey.itemId)
                        MetadataRow("Title", navKey.title)
                        MetadataRow("Type", "Detail Screen")
                        MetadataRow("Status", if (isFavorited) "Favorited" else "Normal")
                        MetadataRow("Navigation", "ActivityScreen<AppScreens.Detail>")
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            navEaseController.backWithResult(
                                Result(action = "shared", itemId = navKey.itemId)
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("📤 Share", modifier = Modifier.padding(vertical = 4.dp))
                    }

                    Button(
                        onClick = {
                            navEaseController.backWithResult(
                                Result(action = "saved", itemId = navKey.itemId)
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text("💾 Save", modifier = Modifier.padding(vertical = 4.dp))
                    }
                }

                OutlinedButton(
                    onClick = { navEaseController.back() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("← Go Back", modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}

@Composable
private fun MetadataRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
            modifier = Modifier.weight(0.4f)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.weight(0.6f),
            textAlign = TextAlign.End
        )
    }
}


