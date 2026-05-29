package com.alim.navease.screens

import androidx.compose.animation.AnimatedContentScope
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import io.github.alimsrepo.navease.generated.backWithMainResult
import io.github.alimsrepo.navease.generated.detailResult
import io.github.alimsrepo.navease.generated.galleryDetailResult
import io.github.alimsrepo.navease.generated.mainArgs
import io.github.alimsrepo.navease.generated.navigateToAnimations
import io.github.alimsrepo.navease.generated.navigateToDetail
import io.github.alimsrepo.navease.generated.navigateToGallery
import io.github.alimsrepo.navease.generated.navigateToProfile
import io.github.alimsrepo.navease.generated.profileResult
import io.github.alimsrepo.navease.runtime.annotations.NavEaseArgs
import io.github.alimsrepo.navease.runtime.annotations.NavEaseResult
import io.github.alimsrepo.navease.runtime.annotations.NavEaseScreen
import io.github.alimsrepo.navease.runtime.domain.NavScreen
import io.github.alimsrepo.navease.runtime.navigation.NavController
import io.github.alimsrepo.navease.runtime.presentation.LocalNavEaseSharedTransitionScope
import io.github.alimsrepo.navease.runtime.presentation.NavTransition

// ── Featured library data ─────────────────────────────────────────────────────

private data class FeaturedLibrary(
    val name: String,
    val author: String,
    val tagline: String,
    val fullDescription: String,
    val emoji: String,
    val stars: String,
    val latestVersion: String,
    val platforms: List<String>,
    val codeSnippet: String,
    val transition: NavTransition,
    val colorIndex: Int,
)

private val featuredLibraries = listOf(
    FeaturedLibrary(
        name = "Ktor",
        author = "JetBrains",
        tagline = "Async HTTP client & server for KMP",
        fullDescription = "Ktor is an asynchronous framework for creating microservices, web applications and more — written in and for Kotlin. It runs on JVM, Android, iOS, JavaScript and native targets with a single, unified API.\n\nKtor's plugin system lets you add features like authentication, serialization, content negotiation, and WebSocket support with just a few lines. The coroutine-first design means your code stays readable and performant even under heavy concurrent load.",
        emoji = "🌐",
        stars = "12.4K",
        latestVersion = "3.0.3",
        platforms = listOf("Android", "iOS", "JVM", "JS", "Native"),
        codeSnippet = "val client = HttpClient {\n    install(ContentNegotiation) {\n        json()\n    }\n}\nval users = client.get(\"https://api.example.com/users\")\n    .body<List<User>>()",
        transition = NavTransition.Push,
        colorIndex = 0,
    ),
    FeaturedLibrary(
        name = "Compose Multiplatform",
        author = "JetBrains",
        tagline = "One UI codebase, every platform",
        fullDescription = "Compose Multiplatform brings Jetpack Compose's declarative UI model to iOS, Desktop (macOS, Windows, Linux), Web (Wasm), and of course Android — sharing 100% of your UI code across all targets.\n\nMaterial 3 theming, animations, gesture handling, and the full Compose component library work identically on every platform. No wrappers, no compromises — just Kotlin.",
        emoji = "🎨",
        stars = "15.7K",
        latestVersion = "1.7.3",
        platforms = listOf("Android", "iOS", "Desktop", "Web"),
        codeSnippet = "@Composable\nfun App() {\n    MaterialTheme {\n        // Runs on Android, iOS,\n        // Desktop and Web!\n        MyScreen()\n    }\n}",
        transition = NavTransition.Zoom,
        colorIndex = 1,
    ),
    FeaturedLibrary(
        name = "SQLDelight",
        author = "Cash App",
        tagline = "Type-safe SQL you'll actually enjoy",
        fullDescription = "SQLDelight generates type-safe Kotlin APIs from your SQL statements. Write SQL, get Kotlin. Schema migrations, verify SQL at compile time, and run the same database code on Android, iOS, JVM and more.\n\nThe IDE plugin provides auto-complete, syntax highlighting, and refactoring support directly in your .sq files. No ORMs, no magic — just SQL done right.",
        emoji = "🗄️",
        stars = "6.1K",
        latestVersion = "2.0.2",
        platforms = listOf("Android", "iOS", "JVM", "Native"),
        codeSnippet = "-- schema.sq\nCREATE TABLE User (\n  id INTEGER PRIMARY KEY,\n  name TEXT NOT NULL\n);\n\nselectAll:\nSELECT * FROM User;",
        transition = NavTransition.Rise,
        colorIndex = 2,
    ),
)

