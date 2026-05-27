package io.github.alimsrepo.navease.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration

class NavEaseProcessor(
    private val codeGenerator: CodeGenerator
) : SymbolProcessor {

    private var generated = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (generated) return emptyList()

        val symbols = resolver.getSymbolsWithAnnotation("io.github.alimsrepo.navease.runtime.NavEaseScreen")
            .filterIsInstance<KSClassDeclaration>()
            .toList()

        if (symbols.isEmpty()) return emptyList()

        generated = true

        data class ScreenEntry(
            val route: String,
            val fqName: String,
            val isStart: Boolean
        )

        val entries = symbols.map { cls ->
            val annotation = cls.annotations.first { it.shortName.asString() == "NavEaseScreen" }
            val route = annotation.arguments.first { it.name?.asString() == "route" }.value as String
            val isStart = annotation.arguments.firstOrNull { it.name?.asString() == "startDestination" }?.value as? Boolean ?: false
            ScreenEntry(route, cls.qualifiedName!!.asString(), isStart)
        }

        // Fall back to first screen if none marked as startDestination
        val startEntry = entries.firstOrNull { it.isStart } ?: entries.first()

        generateAppScreens(entries.map { it.route to it.fqName }, startEntry.route)
        generateScreenFactory(entries.map { it.route to it.fqName })
        generateNavEaseHost()

        return emptyList()
    }

    private fun generateAppScreens(entries: List<Pair<String, String>>, startRoute: String) {
        val subclasses = entries.joinToString("\n    ") { (route, _) ->
            "@Serializable data object $route : AppScreens()"
        }
        val subclassEntries = entries.joinToString("\n                        ") { (route, _) ->
            "subclass($route::class)"
        }

        val file = codeGenerator.createNewFile(
            Dependencies(false),
            "io.github.alimsrepo.navease.generated",
            "AppScreens"
        )
        file.bufferedWriter().use {
            it.write("""
                package io.github.alimsrepo.navease.generated

                import androidx.compose.runtime.Stable
                import androidx.navigation3.runtime.NavKey
                import androidx.savedstate.serialization.SavedStateConfiguration
                import kotlinx.serialization.Serializable
                import kotlinx.serialization.modules.SerializersModule
                import kotlinx.serialization.modules.polymorphic
                import kotlinx.serialization.modules.subclass

                @Stable
                @Serializable
                sealed class AppScreens : NavKey {
                    $subclasses

                    companion object {
                        val startDestination: AppScreens get() = $startRoute

                        val savedStateConfig = SavedStateConfiguration {
                            serializersModule = SerializersModule {
                                polymorphic(NavKey::class) {
                                        $subclassEntries
                                }
                            }
                        }
                    }
                }
            """.trimIndent())
        }
    }

    private fun generateScreenFactory(entries: List<Pair<String, String>>) {
        val imports = entries.joinToString("\n") { (_, fqName) -> "import $fqName" }
        val whenBranches = entries.joinToString("\n            ") { (route, fqName) ->
            "is AppScreens.$route -> ${fqName.substringAfterLast('.')}()"
        }

        val file = codeGenerator.createNewFile(
            Dependencies(false),
            "io.github.alimsrepo.navease.generated",
            "ScreenFactory"
        )
        file.bufferedWriter().use {
            it.write("""
                package io.github.alimsrepo.navease.generated

                import androidx.navigation3.runtime.NavKey
                import io.github.alimsrepo.navease.runtime.domain.NavScreen
                $imports

                object ScreenFactory {
                    fun createScreen(appScreen: NavKey): NavScreen<*> {
                        return when (appScreen) {
                            $whenBranches
                            else -> error("Unknown screen: ${'$'}appScreen")
                        }
                    }
                }
            """.trimIndent())
        }
    }

    private fun generateNavEaseHost() {
        val file = codeGenerator.createNewFile(
            Dependencies(false),
            "io.github.alimsrepo.navease.generated",
            "NavEaseHost"
        )
        file.bufferedWriter().use {
            it.write("""
                package io.github.alimsrepo.navease.generated

                import androidx.compose.runtime.Composable
                import io.github.alimsrepo.navease.runtime.presentation.AppNavGraph

                @Composable
                fun NavEaseHost() {
                    AppNavGraph(
                        initialScreen = AppScreens.startDestination,
                        savedStateConfig = AppScreens.savedStateConfig,
                        screenFactory = ScreenFactory::createScreen
                    )
                }
            """.trimIndent())
        }
    }
}