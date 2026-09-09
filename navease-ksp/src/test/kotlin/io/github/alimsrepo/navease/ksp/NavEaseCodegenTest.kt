package io.github.alimsrepo.navease.ksp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Covers the emitted source. These are the regressions that previously reached users as
 * broken generated code, so each test names the failure it guards against.
 */
class NavEaseCodegenTest {

    private val pkg = "com.example.generated.app"

    private fun entry(
        screen: String = "com.example.HomeScreen",
        key: String = "com.example.AppScreens.Home",
        root: String = "com.example.AppScreens",
        params: List<KeyParam> = emptyList(),
    ) = ScreenEntry(screen, key, params, root)

    // ── Collisions across roots ──────────────────────────────────────────────

    @Test
    fun `two roots declaring the same key simple name do not collide`() {
        val entries = listOf(
            entry(
                screen = "com.example.AppDetailScreen",
                key = "com.example.AppScreens.Detail",
                root = "com.example.AppScreens",
            ),
            entry(
                screen = "com.example.WizardDetailScreen",
                key = "com.example.WizardStep.Detail",
                root = "com.example.WizardStep",
            ),
        )

        val source = NavEaseCodegen.autoRegisterScreens(pkg, entries)

        // Distinct serializer objects; a simple-name scheme produced NavEase_Detail_Ser twice.
        assertTrue(source.contains("NavEaseSer_com_example_AppScreens_Detail"))
        assertTrue(source.contains("NavEaseSer_com_example_WizardStep_Detail"))
        assertNotEquals(entries[0].serializerName, entries[1].serializerName)
    }

    @Test
    fun `generated source declares no imports`() {
        val source = NavEaseCodegen.autoRegisterScreens(
            pkg,
            listOf(entry(params = listOf(KeyParam("id", "kotlin.String")))),
        )
        // Imports are what made two same-named keys ambiguous; everything is qualified now.
        assertFalse(source.lineSequence().any { it.trimStart().startsWith("import ") })
    }

    @Test
    fun `each root gets exactly one host overload`() {
        val entries = listOf(
            entry(key = "com.example.AppScreens.Home", root = "com.example.AppScreens"),
            entry(key = "com.example.AppScreens.Detail", root = "com.example.AppScreens"),
            entry(key = "com.example.WizardStep.Pick", root = "com.example.WizardStep"),
        )

        val source = NavEaseCodegen.hostOverloads(pkg, entries)

        assertEquals(1, source.occurrencesOf("reified T : com.example.AppScreens>"))
        assertEquals(1, source.occurrencesOf("reified T : com.example.WizardStep>"))
    }

    // ── Multi-module ─────────────────────────────────────────────────────────

    @Test
    fun `host overload file name is derived from the generated package`() {
        // A fixed file name meant two modules emitted the same class into the runtime's
        // host package, which fails at dex or link time.
        assertNotEquals(
            NavEaseCodegen.hostOverloadsFileName("com.example.generated.app"),
            NavEaseCodegen.hostOverloadsFileName("com.example.generated.feature"),
        )
        assertEquals(
            "NavEaseHostOverloads_com_example_generated_app",
            NavEaseCodegen.hostOverloadsFileName("com.example.generated.app"),
        )
    }

    @Test
    fun `host overloads stay in the runtime host package`() {
        // They must share the package users import, or the overload never wins resolution.
        val source = NavEaseCodegen.hostOverloads(pkg, listOf(entry()))
        assertTrue(source.startsWith("package io.github.alimsrepo.navease.runtime.host"))
    }

    @Test
    fun `host overload bootstraps before hosting`() {
        val source = NavEaseCodegen.hostOverloads(pkg, listOf(entry()))
        val bootstrap = source.indexOf("$pkg.navEaseBootstrap()")
        val host = source.indexOf("NavEaseHostForRoot(")
        assertTrue("bootstrap call missing", bootstrap >= 0)
        assertTrue("bootstrap must precede the host", bootstrap < host)
    }

    @Test
    fun `host overload forwards every host parameter`() {
        // A stale overload that dropped a parameter used to send the call silently to the
        // runtime's generic host, which does not bootstrap.
        val source = NavEaseCodegen.hostOverloads(pkg, listOf(entry()))
        listOf(
            "start", "onExitRequest", "enableSharedTransitions",
            "navTransition", "onDestinationChanged",
        ).forEach { param ->
            assertTrue("missing parameter: $param", source.contains("$param = $param"))
        }
    }

