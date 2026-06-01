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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.alimsrepo.navease.runtime.annotations.AutoRegister
import io.github.alimsrepo.navease.runtime.navigation.NavEaseController
import io.github.alimsrepo.navease.runtime.presentation.ActivityScreen

@AutoRegister
class CrashGuardDemoScreen : ActivityScreen<AppScreens.CrashGuardDemo>() {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content(navKey: AppScreens.CrashGuardDemo, navEaseController: NavEaseController) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("CrashGuard", fontWeight = FontWeight.SemiBold)
                            Text(
                                "Crash screen previews + config",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        NavBackButton(onClick = { navEaseController.back() })
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ── User-friendly crash screen mock ────────────────────────────
                item {
                    Text(
                        "USER-FACING CRASH SCREEN",
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
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🛡️", style = MaterialTheme.typography.displayMedium)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Oops! Something went wrong",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "We're really sorry about this. We've been notified of the problem and will fix it as soon as possible.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.80f)
                            )
                            Spacer(Modifier.height(20.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primary
                                ) {
                                    Text(
                                        "Restart App",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                    )
                                }
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        "Send Report",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Rendered by CrashGuard — userScreen = MyFriendlyCrashActivity::class",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.50f)
                            )
                        }
                    }
                }

                // ── Developer crash screen mock ────────────────────────────────
                item {
                    Text(
                        "DEVELOPER DEBUG SCREEN",
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
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.error),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("💥", style = MaterialTheme.typography.titleLarge)
                                }
                                Column {
                                    Text(
                                        "NullPointerException",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Text(
                                        "com.example.app",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.70f)
                                    )
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.15f)
                            )
                            Spacer(Modifier.height(10.dp))

                            // Stack trace mock
                            listOf(
                                "at com.example.app.MainActivity\$onCreate\$1.invokeSuspend(MainActivity.kt:42)",
                                "at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:33)",
                                "at kotlinx.coroutines.DispatchedTask.run(DispatchedTask.kt:106)",
                                "at kotlinx.coroutines.scheduling.CoroutineScheduler.runSafely(CoroutineScheduler.kt:584)",
                            ).forEach { line ->
                                Text(
                                    text = line,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.75f)
                                )
                                Spacer(Modifier.height(2.dp))
                            }

                            Spacer(Modifier.height(10.dp))
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.15f)
                            )
                            Spacer(Modifier.height(8.dp))

                            // Device info
                            listOf(
                                "Device" to "Google Pixel 9 Pro",
                                "Android" to "15 (API 35)",
                                "RAM" to "6.2 GB / 12 GB",
                                "Battery" to "78% · Charging",
                                "Network" to "WiFi",
                            ).forEach { (label, value) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.65f)
                                    )
                                    Text(
                                        value,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Configuration ──────────────────────────────────────────────
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
                                "CONFIGURATION",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = "CrashGuard.install(this) {\n" +
                                       "    // Dual crash screens\n" +
                                       "    crashScreen {\n" +
                                       "        userScreen      = FriendlyCrashActivity::class\n" +
                                       "        developerScreen = DebugCrashActivity::class\n" +
                                       "    }\n" +
                                       "    // Persistent log storage\n" +
                                       "    logging {\n" +
                                       "        maxCrashLogs    = 50\n" +
                                       "        enablePersistence = true\n" +
                                       "    }\n" +
                                       "    // Analytics bridge\n" +
                                       "    analytics {\n" +
                                       "        onCrash { throwable, data ->\n" +
                                       "            Firebase.crashlytics.recordException(throwable)\n" +
                                       "        }\n" +
                                       "    }\n" +
                                       "    // Auto-restart\n" +
                                       "    autoRestart { enabled = true; delayMillis = 3_000 }\n" +
                                       "    // Exception filter\n" +
                                       "    excludeExceptions(CancellationException::class)\n" +
                                       "}",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.80f)
                            )
                        }
                    }
                }

                // Features checklist
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
                                "WHAT'S CAPTURED",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Spacer(Modifier.height(10.dp))
                            listOf(
                                "Exception type, message, full stack trace",
                                "Device: manufacturer, model, Android version",
                                "Memory: available / total heap",
                                "Battery: level + charging status",
                                "Network: WiFi / Mobile / Offline",
                                "Activity back stack at time of crash",
                                "Screen orientation and resolution",
                                "Available disk space",
                                "Custom data (add your own key-value pairs)",
                            ).forEach { item ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(vertical = 2.dp)
                                ) {
                                    Text("✓", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodySmall)
                                    Text(item, style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
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

