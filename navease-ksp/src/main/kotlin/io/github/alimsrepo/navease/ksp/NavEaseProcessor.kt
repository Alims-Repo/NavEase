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

//    private var generated = false

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

    // Helper: convert arbitrary route strings into safe Kotlin identifiers used in generated code.
    // We keep the original `route` string for user-facing messages, but always emit sanitized
    // identifiers where Kotlin identifiers are required (class names, function names, etc.).
    private fun safeClassIdent(route: String): String {
        // Replace invalid characters with underscore
        var s = route.replace(Regex("[^A-Za-z0-9_]"), "_")
        if (s.isEmpty()) s = "Screen"
        // If starts with digit, prefix underscore
        if (s[0].isDigit()) s = "_${s}"
        // Ensure first char is uppercase for class/data/object names
        s = s.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        return s
    }

    private fun safeFunIdent(route: String): String {
        val c = safeClassIdent(route)
        return c.replaceFirstChar { it.lowercaseChar() }
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
//        if (generated) return emptyList()

        // Correct FQN matches the annotations sub-package
        val symbols = resolver
            .getSymbolsWithAnnotation("io.github.alimsrepo.navease.runtime.annotations.NavEaseScreen")
            .filterIsInstance<KSClassDeclaration>()
            .toList()

        val autoRegisterSymbols = resolver
            .getSymbolsWithAnnotation("io.github.alimsrepo.navease.runtime.annotations.AutoRegister")
            .filterIsInstance<KSClassDeclaration>()
            .toList()

        if (symbols.isEmpty() && autoRegisterSymbols.isEmpty()) return emptyList()

//        generated = true

        // Collect all source files that contribute @NavEaseScreen classes so KSP can
        // correctly track which outputs depend on which inputs for incremental builds.
        val allSourceFiles = (symbols + autoRegisterSymbols).mapNotNull { it.containingFile }.toSet()
        val deps = Dependencies(aggregating = true, *allSourceFiles.toTypedArray())

        val entries = symbols.map { cls ->
            val annotation = cls.annotations.first { it.shortName.asString() == "NavEaseScreen" }
            val route =
                annotation.arguments.first { it.name?.asString() == "route" }.value as String
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
        if (entries.isNotEmpty()) {
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
        } // end entries.isNotEmpty()

        val startEntry = entries.firstOrNull { it.isStart } ?: entries.firstOrNull()

        if (entries.isNotEmpty() && startEntry != null) {
            generateAppScreens(entries, startEntry.route, deps)
            generateScreenFactory(entries, deps)
            generateNavEaseResults(entries, deps)
            generateNavEaseExtensions(entries, deps)
            generateNavEaseHost(deps)
        }

        if (autoRegisterSymbols.isNotEmpty()) {
            generateAutoRegisterExtension(autoRegisterSymbols, deps)
        }

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
            "kotlin.String" -> "String"
            "kotlin.Int" -> "Int"
            "kotlin.Long" -> "Long"
            "kotlin.Boolean" -> "Boolean"
            "kotlin.Double" -> "Double"
            "kotlin.Float" -> "Float"
            "kotlin.Byte" -> "Byte"
            "kotlin.Short" -> "Short"
            "kotlin.Char" -> "Char"
            "kotlin.Unit" -> "Unit"
            "kotlin.Any" -> "Any"
            else -> null
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

    private fun generateAppScreens(
        entries: List<ScreenEntry>,
        startRoute: String,
        deps: Dependencies
    ) {
        val customImports = importsFrom(entries.flatMap { it.args.orEmpty() })

        // Subclass declarations: each line already carries its 4-space indent so they can
        // be embedded verbatim inside the sealed class body.
        val subclasses = entries.joinToString("\n") { entry ->
            val cls = safeClassIdent(entry.route)
            if (entry.args == null) {
                "    @Serializable data object $cls : AppScreens()"
            } else {
                val params =
                    entry.args.joinToString(", ") { (name, t) -> "val $name: ${t.shortName}" }
                "    @Serializable data class $cls($params) : AppScreens()"
            }
        }

        // subclass() calls inside the polymorphic block (20-space indent)
        val subclassEntries = entries.joinToString("\n") { entry ->
            val cls = safeClassIdent(entry.route)
            "                    subclass($cls::class)"
        }

        val content = buildString {
            appendLine("package $generatedPackage")
            appendLine()
            appendLine("import androidx.compose.runtime.Stable")
            appendLine("import androidx.navigation3.runtime.NavKey")
            appendLine("import androidx.savedstate.serialization.SavedStateConfiguration")
            appendLine("import kotlinx.serialization.Serializable")
            appendLine("import kotlinx.serialization.modules.SerializersModule")
            appendLine("import kotlinx.serialization.modules.polymorphic")
            appendLine("import kotlinx.serialization.modules.subclass")
            if (customImports.isNotEmpty()) appendLine(customImports)
            appendLine()
            appendLine("@Stable")
            appendLine("@Serializable")
            appendLine("sealed class AppScreens : NavKey {")
            appendLine(subclasses)
            appendLine()
            appendLine("    companion object {")
            appendLine("        val startDestination: AppScreens get() = $startRoute")
            appendLine()
            appendLine("        val savedStateConfig = SavedStateConfiguration {")
            appendLine("            serializersModule = SerializersModule {")
            appendLine("                polymorphic(NavKey::class) {")
            appendLine(subclassEntries)
            appendLine("                }")
            appendLine("            }")
            appendLine("        }")
            appendLine("    }")
            append("}")
        }

        val file = codeGenerator.createNewFile(deps, generatedPackage, "AppScreens")
        file.bufferedWriter().use { it.write(content) }
    }

    private fun generateScreenFactory(entries: List<ScreenEntry>, deps: Dependencies) {
        val imports = entries.joinToString("\n") { "import ${it.fqName}" }
        val whenBranches = entries.joinToString("\n") { entry ->
            val simpleName = entry.fqName.substringAfterLast('.')
            val cls = safeClassIdent(entry.route)
            "            is AppScreens.$cls -> $simpleName()"
        }

        val content = buildString {
            appendLine("package $generatedPackage")
            appendLine()
            appendLine("import androidx.navigation3.runtime.NavKey")
            appendLine("import io.github.alimsrepo.navease.runtime.domain.NavScreen")
            if (imports.isNotEmpty()) appendLine(imports)
            appendLine()
            appendLine("object ScreenFactory {")
            appendLine("    fun createScreen(appScreen: NavKey): NavScreen {")
            appendLine("        return when (appScreen) {")
            appendLine(whenBranches)
            appendLine("            else -> error(")
            appendLine("                \"NavEase: No screen registered for key type '\${appScreen::class.simpleName}'. \" +")
            appendLine("                \"Did you forget to annotate the corresponding class with @NavEaseScreen? \" +")
            appendLine("                \"If you just added a new screen, try rebuilding the project.\"")
            appendLine("            )")
            appendLine("        }")
            appendLine("    }")
            append("}")
        }

        val file = codeGenerator.createNewFile(deps, generatedPackage, "ScreenFactory")
        file.bufferedWriter().use { it.write(content) }
    }

    private fun generateNavEaseResults(entries: List<ScreenEntry>, deps: Dependencies) {
        val withResults = entries.filter { it.result != null }
        if (withResults.isEmpty()) return

        val customImports = importsFrom(withResults.flatMap { it.result.orEmpty() })

        val resultClasses = withResults.joinToString("\n\n") { entry ->
            val cls = safeClassIdent(entry.route)
            val params =
                entry.result!!.joinToString(", ") { (name, t) -> "val $name: ${t.shortName}" }
            "data class ${cls}Result($params)"
        }

        val backFunctions = withResults.joinToString("\n\n") { entry ->
            val cls = safeClassIdent(entry.route)
            val params = entry.result!!.joinToString(", ") { (name, t) -> "$name: ${t.shortName}" }
            val args = entry.result.joinToString(", ") { (name, _) -> name }
            "fun NavController.backWith${cls}Result($params) {\n    backWithResult(${cls}Result($args))\n}"
        }

        val resultFunctions = withResults.joinToString("\n\n") { entry ->
            val fnName = safeFunIdent(entry.route)
            val cls = safeClassIdent(entry.route)
            "@Composable\nfun NavController.${fnName}Result(): State<${cls}Result?> =\n    resultOf(${cls}Result::class)"
        }

        val content = buildString {
            appendLine("package $generatedPackage")
            appendLine()
            appendLine("import androidx.compose.runtime.Composable")
            appendLine("import androidx.compose.runtime.State")
            appendLine("import io.github.alimsrepo.navease.runtime.navigation.NavController")
            appendLine("import io.github.alimsrepo.navease.runtime.navigation.backWithResult")
            appendLine("import io.github.alimsrepo.navease.runtime.navigation.resultOf")
            if (customImports.isNotEmpty()) appendLine(customImports)
            appendLine()
            appendLine("// ── Result data classes ──────────────────────────────────────────────────")
            appendLine()
            appendLine(resultClasses)
            appendLine()
            appendLine("// ── backWithXxxResult() extensions ───────────────────────────────────────")
            appendLine()
            appendLine(backFunctions)
            appendLine()
            appendLine("// ── xxxResult() Composable extensions ────────────────────────────────────")
            appendLine()
            append(resultFunctions)
        }

        val file = codeGenerator.createNewFile(deps, generatedPackage, "NavEaseResults")
        file.bufferedWriter().use { it.write(content) }
    }

    private fun generateNavEaseExtensions(entries: List<ScreenEntry>, deps: Dependencies) {
        // navigateToXxx() — one top-level extension per screen on NavController
        val navExtensions = entries.joinToString("\n\n") { entry ->
            val cls = safeClassIdent(entry.route)
            val fnName = "navigateTo${cls}"
            val paramList = if (entry.args.isNullOrEmpty()) {
                "finish: Boolean = false,\n    navTransition: NavTransition? = null"
            } else {
                entry.args.joinToString(", ") { (name, t) -> "$name: ${t.shortName}" } +
                        ", finish: Boolean = false,\n    navTransition: NavTransition? = null"
            }
            val keyConstruct = if (entry.args.isNullOrEmpty()) {
                "AppScreens.$cls"
            } else {
                val argNames = entry.args.joinToString(", ") { (name, _) -> "$name = $name" }
                "AppScreens.$cls($argNames)"
            }
            "fun NavController.$fnName($paramList) {\n    navigate($keyConstruct, finish = finish, navTransition = navTransition)\n}"
        }

        // xxxArgs() — one extension per screen that has @NavEaseArgs
        val argsExtensions =
            entries.filter { !it.args.isNullOrEmpty() }.joinToString("\n\n") { entry ->
                val simpleName = entry.fqName.substringAfterLast('.')
                    val fnName = "${safeFunIdent(entry.route)}Args"
                        val argAssignments =
                            entry.args!!.joinToString(", ") { (name, _) -> "$name = key.$name" }
                        val cls = safeClassIdent(entry.route)
                        "fun NavKey.${fnName}(): $simpleName.Args {\n" +
                                "    val key = this as? AppScreens.$cls\n" +
                                "        ?: error(\n" +
                                "            \"NavEase: ${fnName}() called on '\${this::class.simpleName}' \" +\n" +
                                "            \"but expected AppScreens.$cls. \" +\n" +
                                "            \"Make sure you only call ${fnName}() from inside the ${cls} screen.\"\n" +
                                "        )\n" +
                                "    return $simpleName.Args($argAssignments)\n}"
            }

        val customImports = importsFrom(entries.flatMap { it.args.orEmpty() })
        val screenImports = entries
            .filter { !it.args.isNullOrEmpty() }
            .joinToString("\n") { "import ${it.fqName}" }

        val content = buildString {
            appendLine("package $generatedPackage")
            appendLine()
            appendLine("import androidx.navigation3.runtime.NavKey")
            appendLine("import io.github.alimsrepo.navease.runtime.navigation.NavController")
            appendLine("import io.github.alimsrepo.navease.runtime.presentation.NavTransition")
            if (screenImports.isNotEmpty()) appendLine(screenImports)
            if (customImports.isNotEmpty()) appendLine(customImports)
            appendLine()
            appendLine("// ── navigateToXxx() extensions on NavController ─────────────────────────")
            appendLine()
            appendLine(navExtensions)
            if (argsExtensions.isNotEmpty()) {
                appendLine()
                appendLine("// ── xxxArgs() extensions on NavKey ──────────────────────────────────────")
                appendLine()
                append(argsExtensions)
            }
        }

        val file = codeGenerator.createNewFile(deps, generatedPackage, "NavEaseExtensions")
        file.bufferedWriter().use { it.write(content) }
    }

    private fun generateAutoRegisterExtension(
        symbols: List<KSClassDeclaration>,
        deps: Dependencies,
    ) {
        // autoRegisterScreens() MUST be generated in the same package as the no-op stub so
        // that a single `import io.github.alimsrepo.navease.runtime.presentation.autoRegisterScreens`
        // in App.kt brings both overloads into scope and Kotlin's overload resolution can
        // pick this more-specific typed extension over the star-projected stub.
        val autoRegisterPackage = "io.github.alimsrepo.navease.runtime.presentation"

        val activityScreenFqn = "io.github.alimsrepo.navease.runtime.presentation.ActivityScreen"

        data class AutoEntry(
            val screenFqn: String,
            val screenSimpleName: String,
            val keyFqn: String,
            val keySimpleName: String,
            val rootFqn: String,
            val rootSimpleName: String,
            val isStart: Boolean,
        )

        val entries = symbols.mapNotNull { cls ->
            val screenFqn = cls.qualifiedName?.asString() ?: return@mapNotNull null
            val screenSimpleName = cls.simpleName.asString()

            // Read startDestination from the @AutoRegister annotation
            val annotation = cls.annotations.first { it.shortName.asString() == "AutoRegister" }
            val isStart = annotation.arguments
                .firstOrNull { it.name?.asString() == "startDestination" }
                ?.value as? Boolean ?: false

            // Resolve ActivityScreen<K> from supertypes
            val superType = cls.superTypes
                .map { it.resolve() }
                .firstOrNull { it.declaration.qualifiedName?.asString() == activityScreenFqn }

            if (superType == null) {
                logger.error(
                    "NavEase: @AutoRegister class '$screenSimpleName' must extend ActivityScreen<K>."
                )
                return@mapNotNull null
            }

            val keyType = superType.arguments.firstOrNull()?.type?.resolve()
            val keyDecl = keyType?.declaration as? KSClassDeclaration
            if (keyDecl == null) {
                logger.error(
                    "NavEase: @AutoRegister class '$screenSimpleName' — could not resolve NavKey type K."
                )
                return@mapNotNull null
            }

            val keyFqn = keyDecl.qualifiedName?.asString() ?: return@mapNotNull null

            // Root = the enclosing sealed class that K is nested inside
            val rootDecl = (keyDecl.parentDeclaration as? KSClassDeclaration)
                ?: keyDecl.superTypes
                    .map { it.resolve().declaration }
                    .filterIsInstance<KSClassDeclaration>()
                    .firstOrNull()

            if (rootDecl == null) {
                logger.error(
                    "NavEase: @AutoRegister class '$screenSimpleName' — could not determine the " +
                    "sealed Root NavKey type from K='${keyDecl.simpleName.asString()}'."
                )
                return@mapNotNull null
            }

            val rootFqn = rootDecl.qualifiedName?.asString() ?: return@mapNotNull null
            AutoEntry(
                screenFqn, screenSimpleName,
                keyFqn, keyDecl.simpleName.asString(),
                rootFqn, rootDecl.simpleName.asString(),
                isStart,
            )
        }

        if (entries.isEmpty()) return

        // ── Validate: one NavKey → one screen ─────────────────────────────────
        val keyGroups = entries.groupBy { it.keyFqn }
        keyGroups.filter { it.value.size > 1 }.forEach { (keyFqn, dupes) ->
            logger.error(
                "NavEase: NavKey '${keyFqn.substringAfterLast('.')}' is registered by " +
                "${dupes.size} @AutoRegister screens " +
                "(${dupes.joinToString { "'${it.screenSimpleName}'" }}). " +
                "Each NavKey can only be handled by one screen."
            )
        }
        if (keyGroups.any { it.value.size > 1 }) return

        // ── Validate: all screens share the same Root ──────────────────────────
        val rootTypes = entries.map { it.rootFqn }.toSet()
        if (rootTypes.size > 1) {
            logger.error(
                "NavEase: @AutoRegister screens belong to multiple root NavKey types: " +
                "${rootTypes.joinToString { "'${it.substringAfterLast('.')}'" }}. " +
                "All screens must share the same sealed root class."
            )
            return
        }

        // ── Validate: exactly one startDestination ─────────────────────────────
        val startEntries = entries.filter { it.isStart }
        if (startEntries.size > 1) {
            logger.error(
                "NavEase: ${startEntries.size} @AutoRegister screens have startDestination = true " +
                "(${startEntries.joinToString { "'${it.screenSimpleName}'" }}). " +
                "Exactly one screen must be the start destination."
            )
            return
        }

        val rootFqn = entries.first().rootFqn
        val rootSimpleName = entries.first().rootSimpleName
        val startEntry = startEntries.firstOrNull()
        val screenImports = entries.joinToString("\n") { "import ${it.screenFqn}" }

        // ── addEntry() calls inside the NavEaseAutoInit registrar lambda ───────
        // Each call registers one screen into NavEaseAutoRegistry.
        val addEntryCalls = entries.joinToString("\n") { entry ->
            val cls = entry.keySimpleName
            val root = entry.rootSimpleName
            if (entry.isStart) {
                "            NavEaseAutoRegistry.addEntry(${entry.screenSimpleName}(), $root.$cls::class, serializer<$root.$cls>(), $root.$cls)"
            } else {
                "            NavEaseAutoRegistry.addEntry(${entry.screenSimpleName}(), $root.$cls::class, serializer<$root.$cls>())"
            }
        }
        // ── add() calls inside autoRegisterScreens() ───────────────────────────
        val addCalls = entries.joinToString("\n") { "    add(${it.screenSimpleName}())" }

        val content = buildString {
            // Use the runtime presentation package so this file's autoRegisterScreens() extension
            // is co-located with the no-op stub. That way a single import statement in App.kt
            // brings both overloads into scope and Kotlin resolves the correct typed one.
            appendLine("package $autoRegisterPackage")
            appendLine()
            appendLine("import $rootFqn")
            // NavEaseAutoRegistry and NavEaseScreenScope are in the same package — no import needed,
            // but kept for clarity in case the generated file is read in isolation.
            appendLine("import kotlinx.serialization.serializer")
            appendLine(screenImports)
            appendLine()
            appendLine("// ── NavEaseAutoInit — self-registers all @AutoRegister screens ────────────")
            appendLine()
            appendLine("/**")
            appendLine(" * Auto-generated by NavEase KSP — do not edit.")
            appendLine(" *")
            appendLine(" * Sets [NavEaseAutoRegistry.registrar] so the runtime's universal")
            appendLine(" * `NavEaseHost()` overload can discover screens without any manual listing.")
            appendLine(" *")
            appendLine(" * On JVM/Android this object is initialised automatically via `Class.forName`")
            appendLine(" * triggered inside the runtime when [NavEaseHost] first composes.")
            appendLine(" * On other platforms, access is guaranteed through [autoRegisterScreens].")
            appendLine(" */")
            appendLine("private object NavEaseAutoInit {")
            appendLine("    init {")
            appendLine("        NavEaseAutoRegistry.setRegistrar {")
            appendLine(addEntryCalls)
            appendLine("        }")
            appendLine("    }")
            appendLine("}")
            appendLine()
            appendLine("/**")
            appendLine(" * Ensures [NavEaseAutoInit] is initialised when this file's class is loaded.")
            appendLine(" *")
            appendLine(" * - **JVM / Android**: initialised via `<clinit>` when `Class.forName` loads")
            appendLine(" *   this file's class. The runtime's [NavEaseHost] triggers this automatically.")
            appendLine(" * - **Kotlin/Native / JS / Wasm**: initialised when [autoRegisterScreens] is")
            appendLine(" *   called, which accesses this property as a side effect.")
            appendLine(" */")
            appendLine("@Suppress(\"unused\")")
            appendLine("private val _navEaseAutoInit: Any = NavEaseAutoInit")
            appendLine()
            appendLine("// ── autoRegisterScreens() ────────────────────────────────────────────────")
            appendLine()
            appendLine("/**")
            appendLine(" * Auto-generated by NavEase KSP — do not edit.")
            appendLine(" *")
            appendLine(" * Registers all `@AutoRegister` screens into the given [NavEaseScreenScope].")
            appendLine(" * Also ensures [NavEaseAutoRegistry] is populated as a side-effect, enabling")
            appendLine(" * the zero-configuration [NavEaseHost] overload on all platforms.")
            appendLine(" *")
            appendLine(" * Call this inside the typed [NavEaseHost] registration block:")
            appendLine(" * ```kotlin")
            appendLine(" * NavEaseHost<AppScreens>(start = AppScreens.Splash, onExitRequest = { ... }) {")
            appendLine(" *     autoRegisterScreens()")
            appendLine(" * }")
            appendLine(" * ```")
            if (startEntry != null) {
                appendLine(" *")
                appendLine(" * Start destination: [${startEntry.rootSimpleName}.${startEntry.keySimpleName}]")
                appendLine(" * (set via `@AutoRegister(startDestination = true)` on [${startEntry.screenSimpleName}]).")
            }
            appendLine(" */")
            appendLine("fun NavEaseScreenScope<$rootSimpleName>.autoRegisterScreens() {")
            appendLine("    // Accessing _navEaseAutoInit triggers NavEaseAutoInit.init {} on")
            appendLine("    // Kotlin/Native and JS, ensuring the global registry registrar is set.")
            appendLine("    @Suppress(\"UNUSED_EXPRESSION\")")
            appendLine("    _navEaseAutoInit")
            appendLine(addCalls)
            append("}")
        }

        // File is created in the runtime presentation package directory so both overloads of
        // autoRegisterScreens() (stub + generated) live in the same package.
        val file = codeGenerator.createNewFile(deps, autoRegisterPackage, "AutoRegisterScreens")
        file.bufferedWriter().use { it.write(content) }
    }

    private fun generateNavEaseHost(deps: Dependencies) {
        val content = buildString {
            appendLine("package $generatedPackage")
            appendLine()
            appendLine("import androidx.compose.runtime.Composable")
            appendLine("import io.github.alimsrepo.navease.runtime.presentation.NavEaseNavGraph")
            appendLine("import io.github.alimsrepo.navease.runtime.presentation.NavTransition")
            appendLine()
            appendLine("/**")
            appendLine(" * Generated navigation host. Place this once in your root composable.")
            appendLine(" *")
            appendLine(" * @param onExitRequest         Called when back is pressed on the root screen.")
            appendLine(" *                              Use this to show an exit dialog or finish the Activity.")
            appendLine(" *                              Defaults to a no-op (suitable for iOS / web targets).")
            appendLine(" * @param enableSharedTransitions When `true`, wraps the display in a")
            appendLine(" *                              [SharedTransitionLayout] enabling Compose shared element")
            appendLine(" *                              transitions between screens. Access the scope inside any")
            appendLine(" *                              screen via [LocalNavEaseSharedTransitionScope.current] and")
            appendLine(" *                              the animation scope via [LocalNavAnimatedContentScope.current].")
            appendLine(" *                              Defaults to `false`.")
            appendLine(" * @param navTransition         The screen-to-screen animation style.")
            appendLine(" *                              Defaults to [NavTransition.Push] (iOS-style horizontal slide).")
            appendLine(" *                              See [NavTransition] for all available options:")
            appendLine(" *                              [NavTransition.Push], [NavTransition.Fade], [NavTransition.Rise],")
            appendLine(" *                              [NavTransition.Zoom], [NavTransition.Depth], [NavTransition.Instant].")
            appendLine(" */")
            appendLine("@Composable")
            appendLine("fun NavEaseHost(")
            appendLine("    onExitRequest: () -> Unit = {},")
            appendLine("    enableSharedTransitions: Boolean = false,")
            appendLine("    navTransition: NavTransition = NavTransition.Push,")
            appendLine(") {")
            appendLine("    NavEaseNavGraph(")
            appendLine("        initialScreen = AppScreens.startDestination,")
            appendLine("        savedStateConfig = AppScreens.savedStateConfig,")
            appendLine("        screenFactory = ScreenFactory::createScreen,")
            appendLine("        onExitRequest = onExitRequest,")
            appendLine("        enableSharedTransitions = enableSharedTransitions,")
            appendLine("        navTransition = navTransition,")
            appendLine("    )")
            append("}")
        }

        val file = codeGenerator.createNewFile(deps, generatedPackage, "NavEaseHost")
        file.bufferedWriter().use { it.write(content) }
    }
}
