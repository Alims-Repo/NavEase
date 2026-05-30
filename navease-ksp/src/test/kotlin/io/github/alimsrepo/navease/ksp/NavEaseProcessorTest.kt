package io.github.alimsrepo.navease.ksp

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspSourcesDir
import com.tschuchort.compiletesting.symbolProcessorProviders
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Compilation tests for [NavEaseProcessor].
 *
 * Each test:
 * 1. Feeds synthetic source files into a KotlinCompilation that includes the processor.
 * 2. Verifies that the KSP run succeeds (exit code == OK).
 * 3. Asserts on the content of generated files.
 *
 * These tests do NOT run on Android devices — they compile against the JVM target only,
 * which is sufficient to verify KSP output correctness since the processor itself is
 * platform-agnostic.
 */
class NavEaseProcessorTest {

    // ── Annotation stubs ─────────────────────────────────────────────────────
    // The processor resolves annotations by their FQN string — it does not import the
    // annotation classes. We still declare minimal stubs so the test source files compile.

    private val annotationStubs = SourceFile.kotlin(
        "NavEaseAnnotations.kt",
        """
        package io.github.alimsrepo.navease.runtime.annotations

        @Target(AnnotationTarget.CLASS)
        @Retention(AnnotationRetention.SOURCE)
        annotation class NavEaseScreen(val route: String, val startDestination: Boolean = false)

        @Target(AnnotationTarget.CLASS)
        @Retention(AnnotationRetention.SOURCE)
        annotation class NavEaseArgs

        @Target(AnnotationTarget.CLASS)
        @Retention(AnnotationRetention.SOURCE)
        annotation class NavEaseResult
        """.trimIndent()
    )

    private val navScreenStub = SourceFile.kotlin(
        "NavScreen.kt",
        """
        package io.github.alimsrepo.navease.runtime.domain

        abstract class NavScreen
        """.trimIndent()
    )

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun compile(vararg sources: SourceFile, kspOptions: Map<String, String> = emptyMap()): KotlinCompilation {
        return KotlinCompilation().apply {
            this.sources = listOf(annotationStubs, navScreenStub) + sources.toList()
            symbolProcessorProviders = listOf(NavEaseProcessorProvider())
            // KSP processor options — passed via kspArgs in dev.zacsweers.kctfork
            kspOptions.forEach { (k, v) -> kspArgs[k] = v }
            inheritClassPath = true
        }
    }

    private fun KotlinCompilation.generatedFile(name: String): File =
        kspSourcesDir.walkTopDown().first { it.name == name }

    // ── Tests ─────────────────────────────────────────────────────────────────

    /** A single @NavEaseScreen class with no args or result generates all 5 files. */
    @Test
    fun `single screen no args — generates AppScreens ScreenFactory NavEaseExtensions NavEaseHost`() {
        val source = SourceFile.kotlin(
            "SplashScreen.kt",
            """
            import io.github.alimsrepo.navease.runtime.annotations.NavEaseScreen
            import io.github.alimsrepo.navease.runtime.domain.NavScreen
            import androidx.navigation3.runtime.NavKey

            @NavEaseScreen(route = "Splash", startDestination = true)
            class SplashScreen : NavScreen()
            """.trimIndent()
        )

        val compilation = compile(source)
        val result = compilation.compile()

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        // AppScreens — should have a data object Splash
        val appScreens = compilation.generatedFile("AppScreens.kt").readText()
        assertTrue("AppScreens should declare 'data object Splash'", "data object Splash" in appScreens)
        assertTrue("startDestination should point to Splash", "get() = Splash" in appScreens)

        // ScreenFactory — should have a when branch for Splash
        val factory = compilation.generatedFile("ScreenFactory.kt").readText()
        assertTrue("ScreenFactory should handle AppScreens.Splash", "AppScreens.Splash" in factory)

        // NavEaseExtensions — navigateToSplash()
        val extensions = compilation.generatedFile("NavEaseExtensions.kt").readText()
        assertTrue("Extensions should have navigateToSplash", "navigateToSplash" in extensions)

        // NavEaseHost — generated composable
        val host = compilation.generatedFile("NavEaseHost.kt").readText()
        assertTrue("Host should have NavEaseHost function", "fun NavEaseHost" in host)

        // NavEaseResults — should NOT be generated when no @NavEaseResult exists
        val noResults = compilation.kspSourcesDir.walkTopDown().none { it.name == "NavEaseResults.kt" }
        assertTrue("NavEaseResults.kt should not be generated when no @NavEaseResult", noResults)
    }

