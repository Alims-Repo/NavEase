package io.github.alimsrepo.navease.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType

class NavEaseProcessor(
    private val codeGenerator: CodeGenerator
) : SymbolProcessor {

    private var generated = false

    /**
     * Resolved type information for a single constructor parameter.
     *
     * @property shortName  Unqualified name used in generated source (e.g. `"SampleData?"`).
     * @property importFqn  Fully-qualified name to emit as an `import`, or `null` for
     *                      built-in Kotlin types that need no import.
     */
    private data class TypeInfo(val shortName: String, val importFqn: String?)

    /** A single constructor parameter with its resolved type. */
    private data class ArgParam(val name: String, val typeInfo: TypeInfo)

    private data class ScreenEntry(
        val route: String,
        val fqName: String,
        val isStart: Boolean,
        /** null = data object (no args), non-null = data class with these params */
        val args: List<ArgParam>?,
        /** null = no result, non-null = result fields */
        val result: List<ArgParam>?
    )

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (generated) return emptyList()

        // Correct FQN: io.github.alimsrepo.navease.runtime.NavEaseScreen
        val symbols = resolver
            .getSymbolsWithAnnotation("io.github.alimsrepo.navease.runtime.annotations.NavEaseScreen")
            .filterIsInstance<KSClassDeclaration>()
            .toList()

        if (symbols.isEmpty()) return emptyList()

        generated = true

        // Collect all source files that contribute @NavEaseScreen classes so KSP can
        // correctly track which outputs depend on which inputs for incremental builds.
        val allSourceFiles = symbols.mapNotNull { it.containingFile }.toSet()
        val deps = Dependencies(aggregating = true, *allSourceFiles.toTypedArray())

        val entries = symbols.map { cls ->
            val annotation = cls.annotations.first { it.shortName.asString() == "NavEaseScreen" }
            val route = annotation.arguments.first { it.name?.asString() == "route" }.value as String
            val isStart = annotation.arguments
                .firstOrNull { it.name?.asString() == "startDestination" }
                ?.value as? Boolean ?: false

            val argsClass = cls.declarations
                .filterIsInstance<KSClassDeclaration>()
                .firstOrNull { nested ->
                    nested.annotations.any { it.shortName.asString() == "NavEaseArgs" }
                }

            val args = argsClass?.primaryConstructor?.parameters?.map { param ->
                ArgParam(param.name!!.asString(), resolveTypeInfo(param.type.resolve()))
            }

            val resultClass = cls.declarations
                .filterIsInstance<KSClassDeclaration>()
                .firstOrNull { nested ->
                    nested.annotations.any { it.shortName.asString() == "NavEaseResult" }
                }

            val resultFields = resultClass?.primaryConstructor?.parameters?.map { param ->
                ArgParam(param.name!!.asString(), resolveTypeInfo(param.type.resolve()))
            }

            ScreenEntry(route, cls.qualifiedName!!.asString(), isStart, args, resultFields)
        }

        val startEntry = entries.firstOrNull { it.isStart } ?: entries.first()

        generateAppScreens(entries, startEntry.route, deps)
        generateScreenFactory(entries, deps)
        generateNavEaseResults(entries, deps)
        generateNavEaseHost(deps)

        return emptyList()
    }

    /**
     * Resolves a KSP type into a [TypeInfo] holding:
     * - the short (unqualified) name safe to use directly in generated source
     * - the FQN to emit as an `import`, or null for built-in Kotlin types
     */
    private fun resolveTypeInfo(type: KSType): TypeInfo {
        val fqn = type.declaration.qualifiedName?.asString() ?: "kotlin.String"
        val nullable = if (type.isMarkedNullable) "?" else ""
        return when (fqn) {
            "kotlin.String"  -> TypeInfo("String$nullable",  null)
            "kotlin.Int"     -> TypeInfo("Int$nullable",     null)
            "kotlin.Long"    -> TypeInfo("Long$nullable",    null)
            "kotlin.Boolean" -> TypeInfo("Boolean$nullable", null)
            "kotlin.Double"  -> TypeInfo("Double$nullable",  null)
            "kotlin.Float"   -> TypeInfo("Float$nullable",   null)
            else             -> TypeInfo(
                shortName = "${fqn.substringAfterLast('.')}$nullable",
                importFqn = fqn
            )
        }
    }

    /** Collects all non-null import FQNs from a list of [ArgParam]s, sorted for determinism. */
    private fun importsFrom(params: List<ArgParam>): String =
        params.mapNotNull { it.typeInfo.importFqn }
            .toSortedSet()
            .joinToString("\n") { "import $it" }

    private fun generateAppScreens(entries: List<ScreenEntry>, startRoute: String, deps: Dependencies) {
        // Gather any custom-type imports needed for @NavEaseArgs parameters
        val customImports = importsFrom(entries.flatMap { it.args.orEmpty() })

        val subclasses = entries.joinToString("\n    ") { entry ->
            if (entry.args == null) {
                "@Serializable data object ${entry.route} : AppScreens()"
            } else {
                val params = entry.args.joinToString(", ") { (name, t) -> "val $name: ${t.shortName}" }
                "@Serializable data class ${entry.route}($params) : AppScreens()"
            }
        }

        val subclassEntries = entries.joinToString("\n                        ") { entry ->
            "subclass(${entry.route}::class)"
        }

        val file = codeGenerator.createNewFile(deps, "io.github.alimsrepo.navease.generated", "AppScreens")
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
                $customImports

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

    private fun generateScreenFactory(entries: List<ScreenEntry>, deps: Dependencies) {
        val imports = entries.joinToString("\n") { "import ${it.fqName}" }
        val whenBranches = entries.joinToString("\n            ") { entry ->
            val simpleName = entry.fqName.substringAfterLast('.')
            "is AppScreens.${entry.route} -> $simpleName()"
        }

        val file = codeGenerator.createNewFile(deps, "io.github.alimsrepo.navease.generated", "ScreenFactory")
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
                            else -> error(
                                "NavEase: No screen registered for key type '${'$'}{appScreen::class.simpleName}'. " +
                                "Did you forget to annotate the corresponding class with @NavEaseScreen? " +
                                "If you just added a new screen, try rebuilding the project."
                            )
                        }
                    }
                }
            """.trimIndent())
        }
    }

    private fun generateNavEaseResults(entries: List<ScreenEntry>, deps: Dependencies) {
        val withResults = entries.filter { it.result != null }
        if (withResults.isEmpty()) return

        // Gather any custom-type imports needed for @NavEaseResult parameters
        val customImports = importsFrom(withResults.flatMap { it.result.orEmpty() })

        val resultClasses = withResults.joinToString("\n\n") { entry ->
            val params = entry.result!!.joinToString(", ") { (name, t) -> "val $name: ${t.shortName}" }
            "data class ${entry.route}Result($params)"
        }

        val backFunctions = withResults.joinToString("\n\n") { entry ->
            val params = entry.result!!.joinToString(", ") { (name, t) -> "$name: ${t.shortName}" }
            val args = entry.result.joinToString(", ") { (name, _) -> name }
            """fun NavController.backWith${entry.route}Result($params) {
    backWithResult(${entry.route}Result($args))
}"""
        }

        val resultFunctions = withResults.joinToString("\n\n") { entry ->
            val fnName = entry.route.replaceFirstChar { it.lowercaseChar() }
            """@Composable