    // ── Registration ─────────────────────────────────────────────────────────

    @Test
    fun `registration passes a factory rather than an instance`() {
        val source = NavEaseCodegen.autoRegisterScreens(pkg, listOf(entry()))
        assertTrue(source.contains("{ com.example.HomeScreen() }"))
    }

    @Test
    fun `registration names the key, the root and the serializer`() {
        val source = NavEaseCodegen.autoRegisterScreens(pkg, listOf(entry()))
        assertTrue(
            source.contains(
                "addEntry(com.example.AppScreens.Home::class, com.example.AppScreens::class, " +
                    "NavEaseSer_com_example_AppScreens_Home)",
            ),
        )
    }

    // ── Serializers ──────────────────────────────────────────────────────────

    @Test
    fun `data object serializer writes an empty structure`() {
        val source = NavEaseCodegen.serializer(entry())
        assertTrue(source.contains("encoder.beginStructure(descriptor).endStructure(descriptor)"))
        assertTrue(source.contains("return com.example.AppScreens.Home"))
    }

    @Test
    fun `unknown elements are skipped instead of throwing`() {
        // Removing a key parameter used to make an older saved back stack throw on restore.
        val source = NavEaseCodegen.serializer(
            entry(params = listOf(KeyParam("id", "kotlin.String"))),
        )
        assertTrue(source.contains("CompositeDecoder.UNKNOWN_NAME -> continue@loop"))
    }

    @Test
    fun `a missing element is reported by name`() {
        // Previously a null was cast to a non-null type, giving a bare ClassCastException.
        val source = NavEaseCodegen.serializer(
            entry(params = listOf(KeyParam("userId", "kotlin.String"))),
        )
        assertTrue(source.contains("if (!seen0) throw"))
        assertTrue(source.contains("parameter 'userId' is missing"))
    }

    @Test
    fun `every parameter is encoded and decoded at its own index`() {
        val source = NavEaseCodegen.serializer(
            entry(
                params = listOf(
                    KeyParam("itemId", "kotlin.String"),
                    KeyParam("count", "kotlin.Int"),
                ),
            ),
        )
        assertTrue(source.contains("out.encodeSerializableElement(descriptor, 0, f0, value.itemId)"))
        assertTrue(source.contains("out.encodeSerializableElement(descriptor, 1, f1, value.count)"))
        assertTrue(source.contains("element(\"itemId\", f0.descriptor)"))
        assertTrue(source.contains("element(\"count\", f1.descriptor)"))
        assertTrue(source.contains("itemId = v0 as kotlin.String, count = v1 as kotlin.Int"))
    }

    @Test
    fun `nullable and generic parameter types survive verbatim`() {
        val source = NavEaseCodegen.serializer(
            entry(
                params = listOf(
                    KeyParam("tags", "kotlin.collections.List<kotlin.String>"),
                    KeyParam("note", "kotlin.String?"),
                ),
            ),
        )
        assertTrue(source.contains("typeOf<kotlin.collections.List<kotlin.String>>()"))
        assertTrue(source.contains("typeOf<kotlin.String?>()"))
        assertTrue(source.contains("note = v1 as kotlin.String?"))
    }

    @Test
    fun `serial name is the fully qualified key`() {
        // It is the polymorphic discriminator, so it must be unique across roots.
        val source = NavEaseCodegen.serializer(entry())
        assertTrue(
            source.contains("buildClassSerialDescriptor(\"com.example.AppScreens.Home\")"),
        )
    }

    // ── Determinism ──────────────────────────────────────────────────────────

    @Test
    fun `output depends only on entry order, which the processor sorts`() {
        val entries = listOf(
            entry(key = "com.example.AppScreens.Alpha"),
            entry(key = "com.example.AppScreens.Beta"),
        )
        assertEquals(
            NavEaseCodegen.autoRegisterScreens(pkg, entries),
            NavEaseCodegen.autoRegisterScreens(pkg, entries),
        )
    }

    private fun String.occurrencesOf(needle: String): Int =
        split(needle).size - 1
}
