package com.alim.navease.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import io.github.alimsrepo.navease.generated.mainResult
import io.github.alimsrepo.navease.generated.navigateToMain
import io.github.alimsrepo.navease.runtime.annotations.NavEaseScreen
import io.github.alimsrepo.navease.runtime.navigation.NavController
import io.github.alimsrepo.navease.runtime.domain.NavScreen
import kotlinx.coroutines.delay

@NavEaseScreen(route = "Splash", startDestination = true)
class SplashScreen : NavScreen() {

    @Composable
    override fun Content(
        navKey: NavKey,
        navController: NavController
    ) {
        // Collect result returned from MainScreen (null until Main navigates back with a value)
        val result by navController.mainResult()

        LaunchedEffect(Unit) {
            delay(1000L)
            navController.navigateToMain(userId = "user_a", age = 10)
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Splash Screen",
                    style = MaterialTheme.typography.headlineMedium
                )
                result?.let {
                    Text(
                        text = "Result from Main: ${it}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
