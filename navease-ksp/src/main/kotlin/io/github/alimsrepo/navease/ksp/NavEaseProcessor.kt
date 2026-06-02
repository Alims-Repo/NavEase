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
            generateNavEaseHostOverloads(autoRegisterSymbols, deps)
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

    // ── NavKey serializer code-gen ──────────────────────────────────────────
    //
    // Generates a private KSerializer<Root.Sub> object for each @AutoRegister
    // key class.  This replaces the previous `serializer<Root.Sub>()` call that
    // required `@Serializable` on the user's sealed hierarchy.
    //
    // • data object  → empty structure (no fields)
    // • data class   → encodes/decodes every constructor parameter in order
    //
    // All field serializers are resolved at object-initialisation time via
    // `serializer(typeOf<T>())`, so the field types still need to be
    // serializable (Kotlin primitives, String, or @Serializable custom types).

    private fun generateNavKeySerializer(
        keySimpleName: String,
        keyFqName: String,
        keyParams: List<ArgParam>,
    ): String = buildString {
        val serName = "NavEase_${keySimpleName}_Ser"
        val qualKey = keyFqName

        if (keyParams.isEmpty()) {
            // ── data object ──────────────────────────────────────────────────
            appendLine("private object $serName : KSerializer<$qualKey> {")
            appendLine("    override val descriptor = buildClassSerialDescriptor(\"$qualKey\")")
            appendLine("    override fun serialize(encoder: Encoder, value: $qualKey) {")
            appendLine("        encoder.beginStructure(descriptor).endStructure(descriptor)")
            appendLine("    }")
            appendLine("    override fun deserialize(decoder: Decoder): $qualKey {")
            appendLine("        decoder.beginStructure(descriptor).endStructure(descriptor)")
            appendLine("        return $qualKey")
            appendLine("    }")
            append("}")
        } else {
            // ── data class with constructor params ───────────────────────────
            appendLine("@Suppress(\"UNCHECKED_CAST\")")
            appendLine("private object $serName : KSerializer<$qualKey> {")
            // One field serializer per parameter, resolved once at init time.
            keyParams.forEachIndexed { i, p ->
                appendLine(
                    "    private val _f${i}_ser = " +
                    "serializer(typeOf<${p.typeInfo.shortName}>()) as KSerializer<${p.typeInfo.shortName}>"
                )
            }
            appendLine("    override val descriptor = buildClassSerialDescriptor(\"$qualKey\") {")
            keyParams.forEachIndexed { i, p ->
                appendLine("        element(\"${p.name}\", _f${i}_ser.descriptor)")
            }
            appendLine("    }")
            // serialize
            appendLine("    override fun serialize(encoder: Encoder, value: $qualKey) {")
            appendLine("        val c = encoder.beginStructure(descriptor)")
            keyParams.forEachIndexed { i, p ->
                appendLine(
                    "        c.encodeSerializableElement(descriptor, $i, _f${i}_ser, value.${p.name})"
                )
            }
            appendLine("        c.endStructure(descriptor)")
            appendLine("    }")
            // deserialize — use Any? intermediaries and cast at the end to avoid
            // needing a per-type default value.
            appendLine("    override fun deserialize(decoder: Decoder): $qualKey {")
            keyParams.forEachIndexed { i, _ -> appendLine("        var _f$i: Any? = null") }
            appendLine("        val c = decoder.beginStructure(descriptor)")
            appendLine("        loop@ while (true) {")
            appendLine("            when (val idx = c.decodeElementIndex(descriptor)) {")
            keyParams.forEachIndexed { i, _ ->
                appendLine(
                    "                $i -> _f$i = c.decodeSerializableElement(descriptor, $i, _f${i}_ser)"
                )
            }
            appendLine("                CompositeDecoder.DECODE_DONE -> break@loop")
            appendLine(
                "                else -> throw SerializationException(" +
                "\"NavEase: unexpected index \$idx in $qualKey\")"
            )
            appendLine("            }")
            appendLine("        }")
            appendLine("        c.endStructure(descriptor)")
            val ctorArgs = keyParams.mapIndexed { i, p ->
                "${p.name} = _f$i as ${p.typeInfo.shortName}"
            }.joinToString(", ")
            appendLine("        return $qualKey($ctorArgs)")
            appendLine("    }")
            append("}")
        }
    }

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
            appendLine("import io.github.alimsrepo.navease.runtime.transition.NavTransition")
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
        // Accept both the canonical new package and the old presentation typealias so that
        // users who still import from presentation.ActivityScreen are not broken.
        val activityScreenFqns = setOf(
            "io.github.alimsrepo.navease.runtime.screen.ActivityScreen",
            "io.github.alimsrepo.navease.runtime.presentation.ActivityScreen",
        )
        val registryFqn = "io.github.alimsrepo.navease.runtime.registry.NavEaseAutoRegistry"

        data class AutoEntry(
            val screenFqn: String,
            val screenSimpleName: String,
            val keyFqn: String,
            val keySimpleName: String,
            /** Constructor parameters of the NavKey subclass (empty for data objects). */
            val keyParams: List<ArgParam>,
            val rootFqn: String,
            val rootSimpleName: String,
            val isStart: Boolean,
        )

        val entries = symbols.mapNotNull { cls ->
            val screenFqn = cls.qualifiedName?.asString() ?: return@mapNotNull null
            val screenSimpleName = cls.simpleName.asString()

            val annotation = cls.annotations.first { it.shortName.asString() == "AutoRegister" }
            val isStart = annotation.arguments
                .firstOrNull { it.name?.asString() == "startDestination" }
                ?.value as? Boolean ?: false

            val superType = cls.superTypes
                .map { it.resolve() }
                .firstOrNull { it.declaration.qualifiedName?.asString() in activityScreenFqns }

            if (superType == null) {
                logger.error(
                    "NavEase: @AutoRegister class '$screenSimpleName' must extend " +
                    "ActivityScreen<K> (import from io.github.alimsrepo.navease.runtime.screen)."
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

            // Read constructor parameters of the key class so we can generate a
            // KSerializer for it without requiring @Serializable on the user's class.
            val keyParams: List<ArgParam> = keyDecl.primaryConstructor
                ?.parameters
                ?.map { param ->
                    val resolved = param.type.resolve()
                    ArgParam(param.name!!.asString(), resolveTypeInfo(resolved), resolved)
                }
                ?: emptyList()

            AutoEntry(
                screenFqn, screenSimpleName,
                keyFqn, keyDecl.simpleName.asString(),
                keyParams,
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

        val startEntry = startEntries.firstOrNull()
        val screenImports = entries.joinToString("\n") { "import ${it.screenFqn}" }
        // All unique root and key FQNs — may span multiple sealed classes (e.g. AppScreens + WizardStep)
        val rootImports = entries.map { it.rootFqn }.distinct().joinToString("\n") { "import $it" }
        val keyImports = entries.map { it.keyFqn }.distinct().joinToString("\n") { "import $it" }

        // ── Generate KSerializer objects for every key class ───────────────
        // These replace the previous `serializer<Root.Sub>()` calls, removing
        // the @Serializable requirement from the user's sealed hierarchy.
        val serializersBlock = entries.joinToString("\n\n") { entry ->
            generateNavKeySerializer(entry.keySimpleName, entry.keyFqn, entry.keyParams)
        }

        // Imports needed by field-type serializers (e.g. data class args)
        val keyParamImports = importsFrom(entries.flatMap { it.keyParams })
        val hasParamsEntries = entries.any { it.keyParams.isNotEmpty() }

        val addEntryCalls = entries.joinToString("\n") { entry ->
            "        NavEaseAutoRegistry.addEntry(${entry.screenSimpleName}(), ${entry.keySimpleName}::class, ${entry.rootSimpleName}::class, NavEase_${entry.keySimpleName}_Ser)"
        }

        val content = buildString {
            appendLine("package $generatedPackage")
            appendLine()
            appendLine("import $registryFqn")
            // kotlinx.serialization — core serialization API
            appendLine("import kotlinx.serialization.KSerializer")
            appendLine("import kotlinx.serialization.SerializationException")
            appendLine("import kotlinx.serialization.descriptors.buildClassSerialDescriptor")
            appendLine("import kotlinx.serialization.encoding.CompositeDecoder")
            appendLine("import kotlinx.serialization.encoding.Decoder")
            appendLine("import kotlinx.serialization.encoding.Encoder")
            if (hasParamsEntries) {
                // typeOf and serializer(KType) are only needed when key classes have fields.
                appendLine("import kotlin.reflect.typeOf")
                appendLine("import kotlinx.serialization.serializer")
            }
            if (keyParamImports.isNotEmpty()) appendLine(keyParamImports)
            appendLine(rootImports)
            appendLine(keyImports)
            appendLine(screenImports)
            appendLine()
            appendLine("// Auto-generated by NavEase KSP — do not edit.")
            appendLine()
            appendLine("// ── NavKey serializers (replaces @Serializable on sealed hierarchy) ────────")
            appendLine()
            appendLine(serializersBlock)
            appendLine()
            appendLine("private object NavEaseAutoInit {")
            appendLine("    init {")
            appendLine(addEntryCalls)
            appendLine("        NavEaseAutoRegistry.registerBootstrapHook {")
            appendLine("            @Suppress(\"UNUSED_EXPRESSION\")")
            appendLine("            _navEaseAutoInit")
            appendLine("        }")
            appendLine("    }")
            appendLine("}")
            appendLine()
            appendLine("@Suppress(\"unused\")")
            appendLine("internal val _navEaseAutoInit: Any = NavEaseAutoInit")
            appendLine()
            appendLine("/** Triggers NavEase screen registry initialisation.")
            appendLine(" * Called manually from platform entry points (iOS, Web) or automatically")
            appendLine(" * via Class.forName on Android/JVM. Safe to call multiple times (idempotent). */")
            appendLine("fun navEaseBootstrap() {")
            appendLine("    @Suppress(\"UNUSED_EXPRESSION\")")
            appendLine("    _navEaseAutoInit")
            append("}")
        }

        val file = codeGenerator.createNewFile(deps, generatedPackage, "AutoRegisterScreens")
        file.bufferedWriter().use { it.write(content) }
    }

    private fun generateNavEaseHostOverloads(
        symbols: List<KSClassDeclaration>,
        deps: Dependencies,
    ) {
        val activityScreenFqns = setOf(
            "io.github.alimsrepo.navease.runtime.screen.ActivityScreen",
            "io.github.alimsrepo.navease.runtime.presentation.ActivityScreen",
        )

        val roots = symbols.mapNotNull { cls ->
            val screenSimpleName = cls.simpleName.asString()
            val superType = cls.superTypes
                .map { it.resolve() }
                .firstOrNull { it.declaration.qualifiedName?.asString() in activityScreenFqns } ?: return@mapNotNull null

            val keyType = superType.arguments.firstOrNull()?.type?.resolve()
            val keyDecl = keyType?.declaration as? KSClassDeclaration ?: return@mapNotNull null

            // Ensure key extends NavEaseRoot (directly or indirectly)
            fun KSClassDeclaration.isNavEaseRoot(): Boolean {
                if (qualifiedName?.asString() == "io.github.alimsrepo.navease.runtime.NavEaseRoot") return true
                return superTypes.any { 
                    val decl = it.resolve().declaration as? KSClassDeclaration
                    decl?.isNavEaseRoot() == true
                }
            }
            val isNavEaseRoot = keyDecl.isNavEaseRoot()

            if (!isNavEaseRoot) {
                logger.error(
                    "NavEase: @AutoRegister class '$screenSimpleName' uses key '${keyDecl.simpleName.asString()}' " +
                    "which does not extend NavEaseRoot. Auto-registration requires NavEaseRoot."
                )
                return@mapNotNull null
            }

            val rootDecl = (keyDecl.parentDeclaration as? KSClassDeclaration)
                ?: keyDecl.superTypes
                    .map { it.resolve().declaration }
                    .filterIsInstance<KSClassDeclaration>()
                    .firstOrNull { it.modifiers.contains(com.google.devtools.ksp.symbol.Modifier.SEALED) }

            rootDecl
        }.distinctBy { it.qualifiedName?.asString() }

        if (roots.isEmpty()) return

        val hostPackage = "io.github.alimsrepo.navease.runtime.host"

        val overloads = roots.joinToString("\n\n") { root ->
            val rootSimpleName = root.simpleName.asString()

            """
            /**
             * Smart overload for [NavEaseHost] that automatically bootstraps the registry
             * for the [$rootSimpleName] root.
             */
            @Composable
            inline fun <reified T : $rootSimpleName> NavEaseHost(
                start: $rootSimpleName,
                noinline onExitRequest: () -> Unit = {},
                enableSharedTransitions: Boolean = false,
                navTransition: NavTransition = NavTransition.Push,
            ) {
                $generatedPackage.navEaseBootstrap()
                NavEaseHostForRoot(
                    rootClass = $rootSimpleName::class,
                    start = start,
                    onExitRequest = onExitRequest,
                    enableSharedTransitions = enableSharedTransitions,
                    navTransition = navTransition,
                )
            }
            """.trimIndent()
        }

        val rootImports = roots.joinToString("\n") { "import ${it.qualifiedName?.asString()}" }

        val content = buildString {
            appendLine("package $hostPackage")
            appendLine()
            appendLine("import androidx.compose.runtime.Composable")
            appendLine("import io.github.alimsrepo.navease.runtime.transition.NavTransition")
            appendLine(rootImports)
            appendLine()
            appendLine("// Auto-generated by NavEase KSP — do not edit.")
            appendLine()
            appendLine(overloads)
        }

        val file = codeGenerator.createNewFile(deps, hostPackage, "NavEaseHostOverloads")
        file.bufferedWriter().use { it.write(content) }
    }

    private fun generateNavEaseHost(deps: Dependencies) {
        val hostPackage = "io.github.alimsrepo.navease.runtime.host"
        val content = buildString {
            appendLine("package $hostPackage")
            appendLine()
            appendLine("import androidx.compose.runtime.Composable")
            appendLine("import io.github.alimsrepo.navease.runtime.host.NavEaseNavGraph")
            appendLine("import io.github.alimsrepo.navease.runtime.transition.NavTransition")
            appendLine("import $generatedPackage.AppScreens")
            appendLine("import $generatedPackage.ScreenFactory")
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
            appendLine("}")
            appendLine()
            appendLine("/**")
            appendLine(" * Generated navigation host with explicit start destination.")
            appendLine(" */")
            appendLine("@Composable")
            appendLine("fun NavEaseHost(")
            appendLine("    start: AppScreens,")
            appendLine("    onExitRequest: () -> Unit = {},")
            appendLine("    enableSharedTransitions: Boolean = false,")
            appendLine("    navTransition: NavTransition = NavTransition.Push,")
            appendLine(") {")
            appendLine("    NavEaseNavGraph(")
            appendLine("        initialScreen = start,")
            appendLine("        savedStateConfig = AppScreens.savedStateConfig,")
            appendLine("        screenFactory = ScreenFactory::createScreen,")
            appendLine("        onExitRequest = onExitRequest,")
            appendLine("        enableSharedTransitions = enableSharedTransitions,")
            appendLine("        navTransition = navTransition,")
            appendLine("    )")
            appendLine("}")
        }

        val file = codeGenerator.createNewFile(deps, hostPackage, "NavEaseHost")
        file.bufferedWriter().use { it.write(content) }
    }
}
