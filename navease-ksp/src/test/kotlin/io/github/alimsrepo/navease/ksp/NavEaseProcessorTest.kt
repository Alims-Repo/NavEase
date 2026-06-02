package io.github.alimsrepo.navease.ksp

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspSourcesDir
import com.tschuchort.compiletesting.symbolProcessorProviders
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Compilation tests for [NavEaseProcessor].
 */
@OptIn(ExperimentalCompilerApi::class)
class NavEaseProcessorTest {

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

        annotation class AutoRegister
        """.trimIndent()
    )

    private val navScreenStub = SourceFile.kotlin(
        "NavScreen.kt",
        """
        package io.github.alimsrepo.navease.runtime.domain
        abstract class NavScreen
        """.trimIndent()
    )

    private val activityScreenStub = SourceFile.kotlin(
        "ActivityScreen.kt",
        """
        package io.github.alimsrepo.navease.runtime.screen
        import io.github.alimsrepo.navease.internal.runtime.NavKey
        abstract class ActivityScreen<K : NavKey>
        """.trimIndent()
    )

    private val navEaseRootStub = SourceFile.kotlin(
        "NavEaseRoot.kt",
        """
        package io.github.alimsrepo.navease.runtime
        import io.github.alimsrepo.navease.internal.runtime.NavKey
        interface NavEaseRoot : NavKey
        """.trimIndent()
    )

    private val navKeyStub = SourceFile.kotlin(
        "NavKey.kt",
        """
        package io.github.alimsrepo.navease.internal.runtime
        interface NavKey
        """.trimIndent()
    )

    private fun compile(vararg sources: SourceFile, kspOptions: Map<String, String> = emptyMap()): KotlinCompilation {
        return KotlinCompilation().apply {
            this.sources = listOf(annotationStubs, navScreenStub, activityScreenStub, navEaseRootStub, navKeyStub) + sources.toList()
            this.symbolProcessorProviders = mutableListOf(NavEaseProcessorProvider())
            this.inheritClassPath = true
        }
    }

    private fun KotlinCompilation.generatedFile(name: String): File =
        kspSourcesDir.walkTopDown().first { it.name == name }

    @Test
    fun `single screen no args — generates AppScreens ScreenFactory NavEaseExtensions NavEaseHost`() {
        val source = SourceFile.kotlin(
            "SplashScreen.kt",
            """
            import io.github.alimsrepo.navease.runtime.annotations.NavEaseScreen
            import io.github.alimsrepo.navease.runtime.domain.NavScreen

            @NavEaseScreen(route = "Splash", startDestination = true)
            class SplashScreen : NavScreen()
            """.trimIndent()
        )

        val compilation = compile(source)
        val result = compilation.compile()

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `autoRegister with flat hierarchy — generates correct qualKey in serializer`() {
        val source = SourceFile.kotlin(
            "FlatScreens.kt",
            """
            import io.github.alimsrepo.navease.runtime.annotations.AutoRegister
            import io.github.alimsrepo.navease.runtime.screen.ActivityScreen
            import io.github.alimsrepo.navease.runtime.NavEaseRoot

            sealed interface MyRoot : NavEaseRoot
            data class DetailKey(val id: String) : MyRoot

            @AutoRegister
            class DetailScreen : ActivityScreen<DetailKey>()
            """.trimIndent()
        )

        val compilation = compile(source)
        val result = compilation.compile()

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val autoScreens = compilation.generatedFile("AutoRegisterScreens.kt").readText()
        assertTrue("Serializer should use DetailKey as qualKey", "buildClassSerialDescriptor(\"DetailKey\")" in autoScreens)
        assertTrue("addEntry should use DetailKey::class", "DetailKey::class" in autoScreens)
    }

    @Test
    fun `autoRegister fails if key does not extend NavEaseRoot`() {
        val source = SourceFile.kotlin(
            "InvalidScreens.kt",
            """
            import io.github.alimsrepo.navease.runtime.annotations.AutoRegister
            import io.github.alimsrepo.navease.runtime.screen.ActivityScreen
            import io.github.alimsrepo.navease.internal.runtime.NavKey

            class JustKey : NavKey // Does NOT extend NavEaseRoot

            @AutoRegister
            class MyScreen : ActivityScreen<JustKey>()
            """.trimIndent()
        )

        val compilation = compile(source)
        val result = compilation.compile()

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
    }
}
