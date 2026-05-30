package io.github.alimsrepo.navease.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType

/**
 * NavEase KSP symbol processor.
 *
 * Discovers all classes annotated with
 * `io.github.alimsrepo.navease.runtime.annotations.NavEaseScreen` and generates five files:
 * - `AppScreens.kt` — sealed NavKey hierarchy
 * - `ScreenFactory.kt` — key-to-NavScreen mapping
 * - `NavEaseExtensions.kt` — typed `navigateToXxx()` / `xxxArgs()` extensions
 * - `NavEaseResults.kt` — typed result data classes + extensions (only if any `@NavEaseResult` exists)
 * - `NavEaseHost.kt` — generated `@Composable NavEaseHost(…)` entry point
 *
 * All files are emitted into [generatedPackage].
 *
 * @param codeGenerator    KSP code generator, provided by the KSP runtime.
 * @param logger           KSP logger used to report errors and warnings during processing.
 * @param generatedPackage Package for all generated files.
 *                         Configured via the `navease.generatedPackage` KSP option;
 *                         defaults to `io.github.alimsrepo.navease.generated`.
 */
class NavEaseProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val generatedPackage: String = "io.github.alimsrepo.navease.generated",
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

    /**
     * A single constructor parameter with its resolved type.
     *
     * [ksType] is kept alongside [typeInfo] so that [importsFrom] can call [collectImports]
     * to gather FQNs from nested generic type arguments (e.g. the `SampleData` inside
     * `List<SampleData>` would not appear in [TypeInfo.importFqn] of the outer `List`).
     */
    private data class ArgParam(val name: String, val typeInfo: TypeInfo, val ksType: KSType)

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

        // Correct FQN matches the annotations sub-package
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
                val resolved = param.type.resolve()
                ArgParam(param.name!!.asString(), resolveTypeInfo(resolved), resolved)
            }

            val resultClass = cls.declarations
                .filterIsInstance<KSClassDeclaration>()
                .firstOrNull { nested ->
                    nested.annotations.any { it.shortName.asString() == "NavEaseResult" }
                }

            val resultFields = resultClass?.primaryConstructor?.parameters?.map { param ->
                val resolved = param.type.resolve()
                ArgParam(param.name!!.asString(), resolveTypeInfo(resolved), resolved)
            }

            ScreenEntry(route, cls.qualifiedName!!.asString(), isStart, args, resultFields)
        }

        // ── Validate: duplicate route names ────────────────────────────────────
        val routeCounts = entries.groupingBy { it.route }.eachCount()
        routeCounts.filter { it.value > 1 }.forEach { (route, count) ->
            logger.error(
                "NavEase: route \"$route\" is declared $count times. " +
                "Each @NavEaseScreen must have a unique route value."
            )
        }
        if (routeCounts.any { it.value > 1 }) return emptyList()

        // ── Validate: multiple startDestination ────────────────────────────────
        val startEntries = entries.filter { it.isStart }
        if (startEntries.size > 1) {
            logger.warn(
                "NavEase: ${startEntries.size} screens are marked with startDestination = true " +
                "(${startEntries.joinToString { "\"${it.route}\"" }}). " +
                "Only the first one (\"${startEntries.first().route}\") will be used."
            )
        }

        val startEntry = entries.firstOrNull { it.isStart } ?: entries.first()

        generateAppScreens(entries, startEntry.route, deps)
        generateScreenFactory(entries, deps)
        generateNavEaseResults(entries, deps)
        generateNavEaseExtensions(entries, deps)
        generateNavEaseHost(deps)

        return emptyList()
    }

    /**
     * Kotlin built-in types whose containers need no import in generated source.
     *
     * Generic containers like `kotlin.collections.List` are mapped to their short alias
     * (`List`) without an explicit import because they are always in scope on all KMP targets.
     */
    private val builtInContainerFqns = setOf(
        "kotlin.collections.List",
        "kotlin.collections.MutableList",
        "kotlin.collections.Set",
        "kotlin.collections.MutableSet",
        "kotlin.collections.Map",
        "kotlin.collections.MutableMap",
        "kotlin.Array",
        "kotlin.Pair",
        "kotlin.Triple",
    )

    /**
     * Resolves a KSP type into a [TypeInfo] holding:
     * - the short (unqualified) name safe to use directly in generated source,
     *   including type arguments for generics (e.g. `List<String>`, `Map<String, SampleData?>`)
     * - the FQN to emit as an `import`, or null for built-in / well-known types.
     *   For generics, imports are collected from all type arguments recursively.
     *
     * Imports for type arguments are collected via [collectImports] and surfaced through the
     * [ArgParam] → [importsFrom] pipeline.
     */
    private fun resolveTypeInfo(type: KSType): TypeInfo {
        val fqn = type.declaration.qualifiedName?.asString() ?: "kotlin.String"
        val nullable = if (type.isMarkedNullable) "?" else ""

        // Primitive Kotlin types — never need an import
        val primitiveShort = when (fqn) {
            "kotlin.String"  -> "String"
            "kotlin.Int"     -> "Int"
            "kotlin.Long"    -> "Long"
            "kotlin.Boolean" -> "Boolean"
            "kotlin.Double"  -> "Double"
            "kotlin.Float"   -> "Float"
            "kotlin.Byte"    -> "Byte"
            "kotlin.Short"   -> "Short"
            "kotlin.Char"    -> "Char"
            "kotlin.Unit"    -> "Unit"
            "kotlin.Any"     -> "Any"
            else             -> null
        }
        if (primitiveShort != null) return TypeInfo("$primitiveShort$nullable", null)

        val typeArgs = type.arguments
        val shortBase = fqn.substringAfterLast('.')

        if (typeArgs.isEmpty()) {
            // Non-generic custom type — needs an import
            return TypeInfo("$shortBase$nullable", fqn)
        }

        // Generic type: build "Container<Arg1, Arg2, ...>" recursively.
        // Each type argument may itself be a generic, nullable, or a star projection (*).
        val argStrings = typeArgs.map { arg ->
            val variance = arg.variance
            val argType = arg.type?.resolve()
            when {
                argType == null -> "*" // star projection
                variance.label.isNotEmpty() -> "${variance.label} ${resolveTypeInfo(argType).shortName}"
                else -> resolveTypeInfo(argType).shortName
            }
        }
        val shortName = "$shortBase<${argStrings.joinToString(", ")}>$nullable"

        // Container needs an import only if it is not a well-known Kotlin built-in alias
        val containerImport = if (fqn in builtInContainerFqns) null else fqn

        return TypeInfo(shortName, containerImport)
    }

    /**
     * Recursively collects every import FQN needed to represent [type] and all its type
     * arguments. This is required because [TypeInfo.importFqn] only stores the outermost
     * container's FQN; inner argument FQNs are gathered here.
     */
    private fun collectImports(type: KSType): Set<String> {
        val fqn = type.declaration.qualifiedName?.asString() ?: return emptySet()
        val result = mutableSetOf<String>()

        // Add the container import if it is not a built-in primitive or well-known collection
        val primitives = setOf(
            "kotlin.String", "kotlin.Int", "kotlin.Long", "kotlin.Boolean",
            "kotlin.Double", "kotlin.Float", "kotlin.Byte", "kotlin.Short",
            "kotlin.Char", "kotlin.Unit", "kotlin.Any",
        )
        if (fqn !in primitives && fqn !in builtInContainerFqns) {
            result += fqn
        }

        // Recurse into type arguments
        type.arguments.forEach { arg ->
            arg.type?.resolve()?.let { argType -> result += collectImports(argType) }
        }

        return result
    }

    /**
     * Collects all non-null import FQNs from a list of [ArgParam]s **and** their nested
     * generic type arguments, sorted for determinism.
     */
    private fun importsFrom(params: List<ArgParam>): String =
        params.flatMap { collectImports(it.ksType) }
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

        val file = codeGenerator.createNewFile(deps, generatedPackage, "AppScreens")
        file.bufferedWriter().use {
            it.write("""
                package $generatedPackage

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

        val file = codeGenerator.createNewFile(deps, generatedPackage, "ScreenFactory")
        file.bufferedWriter().use {
            it.write("""
                package $generatedPackage

                import androidx.navigation3.runtime.NavKey
                import io.github.alimsrepo.navease.runtime.domain.NavScreen
                $imports

                object ScreenFactory {
                    fun createScreen(appScreen: NavKey): NavScreen {
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

        val file = codeGenerator.createNewFile(deps, generatedPackage, "NavEaseResults")
        file.bufferedWriter().use {
            it.write("""
                package $generatedPackage

                import androidx.compose.runtime.Composable
                import androidx.compose.runtime.State
                import io.github.alimsrepo.navease.runtime.navigation.NavController
                import io.github.alimsrepo.navease.runtime.navigation.backWithResult
                import io.github.alimsrepo.navease.runtime.navigation.resultOf
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

    private fun generateNavEaseExtensions(entries: List<ScreenEntry>, deps: Dependencies) {
        // navigateToXxx() — one extension per screen on NavController
        val navExtensions = entries.joinToString("\n\n") { entry ->
            val fnName = "navigateTo${entry.route}"
            val paramList = if (entry.args.isNullOrEmpty()) {
                "finish: Boolean = false,\n    navTransition: NavTransition? = null"
            } else {
                entry.args.joinToString(", ") { (name, t) -> "$name: ${t.shortName}" } +
                    ", finish: Boolean = false,\n    navTransition: NavTransition? = null"
            }
            val keyConstruct = if (entry.args.isNullOrEmpty()) {
                "AppScreens.${entry.route}"
            } else {
                val argNames = entry.args.joinToString(", ") { (name, _) -> "$name = $name" }
                "AppScreens.${entry.route}($argNames)"
            }
            """fun NavController.$fnName($paramList) {
    navigate($keyConstruct, finish = finish, navTransition = navTransition)
}"""
        }

        // xxxArgs() — one extension per screen that has @NavEaseArgs
        val argsExtensions = entries.filter { !it.args.isNullOrEmpty() }.joinToString("\n\n") { entry ->
            val simpleName = entry.fqName.substringAfterLast('.')
            val fnName = "${entry.route.replaceFirstChar { it.lowercaseChar() }}Args"
            val argAssignments = entry.args!!.joinToString(", ") { (name, _) -> "$name = key.$name" }
            """fun NavKey.${fnName}(): $simpleName.Args {
    val key = this as AppScreens.${entry.route}
    return $simpleName.Args($argAssignments)
}"""
        }

        // Collect custom type imports from args across all screens
        val customImports = importsFrom(entries.flatMap { it.args.orEmpty() })
        // Collect screen class imports
        val screenImports = entries
            .filter { !it.args.isNullOrEmpty() }
            .joinToString("\n") { "import ${it.fqName}" }

        val body = buildString {
            append("// ── navigateToXxx() extensions on NavController ─────────────────────────\n\n")
            append(navExtensions)
            if (argsExtensions.isNotEmpty()) {
                append("\n\n// ── xxxArgs() extensions on NavKey ──────────────────────────────────────\n\n")
                append(argsExtensions)
            }
        }

        val file = codeGenerator.createNewFile(deps, generatedPackage, "NavEaseExtensions")
        file.bufferedWriter().use {
            it.write("""
                package $generatedPackage

                import androidx.navigation3.runtime.NavKey
                import io.github.alimsrepo.navease.runtime.navigation.NavController
                import io.github.alimsrepo.navease.runtime.presentation.NavTransition
                $screenImports
                $customImports

                $body
            """.trimIndent())
        }
    }

    private fun generateNavEaseHost(deps: Dependencies) {
        val file = codeGenerator.createNewFile(deps, generatedPackage, "NavEaseHost")
        file.bufferedWriter().use {
            it.write("""
                package $generatedPackage

                import androidx.compose.runtime.Composable
                import io.github.alimsrepo.navease.runtime.presentation.NavEaseNavGraph
                import io.github.alimsrepo.navease.runtime.presentation.NavTransition
                /**
                 * Generated navigation host. Place this once in your root composable.
                 *
                 * @param onExitRequest         Called when back is pressed on the root screen.
                 *                              Use this to show an exit dialog or finish the Activity.
                 *                              Defaults to a no-op (suitable for iOS / web targets).
                 * @param enableSharedTransitions When `true`, wraps the display in a
                 *                              [SharedTransitionLayout] enabling Compose shared element
                 *                              transitions between screens. Access the scope inside any
                 *                              screen via [LocalNavEaseSharedTransitionScope.current] and
                 *                              the animation scope via [LocalNavAnimatedContentScope.current].
                 *                              Defaults to `false`.
                 * @param navTransition         The screen-to-screen animation style.
                 *                              Defaults to [NavTransition.Push] (iOS-style horizontal slide).
                 *                              See [NavTransition] for all available options:
                 *                              [NavTransition.Push], [NavTransition.Fade], [NavTransition.Rise],
                 *                              [NavTransition.Zoom], [NavTransition.Depth], [NavTransition.Instant].
                 */
                @Composable
                fun NavEaseHost(
                    onExitRequest: () -> Unit = {},
                    enableSharedTransitions: Boolean = false,
                    navTransition: NavTransition = NavTransition.Push,
                ) {
                    NavEaseNavGraph(
                        initialScreen = AppScreens.startDestination,
                        savedStateConfig = AppScreens.savedStateConfig,
                        screenFactory = ScreenFactory::createScreen,
                        onExitRequest = onExitRequest,
                        enableSharedTransitions = enableSharedTransitions,
                        navTransition = navTransition,
                    )
                }
            """.trimIndent())
        }
    }
}