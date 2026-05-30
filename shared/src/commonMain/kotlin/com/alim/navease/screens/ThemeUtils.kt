package com.alim.navease.screens

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun containerColorAt(index: Int): Color = when (index % 6) {
    0 -> MaterialTheme.colorScheme.primaryContainer
    1 -> MaterialTheme.colorScheme.secondaryContainer
    2 -> MaterialTheme.colorScheme.tertiaryContainer
    3 -> MaterialTheme.colorScheme.surfaceVariant
    4 -> MaterialTheme.colorScheme.errorContainer
    else -> MaterialTheme.colorScheme.inversePrimary
}

@Composable
fun onContainerColorAt(index: Int): Color = when (index % 6) {
    0 -> MaterialTheme.colorScheme.onPrimaryContainer
    1 -> MaterialTheme.colorScheme.onSecondaryContainer
    2 -> MaterialTheme.colorScheme.onTertiaryContainer
    3 -> MaterialTheme.colorScheme.onSurfaceVariant
    4 -> MaterialTheme.colorScheme.onErrorContainer
    else -> MaterialTheme.colorScheme.onPrimaryContainer
}

@Composable
fun accentColorAt(index: Int): Color = when (index % 6) {
    0 -> MaterialTheme.colorScheme.primary
    1 -> MaterialTheme.colorScheme.secondary
    2 -> MaterialTheme.colorScheme.tertiary
    3 -> MaterialTheme.colorScheme.primary
    4 -> MaterialTheme.colorScheme.error
    else -> MaterialTheme.colorScheme.secondary
}

@Composable
fun onAccentColorAt(index: Int): Color = when (index % 6) {
    0 -> MaterialTheme.colorScheme.onPrimary
    1 -> MaterialTheme.colorScheme.onSecondary
    2 -> MaterialTheme.colorScheme.onTertiary
    3 -> MaterialTheme.colorScheme.onPrimary
    4 -> MaterialTheme.colorScheme.onError
    else -> MaterialTheme.colorScheme.onSecondary
}

