package io.github.alimsrepo.navease.runtime

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.alimsrepo.navease.runtime.data.NavController
import io.github.alimsrepo.navease.runtime.domain.AppScreens
import io.github.alimsrepo.navease.runtime.domain.NavScreen

class SplashScreen : NavScreen<AppScreens.Splash>() {
    @Composable
    override fun InitView(
        navKey: AppScreens.Splash,
        navController: NavController
    ) {

    }
}