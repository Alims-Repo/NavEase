package com.alim.navease.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.alimsrepo.navease.runtime.navigation.NavController
import io.github.alimsrepo.navease.runtime.presentation.ActivityScreen

private sealed interface VaultOp {
    data object Idle : VaultOp
    data class Stored(val key: String, val value: String) : VaultOp
    data class Retrieved(val key: String, val value: String?) : VaultOp
    data object Cleared : VaultOp
    data class Error(val message: String) : VaultOp
}

class SecureVaultDemoScreen : ActivityScreen<AppScreens.SecureVaultDemo>() {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content(navKey: AppScreens.SecureVaultDemo, navController: NavController) {
        // Simulated in-memory vault
        val vault = remember { mutableMapOf<String, String>() }

        var keyInput by remember { mutableStateOf("session_token") }
        var valueInput by remember { mutableStateOf("") }
        var lastOp by remember { mutableStateOf<VaultOp>(VaultOp.Idle) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("SecureVault KMP", fontWeight = FontWeight.SemiBold)
                            Text(
                                "Interactive API simulation",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        NavBackButton(onClick = { navController.back() })
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
                // ── Platform note ─────────────────────────────────────────────
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.secondary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🔐", style = MaterialTheme.typography.titleMedium)
                            }
                            Column {
                                Text(
                                    "Demo runs in-memory",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    "On Android the real library uses EncryptedSharedPreferences over Android Keystore. " +
                                    "On iOS it uses Keychain Services. This simulation shows the same API surface.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.80f)
                                )
                            }
                        }
                    }
                }

                // ── Interactive vault ─────────────────────────────────────────
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
                                "VAULT OPERATIONS",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Spacer(Modifier.height(14.dp))

                            // Key field
                            Text(
                                "Key",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                BasicTextField(
                                    value = keyInput,
                                    onValueChange = { keyInput = it },
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurface
                                    ),
                                    modifier = Modifier.fillMaxWidth().padding(12.dp)
                                )
                            }

                            Spacer(Modifier.height(10.dp))

                            // Value field
                            Text(
                                "Value (for put)",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                BasicTextField(
                                    value = valueInput,
                                    onValueChange = { valueInput = it },
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurface
                                    ),
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    decorationBox = { inner ->
                                        if (valueInput.isEmpty()) {
                                            Text(
                                                "eyJhbGciOiJIUzI1NiJ9…",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontFamily = FontFamily.Monospace,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                        inner()
                                    }
                                )
                            }

                            Spacer(Modifier.height(14.dp))

                            // Operation buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        if (valueInput.isBlank()) {
                                            lastOp = VaultOp.Error("value cannot be empty")
                                        } else {
                                            vault[keyInput] = valueInput
                                            lastOp = VaultOp.Stored(keyInput, valueInput)
                                            valueInput = ""
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary
                                    )
                                ) { Text("put()") }

                                Button(
                                    onClick = {
                                        val v = vault[keyInput]
                                        lastOp = VaultOp.Retrieved(keyInput, v)
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                ) { Text("get()") }

                                OutlinedButton(
                                    onClick = {
                                        vault.remove(keyInput)
                                        lastOp = VaultOp.Cleared
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) { Text("remove()") }
                            }

                            // Result display
                            Spacer(Modifier.height(12.dp))
                            HorizontalDivider()
                            Spacer(Modifier.height(10.dp))

                            AnimatedContent(
                                targetState = lastOp,
                                transitionSpec = { fadeIn() togetherWith fadeOut() },
                                label = "vault_result"
                            ) { op ->
                                when (op) {
                                    VaultOp.Idle -> Text(
                                        "Tap an operation above to see the result here.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    is VaultOp.Stored -> Text(
                                        "✓  Stored  \"${op.key}\" → \"${op.value}\"",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    is VaultOp.Retrieved -> {
                                        if (op.value != null) {
                                            Text(
                                                "✓  get(\"${op.key}\") = \"${op.value}\"",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontFamily = FontFamily.Monospace,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        } else {
                                            Text(
                                                "get(\"${op.key}\") = null  (key not stored)",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontFamily = FontFamily.Monospace,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    }
                                    VaultOp.Cleared -> Text(
                                        "✓  remove() — key erased from vault",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.tertiary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    is VaultOp.Error -> Text(
                                        "✗  Error: ${op.message}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                // Stored secrets snapshot
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
                                "VAULT CONTENTS (${vault.size} entries)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Spacer(Modifier.height(10.dp))
                            if (vault.isEmpty()) {
                                Text(
                                    "No secrets stored yet.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            } else {
                                vault.entries.forEach { (k, v) ->
                                    Text(
                                        text = "\"$k\" → \"${"*".repeat(minOf(v.length, 8))}\"",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.80f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Compose integration
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
                                "COMPOSE INTEGRATION",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = "@Composable\nfun LoginScreen() {\n    // Two-way binding — reads via observe(), writes via put()\n    var token by rememberSecureValue(\n        key     = \"auth.token\",\n        default = \"\"\n    )\n    OutlinedTextField(\n        value = token,\n        onValueChange = { token = it },  // writes to Keystore/Keychain\n        label = { Text(\"Bearer Token\") }\n    )\n}",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.80f)
                            )
                        }
                    }
                }

                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}

