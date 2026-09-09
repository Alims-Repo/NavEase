package io.github.alimsrepo.navease.gradle

import io.github.alimsrepo.navease.gradle.NavEasePlugin.Companion.toPackageSegment
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NavEaseExtensionTest {

    private fun extension(): NavEaseExtension =
        ProjectBuilder.builder().build().objects.newInstance(NavEaseExtension::class.java)

    // ── Coordinates ──────────────────────────────────────────────────────────

    @Test
    fun `version defaults to the plugin's own`() {
        assertEquals(NavEaseVersion.VERSION, extension().resolvedVersion())
    }

    @Test
    fun `an explicit version wins`() {
        val ext = extension().apply { version.set("1.2.3") }
        assertEquals("1.2.3", ext.resolvedVersion())
        assertTrue(ext.resolvedRuntimeCoordinate().endsWith(":1.2.3"))
        assertTrue(ext.resolvedKspCoordinate().endsWith(":1.2.3"))
    }

    @Test
    fun `a blank version falls back to the plugin's own`() {
        val ext = extension().apply { version.set("   ") }
        assertEquals(NavEaseVersion.VERSION, ext.resolvedVersion())
    }

    @Test
    fun `coordinates name the runtime and processor artifacts`() {
        val ext = extension()
        assertTrue(ext.resolvedRuntimeCoordinate().contains(NavEaseVersion.RUNTIME_ARTIFACT))
        assertTrue(ext.resolvedKspCoordinate().contains(NavEaseVersion.KSP_ARTIFACT))
        assertTrue(ext.resolvedRuntimeCoordinate().startsWith("${NavEaseVersion.GROUP}:"))
    }

    @Test
    fun `dependency overrides take precedence over coordinates`() {
        val ext = extension().apply {
            kspProcessorDependency.set("com.example:custom-ksp:9.9.9")
            runtimeDependency.set("com.example:custom-runtime:9.9.9")
        }
        assertEquals("com.example:custom-ksp:9.9.9", ext.effectiveKspDependency())
        assertEquals("com.example:custom-runtime:9.9.9", ext.effectiveRuntimeDependency())
    }

    @Test
    fun `dependencies fall back to Maven coordinates when unset`() {
        val ext = extension()
        assertEquals(ext.resolvedKspCoordinate(), ext.effectiveKspDependency())
        assertEquals(ext.resolvedRuntimeCoordinate(), ext.effectiveRuntimeDependency())
    }

    // ── Package segment ──────────────────────────────────────────────────────

    @Test
    fun `module names become legal package segments`() {
        // Two modules generating into one package would produce duplicate classes, so the
        // module name is part of the default — it has to survive as an identifier.
        assertEquals("app", "app".toPackageSegment())
        assertEquals("my_feature", "my-feature".toPackageSegment())
        assertEquals("feature_a_b", "feature.a b".toPackageSegment())
        assertEquals("_2fa", "2fa".toPackageSegment())
        assertEquals("module", "".toPackageSegment())
    }
}