fun NavController.${fnName}Result(): State<${entry.route}Result?> =
    resultOf(${entry.route}Result::class)"""
        }

        val file = codeGenerator.createNewFile(deps, "io.github.alimsrepo.navease.generated", "NavEaseResults")
        file.bufferedWriter().use {
            it.write("""
                package io.github.alimsrepo.navease.generated

                import androidx.compose.runtime.Composable
                import androidx.compose.runtime.State
                import io.github.alimsrepo.navease.runtime.data.NavController
                import io.github.alimsrepo.navease.runtime.data.backWithResult
                import io.github.alimsrepo.navease.runtime.data.resultOf
                $customImports

                // ── Result data classes ──────────────────────────────────────────────────

                $resultClasses

                // ── backWithXxxResult() extensions ───────────────────────────────────────

                $backFunctions

                // ── xxxResult() Composable extensions ────────────────────────────────────

                $resultFunctions
            """.trimIndent())
        }
    }

    private fun generateNavEaseHost(deps: Dependencies) {
        val file = codeGenerator.createNewFile(deps, "io.github.alimsrepo.navease.generated", "NavEaseHost")
        file.bufferedWriter().use {
            it.write("""
                package io.github.alimsrepo.navease.generated

                import androidx.compose.runtime.Composable
                import io.github.alimsrepo.navease.runtime.presentation.AppNavGraph

                /**
                 * Generated navigation host. Place this once in your root composable.
                 *
                 * @param onExitRequest Called when the user presses back on the root screen.
                 *                      Use this to show an exit dialog or finish the Activity.
                 *                      Defaults to a no-op (suitable for iOS / web targets).
                 */
                @Composable
                fun NavEaseHost(onExitRequest: () -> Unit = {}) {
                    AppNavGraph(
                        initialScreen = AppScreens.startDestination,
                        savedStateConfig = AppScreens.savedStateConfig,
                        screenFactory = ScreenFactory::createScreen,
                        onExitRequest = onExitRequest
                    )
                }
            """.trimIndent())
        }
    }
}