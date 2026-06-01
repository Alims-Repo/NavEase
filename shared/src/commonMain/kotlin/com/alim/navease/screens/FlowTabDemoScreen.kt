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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.alimsrepo.navease.runtime.annotations.AutoRegister
import io.github.alimsrepo.navease.runtime.navigation.NavEaseController
import io.github.alimsrepo.navease.runtime.navigation.backWithResult
import io.github.alimsrepo.navease.runtime.screen.ActivityScreen

/**
 * Demonstrates **typed navigation results** via [backWithResult].
 *
 * The user selects a star rating and optional comment, then taps "Submit & Go Back".
 * NavEase posts [Result] to the back-stack entry for [HomeScreen] which observes it
 * via `resultOf<TypedResultDemoScreen.Result>()`.
 */
@AutoRegister
class TypedResultDemoScreen : ActivityScreen<AppScreens.TypedResult>() {

    /** Typed result posted back to the calling screen. */
    data class Result(val rating: Int, val comment: String)

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content(navKey: AppScreens.TypedResult, navEaseController: NavEaseController) {

        var selectedRating by remember { mutableIntStateOf(0) }
        var selectedComment by remember { mutableStateOf("") }

        val comments = listOf("Very useful!", "Easy to integrate", "Great API", "Love the transitions", "Skip")

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("Typed Results", fontWeight = FontWeight.SemiBold)
                            Text(
                                "backWithResult() · resultOf<T>()",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                    navigationIcon = {
                        NavBackButton(onClick = { navEaseController.back() })
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                )
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {

                // ── Rating picker ─────────────────────────────────────────────
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                        ),
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                "RATE NAVEASE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.65f),
                            )
                            Spacer(Modifier.height(16.dp))

                            // Star display
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                repeat(5) { i ->
                                    Text(
                                        text = if (i < selectedRating) "⭐" else "☆",
                                        fontSize = 32.sp,
                                    )
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            // Clickable star buttons
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                repeat(5) { i ->
                                    val star = i + 1
                                    OutlinedButton(
                                        onClick = { selectedRating = star },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = if (selectedRating == star)
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                            else
                                                MaterialTheme.colorScheme.surface.copy(alpha = 0.60f),
                                        ),
                                    ) {
                                        Text(
                                            "$star",
                                            fontWeight = FontWeight.Bold,
                                            color = if (selectedRating == star)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Comment picker ────────────────────────────────────────────
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                "QUICK COMMENT",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.outline,
                            )
                            Spacer(Modifier.height(12.dp))
                            comments.forEach { c ->
                                val selected = selectedComment == c
                                OutlinedButton(
                                    onClick = { selectedComment = if (selected) "" else c },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (selected)
                                            MaterialTheme.colorScheme.secondaryContainer
                                        else
                                            MaterialTheme.colorScheme.surface,
                                    ),
                                ) {
                                    Text(
                                        if (selected) "✓  $c" else c,
                                        color = if (selected)
                                            MaterialTheme.colorScheme.onSecondaryContainer
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                            }
                        }
                    }
                }

                // ── Submit ────────────────────────────────────────────────────
                item {
                    val canSubmit = selectedRating > 0
                    Button(
                        onClick = {
                            navEaseController.backWithResult(
                                Result(
                                    rating = selectedRating,
                                    comment = selectedComment.ifBlank { "No comment" },
                                )
                            )
                        },
                        enabled = canSubmit,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                    ) {
                        Text(
                            if (canSubmit) "Submit & Go Back →" else "Select a rating first",
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = 4.dp),
                        )
                    }
                }

                // ── How it works ──────────────────────────────────────────────
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                "HOW IT WORKS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.outline,
                            )
                            Spacer(Modifier.height(12.dp))

                            ResultStepLabel("In this screen — post a typed result")
                            Spacer(Modifier.height(8.dp))
                            ResultCodeBlock(
                                """navController.backWithResult(
    TypedResultDemoScreen.Result(
        rating  = selectedRating,
        comment = selectedComment,
    )
)"""
                            )

                            Spacer(Modifier.height(16.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(Modifier.height(16.dp))

                            ResultStepLabel("In HomeScreen — observe it one-shot")
                            Spacer(Modifier.height(8.dp))
                            ResultCodeBlock(
                                "val result by navController\n" +
                                "    .resultOf<TypedResultDemoScreen.Result>()\n\n" +
                                "result?.let {\n" +
                                "    Text(\"Rating: \${it.rating}\")   // ✅ typed Int\n" +
                                "    Text(\"Comment: \${it.comment}\") // ✅ typed String\n" +
                                "}"
                            )
                        }
                    }
                }

                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}

@Composable
private fun ResultStepLabel(label: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
        )
    }
}

@Composable
private fun ResultCodeBlock(code: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Text(
            text = code,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
            modifier = Modifier.padding(14.dp),
        )
    }
}

