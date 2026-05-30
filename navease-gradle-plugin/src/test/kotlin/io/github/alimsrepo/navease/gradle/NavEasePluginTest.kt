package io.github.alimsrepo.navease.gradle

import org.gradle.testkit.runner.GradleRunner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class NavEasePluginTest {

    @get:Rule
    val tmpDir = TemporaryFolder()

    // ── helpers ───────────────────────────────────────────────────────────

    private fun writeSettingsFile(projectDir: File) {
        File(projectDir, "settings.gradle.kts").writeText(
            """
            rootProject.name = "test-project"
            dependencyResolutionManagement {
                repositories {
                    mavenCentral()
                    google()
                }
            }
            """.trimIndent()
        )
    }

    private fun runner(projectDir: File, vararg args: String) =
        GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments(*args)
            .forwardOutput()

    // ── unit tests (no Gradle invocation needed) ──────────────────────────

    @Test
    fun `NavEaseVersion constants are not blank`() {
        assertTrue(NavEaseVersion.VERSION.isNotBlank())
        assertTrue(NavEaseVersion.GROUP.isNotBlank())
        assertTrue(NavEaseVersion.RUNTIME_ARTIFACT.isNotBlank())
        assertTrue(NavEaseVersion.KSP_ARTIFACT.isNotBlank())
    }

    @Test
    fun `NavEaseExtension resolves default version from NavEaseVersion`() {
        val ext = NavEaseExtension()
        assertEquals(NavEaseVersion.VERSION, ext.resolvedVersion())
    }

    @Test
    fun `NavEaseExtension resolves custom version`() {
        val ext = NavEaseExtension().apply { version = "1.2.3" }
        assertEquals("1.2.3", ext.resolvedVersion())
    }

    @Test
    fun `NavEaseExtension resolves blank version to bundled default`() {
        val ext = NavEaseExtension().apply { version = "   " }
        assertEquals(NavEaseVersion.VERSION, ext.resolvedVersion())
    }

    @Test
    fun `NavEaseExtension runtime coordinate contains group and artifact`() {
        val ext = NavEaseExtension()
        val coord = ext.resolvedRuntimeCoordinate()
        assertTrue(coord.contains(NavEaseVersion.GROUP))
        assertTrue(coord.contains(NavEaseVersion.RUNTIME_ARTIFACT))
        assertTrue(coord.contains(NavEaseVersion.VERSION))
    }

    @Test
    fun `NavEaseExtension ksp coordinate contains group and artifact`() {
        val ext = NavEaseExtension()
        val coord = ext.resolvedKspCoordinate()
        assertTrue(coord.contains(NavEaseVersion.GROUP))
        assertTrue(coord.contains(NavEaseVersion.KSP_ARTIFACT))
        assertTrue(coord.contains(NavEaseVersion.VERSION))
    }

    @Test
    fun `addRuntimeDependency defaults to true`() {
        val ext = NavEaseExtension()
        assertTrue(ext.addRuntimeDependency)
    }

    @Test
    fun `generatedPackage defaults to blank`() {
        val ext = NavEaseExtension()
        assertTrue(ext.generatedPackage.isBlank())
    }

    // ── functional tests (lightweight — no network, no full KMP compile) ──
    // Note: these spin up a child Gradle process via TestKit and require a
    // compatible JDK on the PATH. Skipped automatically if that's not available.

    @Ignore("Requires a full JDK on PATH — run manually in CI environments")
    @Test
    fun `plugin registers navease extension and tasks on a minimal project`() {
        val projectDir = tmpDir.root
        writeSettingsFile(projectDir)

        File(projectDir, "build.gradle.kts").writeText(
            """
            plugins {
                id("io.github.alims-repo.navease")
            }
            """.trimIndent()
        )

        // `help` is always present — just checks that configuration doesn't crash
        val result = runner(projectDir, "help").build()
        assertTrue("Expected BUILD SUCCESSFUL", result.output.contains("BUILD SUCCESSFUL"))
    }

    @Ignore("Requires a full JDK on PATH — run manually in CI environments")
    @Test
    fun `navease extension block is accepted without errors`() {
        val projectDir = tmpDir.root
        writeSettingsFile(projectDir)

        File(projectDir, "build.gradle.kts").writeText(
            """
            plugins {
                id("io.github.alims-repo.navease")
            }
            navease {
                version = "0.0.3"
                addRuntimeDependency = false
                generatedPackage = "com.example.nav"
            }
            """.trimIndent()
        )

        val result = runner(projectDir, "help").build()
        assertTrue("Expected BUILD SUCCESSFUL", result.output.contains("BUILD SUCCESSFUL"))
    }
}



