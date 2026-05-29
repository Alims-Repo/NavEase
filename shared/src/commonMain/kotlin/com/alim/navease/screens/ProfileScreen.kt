package com.alim.navease.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import io.github.alimsrepo.navease.generated.backWithProfileResult
import io.github.alimsrepo.navease.generated.profileArgs
import io.github.alimsrepo.navease.runtime.annotations.NavEaseArgs
import io.github.alimsrepo.navease.runtime.annotations.NavEaseResult
import io.github.alimsrepo.navease.runtime.annotations.NavEaseScreen
import io.github.alimsrepo.navease.runtime.domain.NavScreen
import io.github.alimsrepo.navease.runtime.navigation.NavController
import io.github.alimsrepo.navease.runtime.presentation.LocalNavEaseSharedTransitionScope

@NavEaseScreen(route = "Profile")
class ProfileScreen : NavScreen() {

    @NavEaseArgs
    data class Args(val username: String, val bio: String)

    @NavEaseResult
    data class Result(val followed: Boolean)

    @OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class,
           ExperimentalLayoutApi::class)
    @Composable
    override fun Content(navKey: NavKey, navController: NavController) {
        val args = navKey.profileArgs()

        val sharedTransitionScope = LocalNavEaseSharedTransitionScope.current
        val animatedContentScope = LocalNavAnimatedContentScope.current

        var contentVisible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { contentVisible = true }

        val statsAlpha by animateFloatAsState(
            targetValue = if (contentVisible) 1f else 0f,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            label = "stats_alpha"
        )
        val statsScale by animateFloatAsState(
            targetValue = if (contentVisible) 1f else 0.85f,
            animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMediumLow),
            label = "stats_scale"
        )

        // Shared bounds — avatar flies from Home screen
        val avatarSharedModifier = if (sharedTransitionScope != null && animatedContentScope != null) {
            with(sharedTransitionScope) {
                Modifier.sharedBounds(
                    sharedContentState = rememberSharedContentState(
                        key = "profile_avatar_${args.username}"
                    ),
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
                    title = { Text("Developer Profile", fontWeight = FontWeight.SemiBold) },
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

                // ── Profile hero ───────────────────────────────────────────────
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Avatar — shared bounds with Home screen
                        Box(
                            modifier = avatarSharedModifier
                                .size(92.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = args.username.take(2).uppercase(),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "@${args.username}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "KMP Developer",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                        ) {
                            Text(
                                text = args.bio,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                            )
                        }

                        // Location + GitHub chip row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf("📍 Remote", "🐙 GitHub", "🌐 KMP").forEach { chip ->
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        chip,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Stats row (animated entrance) ──────────────────────────────
                AnimatedVisibility(
                    visible = contentVisible,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 })
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer { alpha = statsAlpha; scaleX = statsScale; scaleY = statsScale },
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(
                            Triple("42", "Starred", MaterialTheme.colorScheme.primaryContainer),
                            Triple("18", "Bookmarks", MaterialTheme.colorScheme.secondaryContainer),
                            Triple("7", "Categories", MaterialTheme.colorScheme.tertiaryContainer),
                        ).forEach { (value, label, color) ->
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                color = color
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = value,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Tech stack ─────────────────────────────────────────────────
                AnimatedVisibility(
                    visible = contentVisible,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 })
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                "TECH STACK",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Spacer(Modifier.height(12.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    "Kotlin" to MaterialTheme.colorScheme.primaryContainer,
                                    "Compose MP" to MaterialTheme.colorScheme.secondaryContainer,
                                    "Ktor" to MaterialTheme.colorScheme.tertiaryContainer,
                                    "Koin" to MaterialTheme.colorScheme.errorContainer,
                                    "SQLDelight" to MaterialTheme.colorScheme.primaryContainer,
                                    "Coroutines" to MaterialTheme.colorScheme.secondaryContainer,
                                    "KSP" to MaterialTheme.colorScheme.tertiaryContainer,
                                    "NavEase" to MaterialTheme.colorScheme.primaryContainer,
                                ).forEach { (tech, color) ->
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = color
                                    ) {
                                        Text(
                                            tech,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Achievements ───────────────────────────────────────────────
                AnimatedVisibility(
                    visible = contentVisible,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 })
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                "ACHIEVEMENTS",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Spacer(Modifier.height(12.dp))
                            listOf(
                                Triple("🏆", "KMP Pioneer", "Building multiplatform apps since day one"),
                                Triple("🚀", "NavEase Early Adopter", "Using KSP-powered navigation"),
                                Triple("⭐", "Compose Multiplatform Pro", "100% shared UI code"),
                                Triple("🔥", "Open Source Contributor", "10+ merged pull requests"),
                            ).forEachIndexed { i, (emoji, title, subtitle) ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.padding(vertical = 6.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(emoji, style = MaterialTheme.typography.titleSmall)
                                        }
                                    }
                                    Column {
                                        Text(
                                            title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            subtitle,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                if (i < 3) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(start = 52.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Shared element explainer ───────────────────────────────────
                AnimatedVisibility(
                    visible = contentVisible,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 })
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.50f)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                "SHARED ELEMENT — AVATAR",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "The avatar above flew from the Home screen's welcome card. " +
                                       "Both elements use the same shared-content key — NavEase passes the " +
                                       "SharedTransitionScope through LocalNavEaseSharedTransitionScope so you never " +
                                       "need to wire it manually.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = "// Home screen\nModifier.sharedBounds(\n" +
                                       "    key = \"profile_avatar_${args.username}\",\n" +
                                       "    animatedVisibilityScope = animatedContentScope\n)\n\n" +
                                       "// This screen\nModifier.sharedBounds(\n" +
                                       "    key = \"profile_avatar_${args.username}\",\n" +
                                       "    animatedVisibilityScope = animatedContentScope\n)",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
                            )
                        }
                    }
                }

                // ── Follow result ──────────────────────────────────────────────
                AnimatedVisibility(
                    visible = contentVisible,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 })
                ) {
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
                                "Follow this developer? Your choice returns as a typed Result to Home.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(16.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { navController.backWithProfileResult(followed = true) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) { Text("➕  Follow") }
                                OutlinedButton(
                                    onClick = { navController.backWithProfileResult(followed = false) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) { Text("Skip") }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

