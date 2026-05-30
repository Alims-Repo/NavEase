import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.androidLint)

    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)

    alias(libs.plugins.kotlinSerialization)

    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.dokka)
    alias(libs.plugins.binaryCompatibilityValidator)
}

kotlin {
    android {
        namespace = "io.github.alimsrepo.navease.runtime"
        compileSdk {
            version = release(37)
        }
        minSdk = 24
//        consumerProguardFiles("consumer-rules.pro")
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
        iosX64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "navease-runtimeKit"
            isStatic = true
        }
    }

    jvm()

    js { browser() }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.stdlib)

                implementation(compose.runtime)
                implementation(compose.animation)
                // Navigation — exposed as api so consumers (e.g. :shared) can use NavKey directly
                api(libs.androidx.navigation3.ui)
            }
        }
    }
}

mavenPublishing {
    configure(
        KotlinMultiplatform(
            javadocJar = JavadocJar.Dokka("dokkaHtml"),
            sourcesJar = true,
        ),
    )

    pom {
        name.set("NavEase")
        description.set(
            "NavEase",
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

    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = false)
    signAllPublications()
}