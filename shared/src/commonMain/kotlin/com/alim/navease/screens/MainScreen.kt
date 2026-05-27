package com.alim.navease.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.alimsrepo.navease.generated.AppScreens
import io.github.alimsrepo.navease.runtime.NavEaseScreen
import io.github.alimsrepo.navease.runtime.data.NavController
import io.github.alimsrepo.navease.runtime.domain.NavScreen

@NavEaseScreen(route = "Main")
class MainScreen  : NavScreen<AppScreens.Main>() {

    @Composable
    override fun Content(
        navKey: AppScreens.Main,
        navController: NavController
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Main Screen",
                style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
            )
        }
    }

}