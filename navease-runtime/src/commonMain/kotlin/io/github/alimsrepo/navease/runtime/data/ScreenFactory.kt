package io.github.alimsrepo.navease.runtime.data

import io.github.alimsrepo.navease.runtime.domain.AppScreens
import io.github.alimsrepo.navease.runtime.domain.NavScreen

object ScreenFactory {

    @Suppress("UNCHECKED_CAST")
    fun createScreen(appScreen: AppScreens): NavScreen<AppScreens> {
        return when(appScreen) {
            AppScreens.Splash -> SplashScreen()
        }  as NavScreen<AppScreens>
    }
}