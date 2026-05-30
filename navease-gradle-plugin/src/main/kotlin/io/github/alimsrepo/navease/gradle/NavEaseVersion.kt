package io.github.alimsrepo.navease.gradle

/**
 * Holds the version of the NavEase artifacts that this plugin version ships alongside.
 * Updated automatically when the library version changes.
 */
internal object NavEaseVersion {
    const val VERSION = "0.0.3"
    const val GROUP   = "io.github.alims-repo"

    const val RUNTIME_ARTIFACT = "navease-runtime"
    const val KSP_ARTIFACT     = "navease-ksp"

    val runtimeCoordinate get() = "$GROUP:$RUNTIME_ARTIFACT:$VERSION"
    val kspCoordinate     get() = "$GROUP:$KSP_ARTIFACT:$VERSION"
}

