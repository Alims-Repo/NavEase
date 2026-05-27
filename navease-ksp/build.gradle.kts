plugins {
    alias(libs.plugins.kotlinJvm)
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    implementation(libs.ksp.api)
}