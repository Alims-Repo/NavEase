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

    data class ScreenEntry(
        val route: String,
        val fqName: String,
        val isStart: Boolean,
        /** null = data object, non-null = data class with these (name, type) params */
        val args: List<Pair<String, String>>?,
        /** null = no result, non-null = result fields (name, type) */
        val result: List<Pair<String, String>>?
    )

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (generated) return emptyList()

        val symbols = resolver.getSymbolsWithAnnotation("io.github.alimsrepo.navease.runtime.NavEaseScreen")
            .filterIsInstance<KSClassDeclaration>()
            .toList()

        if (symbols.isEmpty()) return emptyList()

        generated = true

        val entries = symbols.map { cls ->
            val annotation = cls.annotations.first { it.shortName.asString() == "NavEaseScreen" }
            val route = annotation.arguments.first { it.name?.asString() == "route" }.value as String
            val isStart = annotation.arguments.firstOrNull { it.name?.asString() == "startDestination" }?.value as? Boolean ?: false

            // Look for a nested class annotated with @NavEaseArgs
            val argsClass = cls.declarations
                .filterIsInstance<KSClassDeclaration>()
                .firstOrNull { nested ->
                    nested.annotations.any { it.shortName.asString() == "NavEaseArgs" }
                }

            val args = argsClass?.primaryConstructor?.parameters?.map { param ->
                param.name!!.asString() to resolveTypeName(param.type.resolve())
            }

            // Look for a nested class annotated with @NavEaseResult
            val resultClass = cls.declarations
                .filterIsInstance<KSClassDeclaration>()
                .firstOrNull { nested ->
                    nested.annotations.any { it.shortName.asString() == "NavEaseResult" }
                }

            val resultFields = resultClass?.primaryConstructor?.parameters?.map { param ->
                param.name!!.asString() to resolveTypeName(param.type.resolve())
            }

            ScreenEntry(route, cls.qualifiedName!!.asString(), isStart, args, resultFields)
        }

        val startEntry = entries.firstOrNull { it.isStart } ?: entries.first()

        generateAppScreens(entries, startEntry.route)
        generateScreenFactory(entries)
        generateNavEaseResults(entries)
        generateNavEaseHost()

        return emptyList()
    }

    private fun resolveTypeName(type: com.google.devtools.ksp.symbol.KSType): String {
        val typeName = type.declaration.qualifiedName?.asString() ?: "String"
        val nullable = if (type.isMarkedNullable) "?" else ""
        val shortName = when (typeName) {
            "kotlin.String" -> "String"
            "kotlin.Int" -> "Int"
            "kotlin.Long" -> "Long"
            "kotlin.Boolean" -> "Boolean"
            "kotlin.Double" -> "Double"
            "kotlin.Float" -> "Float"
            else -> typeName
        }
        return "$shortName$nullable"
    }

    private fun generateAppScreens(entries: List<ScreenEntry>, startRoute: String) {
        val subclasses = entries.joinToString("\n    ") { entry ->
            if (entry.args == null) {
                // No args → data object
                "@Serializable data object ${entry.route} : AppScreens()"
            } else {
                // Has args → data class
                val params = entry.args.joinToString(", ") { (name, type) -> "val $name: $type" }
                "@Serializable data class ${entry.route}($params) : AppScreens()"
            }
        }

        val subclassEntries = entries.joinToString("\n                        ") { entry ->
            "subclass(${entry.route}::class)"
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

    private fun generateScreenFactory(entries: List<ScreenEntry>) {
        val imports = entries.joinToString("\n") { "import ${it.fqName}" }
        val whenBranches = entries.joinToString("\n            ") { entry ->
            val simpleName = entry.fqName.substringAfterLast('.')
            "is AppScreens.${entry.route} -> $simpleName()"
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

    private fun generateNavEaseResults(entries: List<ScreenEntry>) {
        val withResults = entries.filter { it.result != null }
        if (withResults.isEmpty()) return

        val resultClasses = withResults.joinToString("\n\n") { entry ->
            val params = entry.result!!.joinToString(", ") { (name, type) -> "val $name: $type" }
            "data class ${entry.route}Result($params)"
        }

        // backWithXxxResult(...) functions
        val backFunctions = withResults.joinToString("\n\n") { entry ->
            val params = entry.result!!.joinToString(", ") { (name, type) -> "$name: $type" }
            val args = entry.result.joinToString(", ") { (name, _) -> name }
            """fun NavController.backWith${entry.route}Result($params) {
    backWithResult(${entry.route}Result($args))
}"""
        }

        // @Composable xxxResult(): State<XxxResult?> functions
        val resultFunctions = withResults.joinToString("\n\n") { entry ->
            val fnName = entry.route.replaceFirstChar { it.lowercaseChar() }
            """@Composable
fun NavController.${fnName}Result(): State<${entry.route}Result?> =
    resultOf(${entry.route}Result::class)"""
        }

        val file = codeGenerator.createNewFile(
            Dependencies(false),
            "io.github.alimsrepo.navease.generated",
            "NavEaseResults"
        )
        file.bufferedWriter().use {
            it.write("""
                package io.github.alimsrepo.navease.generated

                import androidx.compose.runtime.Composable
                import androidx.compose.runtime.State
                import io.github.alimsrepo.navease.runtime.data.NavController
                import io.github.alimsrepo.navease.runtime.data.backWithResult
                import io.github.alimsrepo.navease.runtime.data.resultOf

                // ── Result data classes ──────────────────────────────────────────────────

                $resultClasses

                // ── backWithXxxResult() extensions ───────────────────────────────────────

                $backFunctions

                // ── xxxResult() Composable extensions ────────────────────────────────────

                $resultFunctions
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