package com.alim.navease.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import io.github.alimsrepo.navease.runtime.NavEaseScreen
import io.github.alimsrepo.navease.runtime.data.NavController
import io.github.alimsrepo.navease.runtime.domain.NavScreen
import io.github.alimsrepo.navease.runtime.domain.AppScreens
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable

@NavEaseScreen(route = "Splash")
class SplashScreen : NavScreen<AppScreens.Splash>() {

    @Composable
    override fun Content(
        navKey: AppScreens.Splash,
        navController: NavController
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            var startAnimation by remember { mutableStateOf(false) }
            val alphaAnim by animateFloatAsState(
                targetValue = if (startAnimation) 1f else 0f,
                animationSpec = tween(durationMillis = 1000)
            )

            LaunchedEffect(Unit) {
                startAnimation = true
                delay(2000) // Keep the splash screen visible for a while
                navController.navigate(AppScreens.Splash) // Navigate to the next screen
            }

            Text(
                text = "Welcome to NavEase!",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.alpha(alphaAnim)
            )
        }
    }
}