// ── Screen ────────────────────────────────────────────────────────────────────

@NavEaseScreen(route = "Main")
class MainScreen : NavScreen() {

    @NavEaseArgs
    data class Args(val userId: String, val age: Int)

    @NavEaseResult
    data class Result(val value: Int)

    @OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
    @Composable
    override fun Content(navKey: NavKey, navController: NavController) {
        val args = navKey.mainArgs()
        val detailResult by navController.detailResult()
        val galleryDetailResult by navController.galleryDetailResult()
        val profileResult by navController.profileResult()

        val sharedTransitionScope = LocalNavEaseSharedTransitionScope.current
        val animatedContentScope = LocalNavAnimatedContentScope.current

        val avatarSharedModifier = if (sharedTransitionScope != null && animatedContentScope != null) {
            with(sharedTransitionScope) {
                Modifier.sharedBounds(
                    sharedContentState = rememberSharedContentState(key = "profile_avatar_${args.userId}"),
                    animatedVisibilityScope = animatedContentScope,
                    enter = SharedEnterFade,
                    exit = SharedExitFade,
                    boundsTransform = AvatarBoundsTransform,
                )
            }
        } else Modifier

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("KMP Hub", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                            Text("Kotlin Multiplatform Libraries", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // ── Developer welcome card ─────────────────────────────────────
                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Avatar — shared element with ProfileScreen
                            Box(
                                modifier = avatarSharedModifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = args.userId.take(2).uppercase(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Welcome, @${args.userId}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Member since ${args.age}  ·  42 libraries bookmarked",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.70f)
                                )
                            }
                        }
                        OutlinedButton(
                            onClick = {
                                navController.navigateToProfile(
                                    username = args.userId,
                                    bio = "Kotlin Multiplatform enthusiast · Open-source contributor · Building cross-platform apps since ${args.age}"
                                )
                            },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 20.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("👤  View Developer Profile  →") }
                    }
                }

