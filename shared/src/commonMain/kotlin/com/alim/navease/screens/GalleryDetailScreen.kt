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

// ── Libraries per category (keyed by category id) ────────────────────────────

private data class LibEntry(val name: String, val stars: String, val description: String)

private val librariesByCategory: Map<Int, List<LibEntry>> = mapOf(
    0 to listOf( // Networking
        LibEntry("Ktor", "12.4K", "Official JetBrains async HTTP client & server with full KMP support and plugin DSL."),
        LibEntry("OkHttp KMP", "4.8K", "Square's battle-tested OkHttp adapted for Kotlin Multiplatform targets."),
        LibEntry("Fuel", "2.3K", "Lightweight Kotlin HTTP networking library with a friendly, fluent API."),
        LibEntry("Apollo Kotlin", "3.7K", "Type-safe GraphQL client for Kotlin with native KMP support and normalized cache."),
        LibEntry("Coil Network", "10.1K", "Image loading via HTTP built on top of Coil 3 with KMP integration."),
        LibEntry("Retrofit KMP", "2.1K", "Type-safe REST client interface inspired by the classic Retrofit, multiplatform edition."),
    ),
    1 to listOf( // UI
        LibEntry("Compose Multiplatform", "15.7K", "JetBrains' KMP UI framework sharing 100% Compose code across all platforms."),
        LibEntry("Coil 3", "10.1K", "Fast, lightweight image loading with Compose Multiplatform support and KMP targets."),
        LibEntry("Voyager", "3.2K", "Pragmatic multiplatform navigation with screen lifecycle and tab navigation."),
        LibEntry("Decompose", "2.8K", "KMP architecture with lifecycle-aware components and navigation support."),
        LibEntry("Material Kolor", "1.4K", "Dynamic Material 3 color generation from seed colors for Compose KMP."),
        LibEntry("Skiko", "2.1K", "Skia-based graphics rendering engine powering Compose Multiplatform on Desktop and Web."),
        LibEntry("Adaptive", "900", "Adaptive layouts and window-size-aware components for Compose Multiplatform."),
    ),
    2 to listOf( // DI
        LibEntry("Koin", "8.9K", "Lightweight DI framework with full KMP support, zero reflection."),
        LibEntry("Kodein", "3.1K", "KOtlin DEpendency INjection — multiplatform DI with a clean Kotlin DSL."),
        LibEntry("kotlin-inject", "1.7K", "Compile-time annotation-based DI with KSP, inspired by Dagger."),
        LibEntry("Hilt KMP", "N/A", "Community bridge layer enabling Hilt-style injection in shared KMP modules."),
    ),
    3 to listOf( // Database
        LibEntry("SQLDelight", "6.1K", "Type-safe multiplatform SQL: write SQL, get Kotlin APIs generated at compile time."),
        LibEntry("Realm Kotlin", "2.4K", "MongoDB Realm's official Kotlin SDK with full KMP support and live queries."),
        LibEntry("Room KMP", "N/A", "Jetpack Room database extended to Kotlin Multiplatform targets."),
        LibEntry("MultiplatformSettings", "1.7K", "Kotlin Multiplatform library for persisting simple key-value data."),
        LibEntry("Exposed", "8.1K", "JetBrains' ORM framework for Kotlin — primarily JVM, adapting to KMP."),
    ),
    4 to listOf( // Async
        LibEntry("Coroutines", "13.2K", "Official Kotlin async library: suspend, async/await, Flow, channels, actors."),
        LibEntry("Flow Extensions", "900", "Additional Flow operators: merge, zip, debounce, switchMap and more."),
        LibEntry("Turbine", "2.4K", "Small testing library for Kotlin Flow — assert emissions and errors easily."),
        LibEntry("Reaktive", "1.8K", "RxJava-like reactive programming for Kotlin Multiplatform."),
        LibEntry("Arrow-fx", "3.2K", "Functional effects and concurrency primitives built on top of Coroutines."),
    ),
    5 to listOf( // Serialization
        LibEntry("kotlinx.serialization", "5.4K", "Official Kotlin compile-time serialization supporting JSON, CBOR, Protobuf and more."),
        LibEntry("Moshi KMP", "1.2K", "Community port of Square's Moshi JSON library for Kotlin Multiplatform."),
        LibEntry("kotlinx.json (strict)", "N/A", "Strict JSON parsing with schema validation built on kotlinx.serialization."),
        LibEntry("Avro4k", "700", "Apache Avro serialization for Kotlin using the kotlinx.serialization framework."),
    ),
    6 to listOf( // Testing
        LibEntry("Kotest", "4.2K", "Powerful Kotlin testing framework with multiplatform runners and property-based tests."),
        LibEntry("MockK", "5.4K", "Mocking library for Kotlin — supports coroutines, extension functions, and KMP."),
        LibEntry("Turbine", "2.4K", "Small testing library for Kotlin Flow — assert emissions and errors easily."),
        LibEntry("Paparazzi", "2.2K", "Snapshot testing library for composables without running on an emulator."),
        LibEntry("Strikt", "1.3K", "Assertion API for Kotlin that is fluent, type-safe and extensible."),
    ),
    7 to listOf( // Architecture
        LibEntry("Decompose", "2.8K", "KMP library for breaking up your code into lifecycle-aware business logic components."),
        LibEntry("MVI Kotlin", "1.4K", "Multiplatform MVI framework with reducers, intents, and Compose integration."),
        LibEntry("Voyager", "3.2K", "Pragmatic multiplatform navigation library with built-in lifecycle and tab support."),
        LibEntry("Ballast", "800", "MVI state management with pluggable side-effect handlers, fully multiplatform."),
        LibEntry("Precompose", "1.6K", "Compose Multiplatform navigation and view-model library inspired by Jetpack."),
        LibEntry("Circuit", "1.1K", "Compose-first UDF architecture from Slack — multiplatform with navigator API."),
    ),
)

