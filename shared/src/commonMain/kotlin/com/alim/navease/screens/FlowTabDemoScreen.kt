package com.alim.navease.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import io.github.alimsrepo.navease.runtime.annotations.NavEaseScreen
import io.github.alimsrepo.navease.runtime.domain.NavScreen
import io.github.alimsrepo.navease.runtime.navigation.NavController

private enum class IndicatorStyle(val label: String, val description: String) {
    Ripple("Ripple", "Full-width background highlight behind the selected item"),
    Dot("Dot", "Small circular dot below the selected icon — minimal, Instagram-style"),
    Line("Line", "Horizontal underline — Material Design 3 style"),
}

private data class NavTabItem(val id: String, val icon: String, val label: String)

private val demoTabs = listOf(
    NavTabItem("home",     "🏠", "Home"),
    NavTabItem("explore",  "🔍", "Explore"),
    NavTabItem("library",  "📚", "Library"),
    NavTabItem("profile",  "👤", "Profile"),
)

@NavEaseScreen(route = "FlowTabDemo")
class FlowTabDemoScreen : NavScreen() {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content(navKey: NavKey, navController: NavController) {
        var selectedTab by remember { mutableStateOf("home") }
        var selectedStyle by remember { mutableStateOf(IndicatorStyle.Ripple) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("FlowTab CMP", fontWeight = FontWeight.SemiBold)
                            Text(
                                "Live interactive demo",
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
            bottomBar = {
                // ── Live FlowTab simulation ────────────────────────────────────
                FlowTabSimulation(
                    tabs = demoTabs,
                    selectedId = selectedTab,
                    indicatorStyle = selectedStyle,
                    onSelect = { selectedTab = it }
                )
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // "Screen content" area
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
                            Text(
                                text = demoTabs.first { it.id == selectedTab }.icon,
                                style = MaterialTheme.typography.displayMedium
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = demoTabs.first { it.id == selectedTab }.label,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "This content area changes as you tap the nav bar below",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.70f)
                            )
                        }
                    }
                }

                // Indicator style switcher
                item {
                    Text(
                        "INDICATOR STYLE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IndicatorStyle.entries.forEach { style ->
                            val isSelected = selectedStyle == style
                            val bgColor by animateColorAsState(
                                targetValue = if (isSelected) MaterialTheme.colorScheme.primary
                                              else MaterialTheme.colorScheme.surface,
                                label = "indicator_chip_bg"
                            )
                            val textColor by animateColorAsState(
                                targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                              else MaterialTheme.colorScheme.onSurface,
                                label = "indicator_chip_text"
                            )
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedStyle = style },
                                shape = RoundedCornerShape(12.dp),
                                color = bgColor
                            ) {
                                Text(
                                    text = style.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textColor,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp)
                                )
                            }
                        }
                    }
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
                            Text(
                                text = selectedStyle.label,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = selectedStyle.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(12.dp))
                            HorizontalDivider()
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = when (selectedStyle) {
                                    IndicatorStyle.Ripple ->
                                        "NavConfig(\n    navIndicator = NavIndicator.Ripple(\n        color = MaterialTheme.colorScheme.primaryContainer,\n        indicatorPadding = 4.dp\n    )\n)"
                                    IndicatorStyle.Dot ->
                                        "NavConfig(\n    navIndicator = NavIndicator.Dot(\n        size = 6.dp,\n        color = MaterialTheme.colorScheme.primary,\n        indicatorPadding = 4.dp\n    )\n)"
                                    IndicatorStyle.Line ->
                                        "NavConfig(\n    navIndicator = NavIndicator.Line(\n        height = 2.dp,\n        width = 40.dp,\n        color = MaterialTheme.colorScheme.primary,\n        indicatorPadding = 4.dp\n    )\n)"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                            )
                        }
                    }
                }

                // Core usage code
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
                                "BASIC USAGE",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = "BottomNavigation(\n" +
                                       "    items = navItems,\n" +
                                       "    selectedId = selectedScreen,\n" +
                                       "    onItemSelected = { item ->\n" +
                                       "        selectedScreen = item.id\n" +
                                       "        // YOUR navigation logic here\n" +
                                       "    },\n" +
                                       "    config = NavConfig(\n" +
                                       "        cornerRadius = 60.dp,\n" +
                                       "        enableBlur = true,   // Haze\n" +
                                       "        navIndicator = NavIndicator.${selectedStyle.name}(),\n" +
                                       "    )\n)",
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

// ── Simulated FlowTab component (pure Compose recreation) ─────────────────────

@Composable
private fun FlowTabSimulation(
    tabs: List<NavTabItem>,
    selectedId: String,
    indicatorStyle: IndicatorStyle,
    onSelect: (String) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        shape = RoundedCornerShape(60.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            tabs.forEach { tab ->
                val isSelected = tab.id == selectedId
                FlowTabItem(
                    tab = tab,
                    isSelected = isSelected,
                    indicatorStyle = indicatorStyle,
                    onClick = { onSelect(tab.id) }
                )
            }
        }
    }
}

@Composable
private fun FlowTabItem(
    tab: NavTabItem,
    isSelected: Boolean,
    indicatorStyle: IndicatorStyle,
    onClick: () -> Unit,
) {
    val selectedColor = MaterialTheme.colorScheme.primary
    val unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
    val rippleColor = MaterialTheme.colorScheme.primaryContainer

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .clickable(onClick = onClick)
            .then(
                if (isSelected && indicatorStyle == IndicatorStyle.Ripple)
                    Modifier.background(rippleColor)
                else Modifier
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = tab.icon,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = tab.label,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) selectedColor else unselectedColor,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
            // Indicator below label
            when {
                isSelected && indicatorStyle == IndicatorStyle.Dot -> {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(selectedColor)
                    )
                }
                isSelected && indicatorStyle == IndicatorStyle.Line -> {
                    Box(
                        modifier = Modifier
                            .width(28.dp)
                            .height(2.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(selectedColor)
                    )
                }
                else -> {
                    // Ripple has background, or not selected — space placeholder
                    Spacer(Modifier.height(5.dp))
                }
            }
        }
    }
}

