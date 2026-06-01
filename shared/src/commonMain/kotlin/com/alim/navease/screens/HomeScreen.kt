package com.alim.navease.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.alimsrepo.navease.runtime.annotations.AutoRegister
import io.github.alimsrepo.navease.runtime.navigation.NavEaseController
import io.github.alimsrepo.navease.runtime.navigation.resultOf
import io.github.alimsrepo.navease.runtime.screen.ActivityScreen
import io.github.alimsrepo.navease.runtime.transition.NavTransition

// ── Feature descriptor ────────────────────────────────────────────────────────

private data class Feature(
    val emoji: String,
    val title: String,
    val subtitle: String,
    val codeSnippet: String,
    val colorIndex: Int,
    val navTransition: NavTransition,
    val onClick: NavEaseController.() -> Unit,
)

@AutoRegister
class HomeScreen : ActivityScreen<AppScreens.Home>() {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content(navKey: AppScreens.Home, navEaseController: NavEaseController) {

        // Observe typed result returned by TypedResultDemoScreen
        val typedResult by navEaseController.resultOf<TypedResultDemoScreen.Result>()

        val features = listOf(
            Feature(
                emoji = "🎬",
                title = "Transitions",
                subtitle = "6 built-in animation styles — Push, Fade, Rise, Zoom, Depth, Instant. Override per navigate() call.",
                codeSnippet = "navController.navigate(\n    AppScreens.Detail,\n    navTransition = NavTransition.Zoom,\n)",
                colorIndex = 0,
                navTransition = NavTransition.Fade,
                onClick = { navigate(AppScreens.Transitions) },
            ),
            Feature(
                emoji = "⬇",
                title = "Nested Navigation",
                subtitle = "Inner NavEaseHost<WizardStep> with its own start key and independent back-stack inside a parent screen.",
                codeSnippet = "NavEaseHost<WizardStep>(\n    start = WizardStep.Step1,\n    onExitRequest = { outerNav.back() },\n) { add(Step1()); add(Step2()) }",
                colorIndex = 2,
                navTransition = NavTransition.Zoom,
                onClick = { navigate(AppScreens.NestedNavDemo, navTransition = NavTransition.Zoom) },
            ),
            Feature(
                emoji = "📦",
                title = "Typed Arguments",
                subtitle = "NavKey subclasses are your route arguments. Data is passed as constructor parameters — strongly typed, no strings, no casting.",
                codeSnippet = "@Serializable\ndata class Detail(\n    val id: String,\n    val count: Int,\n) : AppScreens()\n\n// In screen:\nnavKey.id    // ✅ fully typed",
                colorIndex = 1,
                navTransition = NavTransition.Rise,
                onClick = {
                    navigate(
                        AppScreens.TypedArgs(
                            feature = "Typed Arguments",
                            description = "These values were passed via AppScreens.TypedArgs — no bundles, no strings, no casting.",
                        ),
                        navTransition = NavTransition.Rise,
                    )
                },
            ),
            Feature(
                emoji = "🔁",
                title = "Typed Results",
                subtitle = "A screen posts a typed result back with backWithResult(). The caller observes it via resultOf<T>() — one-shot, composable-safe.",
                codeSnippet = "// In caller:\nval result by navController.resultOf<DetailScreen.Result>()\n\n// In detail screen:\nnavController.backWithResult(\n    DetailScreen.Result(liked = true)\n)",
                colorIndex = 3,
                navTransition = NavTransition.Push,
                onClick = { navigate(AppScreens.TypedResult) },
            ),
        )

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "NavEase",
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                            )
                            Text(
                                text = "KMP navigation library · feature demos",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                )
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {

                // ── Typed-result banner ───────────────────────────────────────
                typedResult?.let { result ->
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
                                Text(
                                    text = "RESULT RECEIVED  ←  TypedResultDemoScreen",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.65f),
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "⭐  Rating: ${result.rating} / 5     Comment: \"${result.comment}\"",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = "val result by navController.resultOf<TypedResultDemoScreen.Result>()",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.65f),
                                )
                            }
                        }
                    }
                }

                // ── Feature cards ─────────────────────────────────────────────
                items(features) { f ->
                    val accent      = accentColorAt(f.colorIndex)
                    val onAccent    = onAccentColorAt(f.colorIndex)
                    val container   = containerColorAt(f.colorIndex)
                    val onContainer = onContainerColorAt(f.colorIndex)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = container),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {

                            // Header
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Text(f.emoji, style = MaterialTheme.typography.headlineMedium)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        f.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = onContainer,
                                    )
                                    Text(
                                        f.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = onContainer.copy(alpha = 0.72f),
                                    )
                                }
                            }

                            Spacer(Modifier.height(12.dp))
                            HorizontalDivider(color = onContainer.copy(alpha = 0.10f))
                            Spacer(Modifier.height(12.dp))

                            // Code snippet
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = accent.copy(alpha = 0.10f),
                            ) {
                                Text(
                                    text = f.codeSnippet,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = accent,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                )
                            }

                            Spacer(Modifier.height(14.dp))

                            // CTA
                            Button(
                                onClick = { f.onClick(navEaseController) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = accent,
                                    contentColor = onAccent,
                                ),
                            ) {
                                Text(
                                    "Try ${f.title} →",
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}
