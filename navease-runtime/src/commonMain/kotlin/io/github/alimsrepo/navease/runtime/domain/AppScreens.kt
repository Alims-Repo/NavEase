package io.github.alimsrepo.navease.runtime.domain

import androidx.compose.runtime.Stable
import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@Stable
@Serializable
sealed class AppScreens : NavKey {
    @Serializable data object Splash : AppScreens()


    companion object {

        val items get() = listOf(Splash)

        val savedStateConfig = SavedStateConfiguration {
            serializersModule = SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(Splash::class)
                }
            }
        }
    }
}