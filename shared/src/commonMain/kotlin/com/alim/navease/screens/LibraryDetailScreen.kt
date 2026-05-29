package com.alim.navease.screens

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import io.github.alimsrepo.navease.generated.backWithLibraryDetailResult
import io.github.alimsrepo.navease.generated.libraryDetailArgs
import io.github.alimsrepo.navease.generated.navigateToCrashGuardDemo
import io.github.alimsrepo.navease.generated.navigateToFlowTabDemo
import io.github.alimsrepo.navease.generated.navigateToNavEaseDemo
import io.github.alimsrepo.navease.generated.navigateToPdfDemo
import io.github.alimsrepo.navease.generated.navigateToPrayerTimesDemo
import io.github.alimsrepo.navease.generated.navigateToSecureVaultDemo
import io.github.alimsrepo.navease.runtime.annotations.NavEaseArgs
import io.github.alimsrepo.navease.runtime.annotations.NavEaseResult
import io.github.alimsrepo.navease.runtime.annotations.NavEaseScreen
import io.github.alimsrepo.navease.runtime.domain.NavScreen
import io.github.alimsrepo.navease.runtime.navigation.NavController
import io.github.alimsrepo.navease.runtime.presentation.LocalNavEaseSharedTransitionScope

@NavEaseScreen(route = "LibraryDetail")
class LibraryDetailScreen : NavScreen() {

    @NavEaseArgs
    data class Args(val libId: String, val libName: String)

    @NavEaseResult
    data class Result(val starred: Boolean)

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
    @Composable
    override fun Content(navKey: NavKey, navController: NavController) {
        val args = navKey.libraryDetailArgs()
        val lib = libraryById(args.libId) ?: run {
            // Fallback — should never happen
            Text("Library not found: ${args.libId}")
            return
        }

        val sharedScope = LocalNavEaseSharedTransitionScope.current
        val animatedScope = LocalNavAnimatedContentScope.current

        val accent = accentColorAt(lib.colorIndex)
        val onAccent = onAccentColorAt(lib.colorIndex)
        val container = containerColorAt(lib.colorIndex)
        val onContainer = onContainerColorAt(lib.colorIndex)

        // Shared bounds — morphs from HomeScreen card
        val heroSharedModifier = if (sharedScope != null && animatedScope != null) {
            with(sharedScope) {
                Modifier.sharedBounds(
                    sharedContentState = rememberSharedContentState(key = "lib_card_${lib.id}"),
                    animatedVisibilityScope = animatedScope,
                )
            }
        } else Modifier

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = lib.name,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = lib.category.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        TextButton(onClick = { navController.back() }) { Text("← Back") }
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
                // ── Hero card (shared bounds with HomeScreen card) ──────────
                item {
                    Surface(
                        modifier = heroSharedModifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        color = container
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(accent.copy(alpha = 0.20f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = lib.emoji, style = MaterialTheme.typography.displaySmall)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = accent.copy(alpha = 0.18f)
                                    ) {
                                        Text(
                                            text = lib.category.label.uppercase(),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = accent,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = lib.name,
                                        style = MaterialTheme.typography.headlineSmall,
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

                            Spacer(Modifier.height(16.dp))
                            HorizontalDivider(color = onContainer.copy(alpha = 0.12f))
                            Spacer(Modifier.height(12.dp))

                            // Version + platforms
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = accent.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "v${lib.version}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = FontFamily.Monospace,
                                        color = accent,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                                lib.platforms.forEach { p ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = onContainer.copy(alpha = 0.08f)
                                    ) {
                                        Text(
                                            text = p,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = onContainer.copy(alpha = 0.75f),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ── About ──────────────────────────────────────────────────────
                item {
                    SectionCard(title = "ABOUT") {
                        Text(
                            text = lib.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // ── Features ───────────────────────────────────────────────────
                item {
                    SectionCard(title = "FEATURES") {
                        lib.features.forEachIndexed { i, feature ->
                            if (i > 0) Spacer(Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "✓",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = accent,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = feature,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // ── Maven Central install ──────────────────────────────────────
                item {
                    SectionCard(title = "INSTALLATION — MAVEN CENTRAL") {
                        lib.artifactIds.forEach { artifactId ->
                            Text(
                                text = "${lib.groupId}:$artifactId:${lib.version}",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = accent,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(4.dp))
                        }
                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = lib.installCode,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.80f)
                        )
                    }
                }

                // ── Quick usage ────────────────────────────────────────────────
                item {
                    SectionCard(title = "QUICK USAGE") {
                        Text(
                            text = lib.quickUsageCode,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                        )
                    }
                }

                // ── Links ──────────────────────────────────────────────────────
                item {
                    SectionCard(title = "LINKS") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { /* open lib.githubUrl */ },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("⚙  GitHub", style = MaterialTheme.typography.labelMedium)
                            }
                            OutlinedButton(
                                onClick = { /* open lib.websiteUrl */ },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("🌐  Website", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = lib.githubUrl,
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = lib.websiteUrl,
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                // ── Open Demo ──────────────────────────────────────────────────
                item {
                    Button(
                        onClick = {
                            when (lib.id) {
                                "navease"      -> navController.navigateToNavEaseDemo()
                                "securevault"  -> navController.navigateToSecureVaultDemo()
                                "flowtab"      -> navController.navigateToFlowTabDemo()
                                "prayertimes"  -> navController.navigateToPrayerTimesDemo()
                                "crashguard"   -> navController.navigateToCrashGuardDemo()
                                "pdfgenerator" -> navController.navigateToPdfDemo()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accent,
                            contentColor = onAccent
                        )
                    ) {
                        Text(
                            text = "Open Interactive Demo  →",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                }

                // ── Star / skip ────────────────────────────────────────────────
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "USEFUL?",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Rate this library and return — the result comes back as a typed NavEase Result to the previous screen.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(14.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { navController.backWithLibraryDetailResult(starred = true) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary
                                    )
                                ) { Text("⭐  Star") }
                                OutlinedButton(
                                    onClick = { navController.backWithLibraryDetailResult(starred = false) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) { Text("Skip") }
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}


