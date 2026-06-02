import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform

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
        // Explicitly target JVM 11 so inline functions in this library
        // produce JVM-11-compatible bytecode — consumers targeting JVM 11+ work fine.
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "navease-runtimeKit"
            isStatic = true
        }
    }

    jvm {
        // Same reason: pin the output bytecode to JVM 11 for broad consumer compatibility.
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    js { browser() }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain {
            dependencies {
                api("androidx.navigationevent:navigationevent:1.1.1")
                api("androidx.navigationevent:navigationevent-compose:1.1.1")

                implementation("androidx.collection:collection:1.6.0")

                implementation("androidx.lifecycle:lifecycle-runtime:2.10.0")
                implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")

                implementation("org.jetbrains.compose.runtime:runtime:1.11.0")
                implementation("org.jetbrains.compose.animation:animation:1.11.0")

                // Serialization — needed for KSerializer in NavEaseGraph DSL
                implementation(libs.kotlinx.serializationCore)
            }
        }

        androidMain.dependencies {
            implementation("androidx.core:core-ktx:1.1.0")
        }
    }
}

mavenPublishing {
    configure(
        KotlinMultiplatform(
            javadocJar = JavadocJar.Dokka("dokkaGeneratePublicationHtml"),
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

    publishToMavenCentral()
    signAllPublications()
}