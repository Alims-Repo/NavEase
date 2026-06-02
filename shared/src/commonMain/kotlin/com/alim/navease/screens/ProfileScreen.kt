package com.alim.navease.screens

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.dp
import io.github.alimsrepo.navease.internal.navigation.ui.LocalNavAnimatedContentScope
import io.github.alimsrepo.navease.runtime.annotations.AutoRegister
import io.github.alimsrepo.navease.runtime.composition.LocalNavEaseSharedTransitionScope
import io.github.alimsrepo.navease.runtime.navigation.NavEaseController
import io.github.alimsrepo.navease.runtime.navigation.backWithResult
import io.github.alimsrepo.navease.runtime.screen.ActivityScreen
import kotlinx.serialization.Serializable

@AutoRegister
class ProfileScreen : ActivityScreen<AppScreens.Profile>() {

    @Serializable
    data class Result(
        val updated: Boolean,
        val changesCount: Int = 0
    )

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
    @Composable
    override fun Content(navKey: AppScreens.Profile, navEaseController: NavEaseController) {
        val sharedScope = LocalNavEaseSharedTransitionScope.current
        val animatedScope = LocalNavAnimatedContentScope.current

        var hasChanges by remember { mutableStateOf(false) }
        var changesCount by remember { mutableStateOf(0) }

        val sharedModifier = if (sharedScope != null) {
            with(sharedScope) {
                Modifier.sharedBounds(
                    sharedContentState = rememberSharedContentState(key = "nav_card_Profile"),
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
                            if (navKey.isEditable) "Edit Profile" else "Profile",
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            navEaseController.backWithResult(
                                Result(updated = hasChanges, changesCount = changesCount)
                            )
                        }) {
                            Text("←", style = MaterialTheme.typography.headlineMedium)
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
                // Profile Header
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
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "👤",
                                style = MaterialTheme.typography.displayLarge
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        Text(
                            "John Doe",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            "User ID: ${navKey.userId}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )

                        if (navKey.isEditable) {
                            Spacer(Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    "✏️ Edit Mode",
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }

                // Profile Information
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "Information",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        ProfileInfoRow(label = "Email", value = "john.doe@example.com")
                        HorizontalDivider()
                        ProfileInfoRow(label = "Phone", value = "+1 (555) 123-4567")
                        HorizontalDivider()
                        ProfileInfoRow(label = "Location", value = "San Francisco, CA")
                        HorizontalDivider()
                        ProfileInfoRow(label = "Member Since", value = "January 2024")
                    }
                }

                // Statistics
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            "Statistics",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem(label = "Posts", value = "127")
                            StatItem(label = "Followers", value = "1.2K")
                            StatItem(label = "Following", value = "842")
                        }
                    }
                }

                // Actions
                if (navKey.isEditable) {
                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = {
                            hasChanges = true
                            changesCount++
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "💾 Make Change ($changesCount changes)",
                            modifier = Modifier.padding(vertical = 8.dp),
                            style = MaterialTheme.typography.titleSmall
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            navEaseController.backWithResult(
                                Result(updated = hasChanges, changesCount = changesCount)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            if (hasChanges) "Save & Go Back" else "Cancel",
                            modifier = Modifier.padding(vertical = 8.dp),
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
        )
    }
}

