import com.vanniktech.maven.publish.GradlePlugin
import com.vanniktech.maven.publish.JavadocJar

plugins {
    alias(libs.plugins.kotlinJvm)
    `java-gradle-plugin`

    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.dokka)
    alias(libs.plugins.binaryCompatibilityValidator)
}

kotlin {
    jvmToolchain(21)
}

// ── Generate NavEaseVersion.kt from gradle.properties ──────────────────────
// VERSION_NAME and GROUP live in navease-gradle-plugin/gradle.properties so this
// works both locally (composite build) and in CI (standalone publishing).
val navEaseVersion: String = providers.gradleProperty("VERSION_NAME")
    .orNull ?: error("VERSION_NAME not found in gradle.properties")
val navEaseGroup: String = providers.gradleProperty("GROUP")
    .orNull ?: error("GROUP not found in gradle.properties")

val generateNavEaseVersion by tasks.registering {
    val outputDir = layout.buildDirectory.dir("generated/navease-version/kotlin")

    inputs.property("version", navEaseVersion)
    inputs.property("group", navEaseGroup)
    outputs.dir(outputDir)

    doLast {
        val outFile = outputDir.get().file(
            "io/github/alimsrepo/navease/gradle/NavEaseVersion.kt"
        ).asFile
        outFile.parentFile.mkdirs()
        outFile.writeText(
            """
            package io.github.alimsrepo.navease.gradle

            /**
             * Auto-generated from gradle.properties — do not edit manually.
             */
            internal object NavEaseVersion {
                const val VERSION = "$navEaseVersion"
                const val GROUP   = "$navEaseGroup"

                const val RUNTIME_ARTIFACT = "navease-runtime"
                const val KSP_ARTIFACT     = "navease-ksp"

                val kspCoordinate     get() = "${'$'}{GROUP}:${'$'}{KSP_ARTIFACT}:${'$'}{VERSION}"
                val runtimeCoordinate get() = "${'$'}{GROUP}:${'$'}{RUNTIME_ARTIFACT}:${'$'}{VERSION}"
            }
            """.trimIndent()
        )
    }
}

kotlin.sourceSets.main {
    kotlin.srcDir(generateNavEaseVersion.map { layout.buildDirectory.dir("generated/navease-version/kotlin").get() })
}

dependencies {
    // Type-safe access to KotlinMultiplatformExtension and KotlinCompilationTask.
    // Keep this version in sync with [versions] kotlin in gradle/libs.versions.toml.
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.21")

    // Plugin functional tests
    testImplementation(gradleTestKit())
    testImplementation(libs.junit)
    testImplementation(libs.kotlin.testJunit)
}

// ── Plugin descriptor ──────────────────────────────────────────────────────
gradlePlugin {
    plugins {
        create("navease") {
            id = "io.github.alims-repo.navease"
            displayName = "NavEase Gradle Plugin"
            description = "Wires KSP, navease-runtime, and navease-ksp automatically for Kotlin Multiplatform projects — zero boilerplate setup."
            implementationClass = "io.github.alimsrepo.navease.gradle.NavEasePlugin"
            tags.set(listOf("kotlin", "kmp", "multiplatform", "navigation", "ksp", "compose"))
        }
    }
}

// ── Publishing ─────────────────────────────────────────────────────────────
mavenPublishing {
    configure(
        GradlePlugin(
            javadocJar = JavadocJar.Dokka("dokkaGeneratePublicationHtml"),
            sourcesJar = true,
        )
    )

    pom {
        name.set("NavEase Gradle Plugin")
        description.set(
            "Gradle plugin that automatically wires KSP and NavEase for " +
            "Kotlin Multiplatform projects. Eliminates all boilerplate setup."
        )
        inceptionYear.set("2026")
        url.set("https://github.com/Alims-Repo/NavEase")

        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("repo")
            }
        }

        developers {
            developer {
                id.set("alims-repo")
                name.set("Alim Sourav")
                email.set("sourav.0.alim@gmail.com")
                url.set("https://github.com/Alims-Repo")
            }
        }

        scm {
            url.set("https://github.com/Alims-Repo/NavEase")
            connection.set("scm:git:git://github.com/Alims-Repo/NavEase.git")
            developerConnection.set("scm:git:ssh://git@github.com/Alims-Repo/NavEase.git")
        }

        issueManagement {
            system.set("GitHub")
            url.set("https://github.com/Alims-Repo/NavEase/issues")
        }
    }

    publishToMavenCentral()
    signAllPublications()
}

