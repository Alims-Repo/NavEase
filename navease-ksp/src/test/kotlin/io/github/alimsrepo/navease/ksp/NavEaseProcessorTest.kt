package io.github.alimsrepo.navease.ksp

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.configureKsp
import com.tschuchort.compiletesting.kspProcessorOptions
import com.tschuchort.compiletesting.kspSourcesDir
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi

/**
 * Runs the processor over real sources, covering resolution and the build-time checks that
 * [NavEaseCodegenTest] cannot reach.
 *
 * Only KSP runs. The generated code references Compose and the runtime, so compiling it here
 * would mean vendoring a large stub surface for no extra coverage; these tests assert on the
 * generated sources and on the diagnostics instead.
 */
@OptIn(ExperimentalCompilerApi::class)
class NavEaseProcessorTest {

    @get:Rule
    val workingDir = TemporaryFolder()

    private val generatedPackage = "com.example.generated.app"

    private fun stubs() = listOf(
        SourceFile.kotlin(
            "NavKey.kt",
            """
            package io.github.alimsrepo.navease.internal.runtime
            interface NavKey
            """.trimIndent(),
        ),
        SourceFile.kotlin(
            "NavEaseRoot.kt",
            """
            package io.github.alimsrepo.navease.runtime
            import io.github.alimsrepo.navease.internal.runtime.NavKey
            interface NavEaseRoot : NavKey
            """.trimIndent(),
        ),
        SourceFile.kotlin(
            "ActivityScreen.kt",
            """
            package io.github.alimsrepo.navease.runtime.screen
            import io.github.alimsrepo.navease.internal.runtime.NavKey
            abstract class ActivityScreen<K : NavKey>
            """.trimIndent(),
        ),
        SourceFile.kotlin(
            "AutoRegister.kt",
            """
            package io.github.alimsrepo.navease.runtime.annotations
            @Target(AnnotationTarget.CLASS)
            @Retention(AnnotationRetention.SOURCE)
            annotation class AutoRegister
            """.trimIndent(),
        ),
    )

    private class Result(
        val exitCode: KotlinCompilation.ExitCode,
        val messages: String,
        val generated: Map<String, String>,
    ) {
        fun file(nameFragment: String): String =
            generated.entries.firstOrNull { it.key.contains(nameFragment) }?.value
                ?: error("No generated file matching '$nameFragment'. Got: ${generated.keys}")
    }

    private fun process(vararg sources: SourceFile): Result {
        val compilation = KotlinCompilation().apply {
            workingDir = this@NavEaseProcessorTest.workingDir.root
            this.sources = stubs() + sources
            inheritClassPath = true
            messageOutputStream = java.io.ByteArrayOutputStream()
            configureKsp {
                symbolProcessorProviders.add(NavEaseProcessorProvider())
                processorOptions["navease.generatedPackage"] = generatedPackage
            }
        }
        val result = compilation.compile()
        val generated = compilation.kspSourcesDir
            .walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .associate { it.name to it.readText() }
        return Result(result.exitCode, result.messages, generated)
    }

    private fun appScreens(body: String) = SourceFile.kotlin(
        "AppScreens.kt",
        """
        package com.example
        import io.github.alimsrepo.navease.runtime.NavEaseRoot
        import io.github.alimsrepo.navease.runtime.annotations.AutoRegister
        import io.github.alimsrepo.navease.runtime.screen.ActivityScreen
        $body
        """.trimIndent(),
    )

    // ── Happy path ───────────────────────────────────────────────────────────

    @Test
    fun `registers a data object and a data class key`() {
        val result = process(
            appScreens(
                """
                sealed class AppScreens : NavEaseRoot {
                    data object Home : AppScreens()
                    data class Detail(val itemId: String, val page: Int) : AppScreens()
                }
                @AutoRegister class HomeScreen : ActivityScreen<AppScreens.Home>()
                @AutoRegister class DetailScreen : ActivityScreen<AppScreens.Detail>()
                """,
            ),
        )

        val source = result.file("AutoRegisterScreens")
        assertTrue(source.contains("{ com.example.HomeScreen() }"))
        assertTrue(source.contains("{ com.example.DetailScreen() }"))
        assertTrue(source.contains("typeOf<kotlin.String>()"))
        assertTrue(source.contains("typeOf<kotlin.Int>()"))
        assertTrue(source.contains("com.example.AppScreens::class"))
    }

    @Test
    fun `entries are emitted in a stable order regardless of declaration order`() {
        val declared = process(
            appScreens(
                """
                sealed class AppScreens : NavEaseRoot {
                    data object Zulu : AppScreens()
                    data object Alpha : AppScreens()
                }
                @AutoRegister class ZuluScreen : ActivityScreen<AppScreens.Zulu>()
                @AutoRegister class AlphaScreen : ActivityScreen<AppScreens.Alpha>()
                """,
            ),
        ).file("AutoRegisterScreens")

        // Sorted by key FQN, so Alpha precedes Zulu even though Zulu is declared first.
        assertTrue(declared.indexOf("AppScreens.Alpha") < declared.indexOf("AppScreens.Zulu"))
    }