    /** A screen with @NavEaseArgs generates a data class route and a typed navigateToXxx extension. */
    @Test
    fun `screen with NavEaseArgs — generates data class route and typed navigate extension`() {
        val source = SourceFile.kotlin(
            "DetailScreen.kt",
            """
            import io.github.alimsrepo.navease.runtime.annotations.NavEaseScreen
            import io.github.alimsrepo.navease.runtime.annotations.NavEaseArgs
            import io.github.alimsrepo.navease.runtime.domain.NavScreen

            @NavEaseScreen(route = "Detail", startDestination = true)
            class DetailScreen : NavScreen() {
                @NavEaseArgs
                data class Args(val itemId: Int, val label: String)
            }
            """.trimIndent()
        )

        val compilation = compile(source)
        val result = compilation.compile()

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val appScreens = compilation.generatedFile("AppScreens.kt").readText()
        assertTrue(
            "AppScreens.Detail should be a data class with itemId and label",
            "data class Detail(val itemId: Int, val label: String)" in appScreens
        )

        val extensions = compilation.generatedFile("NavEaseExtensions.kt").readText()
        assertTrue("navigateToDetail should accept itemId", "itemId: Int" in extensions)
        assertTrue("navigateToDetail should accept label", "label: String" in extensions)
        assertTrue("xxxArgs() extension should be generated", "detailArgs()" in extensions)
        assertTrue(
            "xxxArgs() should use safe cast (as?), not hard cast",
            "as? AppScreens.Detail" in extensions
        )
    }

    /** Generated files must start with `package …` at column 0 — no stray leading whitespace. */
    @Test
    fun `generated files — no stray leading whitespace on package declaration`() {
        val source = SourceFile.kotlin(
            "NavScreen.kt",
            """
            import io.github.alimsrepo.navease.runtime.annotations.NavEaseScreen
            import io.github.alimsrepo.navease.runtime.annotations.NavEaseArgs
            import io.github.alimsrepo.navease.runtime.domain.NavScreen

            @NavEaseScreen(route = "Nav", startDestination = true)
            class NavScreen : NavScreen() {
                @NavEaseArgs
                data class Args(val a: String, val b: String)
            }
            """.trimIndent()
        )

        val compilation = compile(source)
        assertEquals(KotlinCompilation.ExitCode.OK, compilation.compile().exitCode)

        listOf("AppScreens.kt", "ScreenFactory.kt", "NavEaseExtensions.kt", "NavEaseHost.kt").forEach { name ->
            val text = compilation.generatedFile(name).readText()
            assertTrue(
                "$name must start with 'package' at column 0 (no stray indentation)",
                text.trimStart().startsWith("package ")
            )
            assertTrue(
                "$name first line must not have leading spaces",
                text.lines().first { it.isNotBlank() }.startsWith("package ")
            )
        }
    }

    /** A screen with @NavEaseResult generates NavEaseResults.kt with typed extensions. */
    @Test
    fun `screen with NavEaseResult — generates NavEaseResults with typed extensions`() {
        val source = SourceFile.kotlin(
            "ImagePickerScreen.kt",
            """
            import io.github.alimsrepo.navease.runtime.annotations.NavEaseScreen
            import io.github.alimsrepo.navease.runtime.annotations.NavEaseResult
            import io.github.alimsrepo.navease.runtime.domain.NavScreen

            @NavEaseScreen(route = "ImagePicker", startDestination = true)
            class ImagePickerScreen : NavScreen() {
                @NavEaseResult
                data class Result(val imageUri: String, val success: Boolean)
            }
            """.trimIndent()
        )

        val compilation = compile(source)
        val result = compilation.compile()

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val results = compilation.generatedFile("NavEaseResults.kt").readText()
        assertTrue("ImagePickerResult data class should be generated", "data class ImagePickerResult" in results)
        assertTrue("backWithImagePickerResult should be generated", "backWithImagePickerResult" in results)
        assertTrue("imagePickerResult() composable should be generated", "fun NavController.imagePickerResult" in results)
    }

    /** A custom type used in @NavEaseArgs gets an import emitted in the generated file. */
    @Test
    fun `custom type in NavEaseArgs — emits import in generated files`() {
        val customType = SourceFile.kotlin(
            "SampleData.kt",
            """
            package com.example

            data class SampleData(val value: String)
            """.trimIndent()
        )
        val source = SourceFile.kotlin(
            "MainScreen.kt",
            """
            import io.github.alimsrepo.navease.runtime.annotations.NavEaseScreen
            import io.github.alimsrepo.navease.runtime.annotations.NavEaseArgs
            import io.github.alimsrepo.navease.runtime.domain.NavScreen
            import com.example.SampleData

            @NavEaseScreen(route = "Main", startDestination = true)
            class MainScreen : NavScreen() {
                @NavEaseArgs
                data class Args(val data: SampleData)
            }
            """.trimIndent()
        )

        val compilation = compile(customType, source)
        val result = compilation.compile()

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val appScreens = compilation.generatedFile("AppScreens.kt").readText()
        assertTrue(
            "AppScreens.kt should import com.example.SampleData",
            "import com.example.SampleData" in appScreens
        )
    }

    /** The `navease.generatedPackage` KSP option changes the output package of all files. */
    @Test
    fun `navease_generatedPackage option — changes package of all generated files`() {
        val source = SourceFile.kotlin(
            "HomeScreen.kt",
            """
            import io.github.alimsrepo.navease.runtime.annotations.NavEaseScreen
            import io.github.alimsrepo.navease.runtime.domain.NavScreen

            @NavEaseScreen(route = "Home", startDestination = true)
            class HomeScreen : NavScreen()
            """.trimIndent()
        )

        val customPkg = "com.myapp.nav.generated"
        val compilation = compile(source, kspOptions = mapOf("navease.generatedPackage" to customPkg))
        val result = compilation.compile()

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        listOf("AppScreens.kt", "ScreenFactory.kt", "NavEaseExtensions.kt", "NavEaseHost.kt").forEach { fileName ->
            val file = compilation.generatedFile(fileName)
            assertTrue(
                "$fileName should declare package $customPkg",
                "package $customPkg" in file.readText()
            )
        }
    }

