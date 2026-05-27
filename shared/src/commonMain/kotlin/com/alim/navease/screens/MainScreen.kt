package com.alim.navease.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import io.github.alimsrepo.navease.runtime.annotations.NavEaseArgs
import io.github.alimsrepo.navease.runtime.annotations.NavEaseResult
import io.github.alimsrepo.navease.runtime.annotations.NavEaseScreen
import io.github.alimsrepo.navease.generated.backWithMainResult
import io.github.alimsrepo.navease.generated.detailResult
import io.github.alimsrepo.navease.generated.mainArgs
import io.github.alimsrepo.navease.generated.navigateToDetail
import io.github.alimsrepo.navease.runtime.navigation.NavController
import io.github.alimsrepo.navease.runtime.domain.NavScreen

@NavEaseScreen(route = "Main")
class MainScreen : NavScreen() {

    @NavEaseArgs
    data class Args(val userId: String, val age: Int)

    @NavEaseResult
    data class Result(val value: Int)

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content(
        navKey: NavKey,
        navController: NavController
    ) {
        val args = navKey.mainArgs()
        val detailResult by navController.detailResult()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "NavEase Demo",
                            fontWeight = FontWeight.SemiBold
                        )
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── User info card ─────────────────────────────────────────────
                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Avatar circle
                            Box(
                                modifier = Modifier
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
                            Column {
                                Text(
                                    text = "Hello, ${args.userId}!",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Age: ${args.age}  ·  ID: ${args.userId}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }

                // ── Section label ──────────────────────────────────────────────
                item {
                    Text(
                        text = "FEATURES",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                // ── Args demo card ─────────────────────────────────────────────
                item {
                    FeatureCard(
                        badge = "01",
                        badgeColor = MaterialTheme.colorScheme.tertiary,
                        badgeOnColor = MaterialTheme.colorScheme.onTertiary,
                        title = "Typed Arguments",
                        subtitle = "Navigate to a screen passing typed args — accessed via navKey.xxxArgs()",
                        action = "Explore Detail →",
                        onAction = {
                            navController.navigateToDetail(
                                featureName = "Typed Arguments",
                                description = "This screen received args from MainScreen.\n\n" +
                                    "The KSP processor generated AppScreens.Detail(featureName, description) " +
                                    "as a @Serializable data class, and a navigateToDetail() extension " +
                                    "on NavController so you never reference generated types directly."
                            )
                        }
                    )
                }

                // ── Back-with-result card ──────────────────────────────────────
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
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
                                    Text(
                                        "02",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondary
                                    )
                                }
                                Column {
                                    Text(
                                        "Back with Result",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        "Returns a typed value back to SplashScreen",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(Modifier.height(16.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(Modifier.height(16.dp))

                            Text(
                                text = "Choose a return value:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(10.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(1, 2, 3).forEach { v ->
                                    FilledTonalButton(
                                        onClick = { navController.backWithMainResult(value = v) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("$v")
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { navController.back() },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Back (no result)")
                            }
                        }
                    }
                }

                // ── Detail result banner ───────────────────────────────────────
                detailResult?.let { res ->
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = if (res.liked) MaterialTheme.colorScheme.secondaryContainer
                                    else MaterialTheme.colorScheme.errorContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = if (res.liked) "👍" else "👎",
                                    style = MaterialTheme.typography.headlineSmall
                                )
                                Column {
                                    Text(
                                        text = "Result from Detail",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (res.liked) MaterialTheme.colorScheme.onSecondaryContainer
                                                else MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Text(
                                        text = "liked = ${res.liked}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (res.liked) MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f)
                                                else MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.75f)
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Stack info card ────────────────────────────────────────────
                item {
                    Text(
                        text = "BACK STACK",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
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
                                                text = "top",
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

@Composable
private fun FeatureCard(
    badge: String,
    badgeColor: androidx.compose.ui.graphics.Color,
    badgeOnColor: androidx.compose.ui.graphics.Color,
    title: String,
    subtitle: String,
    action: String,
    onAction: () -> Unit
) {
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
                        .background(badgeColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        badge,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = badgeOnColor
                    )
                }
                Column {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onAction,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = badgeColor,
                    contentColor = badgeOnColor
                )
            ) {
                Text(action, fontWeight = FontWeight.Medium)
            }
        }
    }
}