    @Test
    fun `a key under an intermediate sealed layer still resolves to the outermost root`() {
        // The old parent-declaration lookup stopped at Tab, and the host for AppScreens then
        // reported that it had no screens.
        val result = process(
            appScreens(
                """
                sealed class AppScreens : NavEaseRoot {
                    sealed class Tab : AppScreens() {
                        data object Feed : Tab()
                    }
                }
                @AutoRegister class FeedScreen : ActivityScreen<AppScreens.Tab.Feed>()
                """,
            ),
        )

        val source = result.file("AutoRegisterScreens")
        assertTrue(
            "expected the root to be AppScreens, got:\n$source",
            source.contains("com.example.AppScreens.Tab.Feed::class, com.example.AppScreens::class"),
        )
    }

    @Test
    fun `two roots each get their own host overload`() {
        val result = process(
            appScreens(
                """
                sealed class AppScreens : NavEaseRoot {
                    data object Home : AppScreens()
                }
                sealed class WizardStep : NavEaseRoot {
                    data object Pick : WizardStep()
                }
                @AutoRegister class HomeScreen : ActivityScreen<AppScreens.Home>()
                @AutoRegister class PickScreen : ActivityScreen<WizardStep.Pick>()
                """,
            ),
        )

        val overloads = result.file("NavEaseHostOverloads")
        assertTrue(overloads.contains("reified T : com.example.AppScreens>"))
        assertTrue(overloads.contains("reified T : com.example.WizardStep>"))
    }

    @Test
    fun `the host overload file name follows the generated package`() {
        val result = process(
            appScreens(
                """
                sealed class AppScreens : NavEaseRoot { data object Home : AppScreens() }
                @AutoRegister class HomeScreen : ActivityScreen<AppScreens.Home>()
                """,
            ),
        )
        assertTrue(
            result.generated.keys.any { it == "NavEaseHostOverloads_com_example_generated_app.kt" },
        )
    }

    @Test
    fun `no annotated screens generates nothing`() {
        val result = process(
            appScreens(
                """
                sealed class AppScreens : NavEaseRoot { data object Home : AppScreens() }
                class HomeScreen : ActivityScreen<AppScreens.Home>()
                """,
            ),
        )
        assertEquals(emptyMap<String, String>(), result.generated)
    }

    // ── Validation ───────────────────────────────────────────────────────────

    @Test
    fun `two screens claiming one key is an error`() {
        val result = process(
            appScreens(
                """
                sealed class AppScreens : NavEaseRoot { data object Home : AppScreens() }
                @AutoRegister class HomeScreen : ActivityScreen<AppScreens.Home>()
                @AutoRegister class OtherHomeScreen : ActivityScreen<AppScreens.Home>()
                """,
            ),
        )

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("is claimed by 2 @AutoRegister screens"))
    }

    @Test
    fun `a screen that does not extend ActivityScreen is an error`() {
        val result = process(
            appScreens(
                """
                sealed class AppScreens : NavEaseRoot { data object Home : AppScreens() }
                @AutoRegister class HomeScreen
                """,
            ),
        )

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("must extend ActivityScreen<K> directly"))
    }

    @Test
    fun `an intermediate base class is an error, and says why`() {
        val result = process(
            appScreens(
                """
                sealed class AppScreens : NavEaseRoot { data object Home : AppScreens() }
                abstract class TrackedScreen<K : io.github.alimsrepo.navease.internal.runtime.NavKey>
                    : ActivityScreen<K>()
                @AutoRegister class HomeScreen : TrackedScreen<AppScreens.Home>()
                """,
            ),
        )

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("must extend ActivityScreen<K> directly"))
    }

    @Test
    fun `a key that is not a NavEaseRoot is an error`() {
        val result = process(
            appScreens(
                """
                object PlainKey : io.github.alimsrepo.navease.internal.runtime.NavKey
                @AutoRegister class PlainScreen : ActivityScreen<PlainKey>()
                """,
            ),
        )

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("does not implement NavEaseRoot"))
    }

    @Test
    fun `a screen needing constructor arguments is an error`() {
        // Generated code calls ScreenClass(), so this would not compile downstream.
        val result = process(
            appScreens(
                """
                sealed class AppScreens : NavEaseRoot { data object Home : AppScreens() }
                @AutoRegister class HomeScreen(val repo: String) : ActivityScreen<AppScreens.Home>()
                """,
            ),
        )

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("must have a no-argument constructor"))
    }

    @Test
    fun `an abstract screen is an error`() {
        val result = process(
            appScreens(
                """
                sealed class AppScreens : NavEaseRoot { data object Home : AppScreens() }
                @AutoRegister abstract class HomeScreen : ActivityScreen<AppScreens.Home>()
                """,
            ),
        )

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("must be a concrete class"))
    }
}
