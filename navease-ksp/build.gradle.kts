import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm

plugins {
    alias(libs.plugins.kotlinJvm)

    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.dokka)
    alias(libs.plugins.binaryCompatibilityValidator)
}

kotlin {
    jvmToolchain(21)
}


tasks.withType<Test>().configureEach {
    failOnNoDiscoveredTests = false
}

dependencies {
    implementation(libs.ksp.api)

    testImplementation(libs.kotlin.testJunit)
    testImplementation(libs.junit)
    // Runs the processor over real sources so resolution and validation are covered,
    // not just the emitted strings.
    testImplementation(libs.kotlin.compile.testing.ksp)
    testImplementation(libs.ksp.api)
}

mavenPublishing {
    configure(
        KotlinJvm(
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