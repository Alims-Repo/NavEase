plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.navease) apply false
    alias(libs.plugins.androidLint) apply false

    alias(libs.plugins.dokka)
}

dependencies {
    dokka(project(":navease-ksp"))
    dokka(project(":navease-runtime"))
    dokka(project(":navease-gradle-plugin"))
}

dokka {
    moduleName.set("NavEase CMP")

    pluginsConfiguration.html {
        footerMessage.set("© 2026 Alim Sourav · Apache-2.0")
    }

    dokkaPublications.html {
        // Emit straight into docs/api/ so GitHub Pages (which serves /docs)
        // exposes the API reference at /SecureVault-KMP/api/.
        outputDirectory.set(rootDir.resolve("docs/api"))
    }
}