                // ── Trending libraries ─────────────────────────────────────────
                item {
                    Text(
                        text = "TRENDING THIS WEEK",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                item {
                    Text(
                        text = "Each card uses a different NavTransition — tap to see it!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                featuredLibraries.forEachIndexed { index, lib ->
                    item(key = lib.name) {
                        val cardSharedModifier = if (sharedTransitionScope != null && animatedContentScope != null) {
                            with(sharedTransitionScope) {
                                Modifier.sharedBounds(
                                    sharedContentState = rememberSharedContentState(key = "lib_card_${lib.name}"),
                                    animatedVisibilityScope = animatedContentScope,
                                    enter = SharedEnterFade,
                                    exit = SharedExitFade,
                                    boundsTransform = CardMorphBoundsTransform,
                                )
                            }
                        } else Modifier

                        FeaturedLibraryCard(
                            lib = lib,
                            transitionLabel = when (lib.transition) {
                                NavTransition.Push -> "Push →"
                                NavTransition.Zoom -> "Zoom ⊕"
                                NavTransition.Rise -> "Rise ↑"
                                else -> lib.transition::class.simpleName ?: ""
                            },
                            modifier = cardSharedModifier,
                            onClick = {
                                navController.navigateToDetail(
                                    featureName = lib.name,
                                    description = "${lib.fullDescription}\n\n---\n" +
                                        "Author: ${lib.author}  ·  v${lib.latestVersion}\n" +
                                        "Stars: ⭐ ${lib.stars}\n" +
                                        "Platforms: ${lib.platforms.joinToString(", ")}\n\n" +
                                        "Code example:\n${lib.codeSnippet}",
                                    navTransition = lib.transition,
                                )
                            }
                        )
                    }
                }

                // ── Explore section ────────────────────────────────────────────
                item {
                    Text(
                        text = "EXPLORE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Browse categories card
                        Card(
                            modifier = Modifier.weight(1f).clickable {
                                navController.navigateToGallery(title = "Browse Categories")
                            },
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("🗂️", style = MaterialTheme.typography.headlineMedium)
                                Text(
                                    "Browse by Category",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    "8 categories · 40+ libraries\nShared element transitions",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.70f)
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        "Different transition per category",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        // Transition gallery card
                        Card(
                            modifier = Modifier.weight(1f).clickable {
                                navController.navigateToAnimations()
                            },
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("🎬", style = MaterialTheme.typography.headlineMedium)
                                Text(
                                    "Transition Gallery",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Text(
                                    "6 built-in styles\nInteractive preview",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.70f)
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        "Try each animation live",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Rate experience (back-with-result demo) ────────────────────
                item {
                    Text(
                        text = "RATE YOUR EXPERIENCE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.secondary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("⭐", style = MaterialTheme.typography.bodyMedium)
                                }
                                Column {
                                    Text(
                                        "Back-with-Result Demo",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        "Returns a typed value to SplashScreen",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(Modifier.height(14.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(Modifier.height(14.dp))
                            Text(
                                "How many stars would you give KMP Hub?",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("⭐ 1", "⭐⭐ 2", "⭐⭐⭐ 3").forEachIndexed { i, label ->
                                    FilledTonalButton(
                                        onClick = { navController.backWithMainResult(value = i + 1) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) { Text(label, style = MaterialTheme.typography.labelSmall) }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { navController.back() },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) { Text("← Back (no result)") }
                        }
                    }
                }

                // ── Result banners ────────────────────────────────���───────────
                detailResult?.let { res ->
                    item {
                        ResultBanner(
                            icon = if (res.liked) "👍" else "👎",
                            title = if (res.liked) "Library Liked!" else "Fair enough",
                            subtitle = "Result returned from Library Detail",
                            positive = res.liked
                        )
                    }
                }

                galleryDetailResult?.let { res ->
                    item {
                        ResultBanner(
                            icon = if (res.bookmarked) "🔖" else "✖️",
                            title = if (res.bookmarked) "Library Bookmarked!" else "Not bookmarked",
                            subtitle = "Result returned from Category Detail",
                            positive = res.bookmarked
                        )
                    }
                }

                profileResult?.let { res ->
                    item {
                        ResultBanner(
                            icon = if (res.followed) "➕" else "✖️",
                            title = if (res.followed) "Following @${args.userId}!" else "Not following",
                            subtitle = "Result returned from Developer Profile",
                            positive = res.followed
                        )
                    }
                }

                // ── Back stack inspector ───────────────────────────────────────
                item {
                    Text(
                        text = "NAVIGATION STACK",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            navController.getHistory().forEachIndexed { index, key ->
                                val isTop = index == navController.getHistory().lastIndex
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isTop) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.outlineVariant
                                            )
                                    )
                                    Text(
                                        text = "#$index  ${key::class.simpleName}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isTop) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isTop) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (isTop) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer
                                        ) {
                                            Text(
                                                text = "current",
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                }
                                if (index < navController.getHistory().lastIndex) {
                                    Spacer(Modifier.height(10.dp))
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

// ── Composable helpers ───────────────────────────────────────────────────────

@Composable
private fun FeaturedLibraryCard(
    lib: FeaturedLibrary,
    transitionLabel: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = itemContainerColor(lib.colorIndex)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(itemAccentColor(lib.colorIndex)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(lib.emoji, style = MaterialTheme.typography.titleLarge)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        lib.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = itemOnContainerColor(lib.colorIndex)
                    )
                    Text(
                        lib.tagline,
                        style = MaterialTheme.typography.bodySmall,
                        color = itemOnContainerColor(lib.colorIndex).copy(alpha = 0.70f)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "⭐ ${lib.stars}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = itemOnContainerColor(lib.colorIndex)
                    )
                    Text(
                        "v${lib.latestVersion}",
                        style = MaterialTheme.typography.labelSmall,
                        color = itemOnContainerColor(lib.colorIndex).copy(alpha = 0.60f)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            // Platform chips
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                lib.platforms.take(4).forEach { platform ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = itemAccentColor(lib.colorIndex).copy(alpha = 0.18f)
                    ) {
                        Text(
                            platform,
                            style = MaterialTheme.typography.labelSmall,
                            color = itemAccentColor(lib.colorIndex),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            // Transition badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = itemAccentColor(lib.colorIndex).copy(alpha = 0.12f)
                ) {
                    Text(
                        "Transition: $transitionLabel",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = itemAccentColor(lib.colorIndex),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Text(
                    "by ${lib.author}",
                    style = MaterialTheme.typography.labelSmall,
                    color = itemOnContainerColor(lib.colorIndex).copy(alpha = 0.55f)
                )
            }
        }
    }
}

@Composable
private fun ResultBanner(icon: String, title: String, subtitle: String, positive: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (positive) MaterialTheme.colorScheme.secondaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(icon, style = MaterialTheme.typography.headlineSmall)
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (positive) MaterialTheme.colorScheme.onSecondaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (positive) MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.70f)
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f)
                )
            }
        }
    }
}

