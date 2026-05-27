package com.alim.navease.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import io.github.alimsrepo.navease.NavEaseController
import io.github.alimsrepo.navease.NavEaseScreen
import io.github.alimsrepo.navease.Transition
import com.alim.navease.AppScreens
import kotlinx.coroutines.delay

/**
 * Splash screen shown on cold start.
 *
 * Automatically navigates to [AppScreens.Home] after a short delay using a fade-out.
 * Registered with NavEase via [NavEaseScreen] — no manual factory wiring required.
 */
@NavEaseScreen(route = AppScreens.Splash::class, transition = Transition.FADE)
@Composable
fun SplashScreen(navKey: AppScreens.Splash, nav: NavEaseController) {
    var alpha by remember { mutableFloatStateOf(0f) }
    val animatedAlpha by animateFloatAsState(
        targetValue = alpha,
        animationSpec = tween(durationMillis = 800),
        label = "SplashAlpha",
    )

    LaunchedEffect(Unit) {
        alpha = 1f
        delay(1_500)
        // Replace splash with home so pressing Back exits the app instead of returning to splash
        nav.popUpTo(AppScreens.Splash, inclusive = true)
        nav.navigate(AppScreens.Home)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "NavEase",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.alpha(animatedAlpha),
        )
    }
}