// ── Screen ────────────────────────────────────────────────────────────────────

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

        // Shared bounds — matches the category card in GalleryScreen
        val heroSharedModifier = if (sharedTransitionScope != null && animatedContentScope != null) {
            with(sharedTransitionScope) {
                Modifier.sharedBounds(
                    sharedContentState = rememberSharedContentState(key = "gallery_card_${args.itemId}"),
                    animatedVisibilityScope = animatedContentScope,
                    enter = GallerySharedEnterFade,
                    exit = SharedExitFade,
                    boundsTransform = GalleryHeroBoundsTransform,
                )
            }
        } else Modifier

        val libraries = remember(args.itemId) {
            librariesByCategory[args.itemId] ?: emptyList()
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(args.itemTitle, fontWeight = FontWeight.SemiBold)
                            Text(
                                "${libraries.size} libraries",
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

                        // ── Hero card — shared bounds from GalleryScreen ────────
                        Surface(
                            modifier = heroSharedModifier.fillMaxWidth(),
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
                                            .size(68.dp)
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(itemAccentColor(args.colorIndex)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(args.emoji, style = MaterialTheme.typography.headlineMedium)
                                    }
                                    Column {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = itemAccentColor(args.colorIndex).copy(alpha = 0.25f)
                                        ) {
                                            Text(
                                                text = "CATEGORY",
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
                                        Text(
                                            text = args.itemTag,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = itemOnContainerColor(args.colorIndex).copy(alpha = 0.70f)
                                        )
                                    }
                                }
                                Spacer(Modifier.height(16.dp))
                                HorizontalDivider(
                                    color = itemOnContainerColor(args.colorIndex).copy(alpha = 0.12f)
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    text = args.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = itemOnContainerColor(args.colorIndex).copy(alpha = 0.80f)
                                )
                            }
                        }

                        // ── Shared transition explainer ────────────────────────
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = itemContainerColor(args.colorIndex).copy(alpha = 0.45f)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    "SHARED ELEMENT — CATEGORY HERO",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "The hero card above shares its bounds with the category row in the list. " +
                                           "Both use key \"gallery_card_${args.itemId}\". " +
                                           "The compact row expands into a full hero using a soft spring animation.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    text = "// List row\nModifier.sharedBounds(key = \"gallery_card_${args.itemId}\", ...)\n\n" +
                                           "// Hero detail\nModifier.sharedBounds(key = \"gallery_card_${args.itemId}\", ...)",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
                                )
                            }
                        }

                        // ── Library list ───────────────────────────────────────
                        if (libraries.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Text(
                                        "TOP LIBRARIES IN ${args.itemTitle.uppercase()}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    libraries.forEachIndexed { index, lib ->
                                        LibraryRow(lib = lib, accentColor = itemAccentColor(args.colorIndex))
                                        if (index < libraries.lastIndex) {
                                            HorizontalDivider(
                                                modifier = Modifier.padding(top = 10.dp, bottom = 10.dp, start = 44.dp),
                                                color = MaterialTheme.colorScheme.outlineVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // ── Bookmark result ────────────────────────────────────
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    "BACK WITH RESULT",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Bookmark this category? Your choice is returned as a typed Result to the category browser.",
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
                                        onClick = {
                                            navController.backWithGalleryDetailResult(bookmarked = true)
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = itemAccentColor(args.colorIndex),
                                            contentColor = itemOnAccentColor(args.colorIndex)
                                        )
                                    ) { Text("🔖  Bookmark") }
                                    OutlinedButton(
                                        onClick = {
                                            navController.backWithGalleryDetailResult(bookmarked = false)
                                        },
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

@Composable
private fun LibraryRow(lib: LibEntry, accentColor: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            modifier = Modifier.size(32.dp),
            shape = RoundedCornerShape(8.dp),
            color = accentColor.copy(alpha = 0.15f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    "⭐",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    lib.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = accentColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        lib.stars,
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(
                lib.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
