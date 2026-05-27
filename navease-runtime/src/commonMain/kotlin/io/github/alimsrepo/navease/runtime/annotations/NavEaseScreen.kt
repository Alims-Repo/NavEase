package io.github.alimsrepo.navease.runtime.annotations

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class NavEaseScreen(
    val route: String,
    val startDestination: Boolean = false
)