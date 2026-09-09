package io.github.alimsrepo.navease.gradle

import org.gradle.testkit.runner.GradleRunner
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/**
 * Configures a real build with the plugin applied.
 *
 * TestKit injects the plugin into a classloader that is a parent of the build script's, so a
 * Kotlin Gradle plugin declared by the test project is not visible to it. That rules out
 * functional coverage of the multiplatform wiring here; the repository's own `:shared` module
 * applies this plugin through a composite build and covers that end to end.
 *
 * What is worth asserting at this level is that the plugin can be instantiated and reports a
 * usable message when the build is not a Kotlin Multiplatform one.
 */
class NavEasePluginTest {

    @get:Rule
    val tmpDir = TemporaryFolder()

    private fun project(buildScript: String): File {
        val dir = tmpDir.root
        File(dir, "settings.gradle.kts").writeText(
            """
            rootProject.name = "test-project"
            dependencyResolutionManagement {
                repositories {
                    google()
                    mavenCentral()
                }
            }
            """.trimIndent(),
        )
        File(dir, "build.gradle.kts").writeText(buildScript)
        return dir
    }

    private fun run(dir: File, vararg args: String) =
        GradleRunner.create()
            .withProjectDir(dir)
            .withPluginClasspath()
            .withArguments(*args)
            .forwardOutput()

    @Test
    fun `the plugin can be instantiated without the Kotlin Gradle plugin present`() {
        // Gradle resolves the types named in a plugin class's method signatures when it
        // decorates the class. When those included KotlinMultiplatformExtension, applying the
        // plugin died with NoClassDefFoundError before any NavEase code ran.
        val dir = project(
            """
            plugins { id("io.github.alims-repo.navease") }
            """.trimIndent(),
        )

        val result = run(dir, "help").buildAndFail()

        assertTrue(
            "expected a NavEase message, got:\n${result.output}",
            result.output.contains("[NavEase]"),
        )
        assertTrue(
            "expected the failure to name the missing plugin, got:\n${result.output}",
            result.output.contains("Kotlin Multiplatform") ||
                result.output.contains("com.google.devtools.ksp"),
        )
    }
}