    /** Generic types in @NavEaseArgs generate correct type params and collect inner imports. */
    @Test
    fun `generic types in NavEaseArgs — correct short name and imports`() {
        val customType = SourceFile.kotlin(
            "Item.kt",
            """
            package com.example

            data class Item(val id: Int)
            """.trimIndent()
        )
        val source = SourceFile.kotlin(
            "ListScreen.kt",
            """
            import io.github.alimsrepo.navease.runtime.annotations.NavEaseScreen
            import io.github.alimsrepo.navease.runtime.annotations.NavEaseArgs
            import io.github.alimsrepo.navease.runtime.domain.NavScreen
            import com.example.Item

            @NavEaseScreen(route = "ItemList", startDestination = true)
            class ItemListScreen : NavScreen() {
                @NavEaseArgs
                data class Args(
                    val items: List<Item>,
                    val mapping: Map<String, Item>,
                    val maybeItem: Item?
                )
            }
            """.trimIndent()
        )

        val compilation = compile(customType, source)
        val result = compilation.compile()

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val appScreens = compilation.generatedFile("AppScreens.kt").readText()
        // List<Item> should appear
        assertTrue("AppScreens should use List<Item>", "List<Item>" in appScreens)
        // Map<String, Item> should appear
        assertTrue("AppScreens should use Map<String, Item>", "Map<String, Item>" in appScreens)
        // Custom type import should be present
        assertTrue("AppScreens.kt should import com.example.Item", "import com.example.Item" in appScreens)
    }

    /** Duplicate route names must cause the processor to fail (exit code != OK). */
    @Test
    fun `duplicate route names — processor reports error and does not generate`() {
        val source = SourceFile.kotlin(
            "DuplicateScreens.kt",
            """
            import io.github.alimsrepo.navease.runtime.annotations.NavEaseScreen
            import io.github.alimsrepo.navease.runtime.domain.NavScreen

            @NavEaseScreen(route = "Home", startDestination = true)
            class HomeScreen : NavScreen()

            @NavEaseScreen(route = "Home")
            class HomeDuplicateScreen : NavScreen()
            """.trimIndent()
        )

        val compilation = compile(source)
        val result = compilation.compile()

        // KSP logger.error() causes the compilation to fail
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
    }

    /** More than one startDestination = true emits a warning but still generates successfully. */
    @Test
    fun `multiple startDestination — processor warns but picks the first one`() {
        val source = SourceFile.kotlin(
            "MultiStartScreens.kt",
            """
            import io.github.alimsrepo.navease.runtime.annotations.NavEaseScreen
            import io.github.alimsrepo.navease.runtime.domain.NavScreen

            @NavEaseScreen(route = "Alpha", startDestination = true)
            class AlphaScreen : NavScreen()

            @NavEaseScreen(route = "Beta", startDestination = true)
            class BetaScreen : NavScreen()
            """.trimIndent()
        )

        val compilation = compile(source)
        val result = compilation.compile()

        // Should still compile — warning only, not error
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        // First declared start destination wins
        val appScreens = compilation.generatedFile("AppScreens.kt").readText()
        assertTrue("startDestination should point to Alpha (first)", "get() = Alpha" in appScreens)
    }

    /** Multiple screens: exactly one marked `startDestination = true` sets the correct default. */
    @Test
    fun `multiple screens — startDestination is wired to the correct route`() {
        val source = SourceFile.kotlin(
            "Screens.kt",
            """
            import io.github.alimsrepo.navease.runtime.annotations.NavEaseScreen
            import io.github.alimsrepo.navease.runtime.domain.NavScreen

            @NavEaseScreen(route = "Login", startDestination = true)
            class LoginScreen : NavScreen()

            @NavEaseScreen(route = "Dashboard")
            class DashboardScreen : NavScreen()

            @NavEaseScreen(route = "Settings")
            class SettingsScreen : NavScreen()
            """.trimIndent()
        )

        val compilation = compile(source)
        val result = compilation.compile()

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val appScreens = compilation.generatedFile("AppScreens.kt").readText()
        assertTrue("startDestination should point to Login", "get() = Login" in appScreens)
        assertTrue("AppScreens should have Dashboard", "data object Dashboard" in appScreens)
        assertTrue("AppScreens should have Settings", "data object Settings" in appScreens)

        val extensions = compilation.generatedFile("NavEaseExtensions.kt").readText()
        assertTrue("navigateToLogin should exist", "navigateToLogin" in extensions)
        assertTrue("navigateToDashboard should exist", "navigateToDashboard" in extensions)
        assertTrue("navigateToSettings should exist", "navigateToSettings" in extensions)
    }
}



