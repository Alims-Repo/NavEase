package io.github.alimsrepo.navease.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Modifier

/**
 * Resolves every `@AutoRegister` screen and hands the result to [NavEaseCodegen], which
 * writes `AutoRegisterScreens.kt` into [generatedPackage] and one
 * `NavEaseHostOverloads_<module>.kt` into the runtime's host package.
 *
 * This class owns resolution and build-time validation only; emission lives in
 * [NavEaseCodegen] so it can be tested without running a compilation.
 *
 * @param codeGenerator    KSP code generator.
 * @param logger           KSP logger, used to report validation errors against the offending class.
 * @param generatedPackage Package for `AutoRegisterScreens.kt`, from the
 *                         `navease.generatedPackage` KSP option. The Gradle plugin makes it
 *                         unique per module by default.
 */
class NavEaseProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val generatedPackage: String = DEFAULT_GENERATED_PACKAGE,
) : SymbolProcessor {

    internal companion object {
        const val DEFAULT_GENERATED_PACKAGE = "io.github.alimsrepo.navease.generated"
        const val AUTO_REGISTER_FQN = "io.github.alimsrepo.navease.runtime.annotations.AutoRegister"
        const val ACTIVITY_SCREEN_FQN = "io.github.alimsrepo.navease.runtime.screen.ActivityScreen"
        const val NAV_EASE_ROOT_FQN = "io.github.alimsrepo.navease.runtime.NavEaseRoot"
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(AUTO_REGISTER_FQN)
            .filterIsInstance<KSClassDeclaration>()
            .toList()

        if (symbols.isEmpty()) return emptyList()

        // Sorted because KSP returns symbols in an unspecified order, and unsorted output
        // would make every build produce a different file.
        val entries = symbols.mapNotNull(::resolveEntry).sortedBy { it.keyFqn }

        if (entries.isEmpty() || !validate(entries)) return emptyList()

        val deps = Dependencies(
            aggregating = true,
            *symbols.mapNotNull { it.containingFile }.distinct().toTypedArray(),
        )

        write(
            deps = deps,
            packageName = generatedPackage,
            fileName = "AutoRegisterScreens",
            content = NavEaseCodegen.autoRegisterScreens(generatedPackage, entries),
        )
        write(
            deps = deps,
            packageName = NavEaseCodegen.HOST_PACKAGE,
            fileName = NavEaseCodegen.hostOverloadsFileName(generatedPackage),
            content = NavEaseCodegen.hostOverloads(generatedPackage, entries),
        )

        return emptyList()
    }

    private fun write(
        deps: Dependencies,
        packageName: String,
        fileName: String,
        content: String,
    ) {
        codeGenerator.createNewFile(deps, packageName, fileName)
            .bufferedWriter()
            .use { it.write(content) }
    }

    // ── Resolution ───────────────────────────────────────────────────────────

    /** Resolves one annotated class, reporting the first problem found and returning null. */
    private fun resolveEntry(cls: KSClassDeclaration): ScreenEntry? {
        val simpleName = cls.simpleName.asString()
        val screenFqn = cls.qualifiedName?.asString() ?: return null

        if (cls.classKind != ClassKind.CLASS || Modifier.ABSTRACT in cls.modifiers) {
            logger.error("NavEase: @AutoRegister '$simpleName' must be a concrete class.", cls)
            return null
        }

        // Generated code constructs the screen as `ScreenClass()`, so any parameter the
        // caller would have to supply is fatal.
        if (cls.primaryConstructor?.parameters.orEmpty().any { !it.hasDefault }) {
            logger.error(
                "NavEase: @AutoRegister '$simpleName' must have a no-argument constructor. " +
                    "NavEase creates one instance per host and cannot supply constructor " +
                    "arguments — obtain dependencies inside Content() instead.",
                cls,
            )
            return null
        }

        val activityScreen = cls.superTypes
            .map { it.resolve() }
            .firstOrNull { it.declaration.qualifiedName?.asString() == ACTIVITY_SCREEN_FQN }
        if (activityScreen == null) {
            logger.error(
                "NavEase: @AutoRegister '$simpleName' must extend ActivityScreen<K> directly. " +
                    "An intermediate base class of your own hides the key type from KSP — put " +
                    "shared behaviour in a composable called from Content() instead.",
                cls,
            )
            return null
        }

        val keyDecl = activityScreen.arguments.firstOrNull()?.type?.resolve()?.declaration
            as? KSClassDeclaration
        if (keyDecl == null) {
            logger.error(
                "NavEase: @AutoRegister '$simpleName' — could not resolve the key type K from " +
                    "ActivityScreen<K>.",
                cls,
            )
            return null
        }
        val keyFqn = keyDecl.qualifiedName?.asString() ?: return null

        if (!keyDecl.isNavEaseRoot()) {
            logger.error(
                "NavEase: @AutoRegister '$simpleName' uses key '${keyDecl.simpleName.asString()}', " +
                    "which does not implement NavEaseRoot. Declare your keys as " +
                    "`sealed class YourRoot : NavEaseRoot`.",
                cls,
            )
            return null
        }

        val rootFqn = resolveRoot(keyDecl).qualifiedName?.asString() ?: return null

        val keyParams = keyDecl.primaryConstructor?.parameters.orEmpty().map { param ->
            val name = param.name?.asString() ?: return null
            KeyParam(name = name, type = param.type.resolve().render())
        }

        return ScreenEntry(screenFqn, keyFqn, keyParams, rootFqn)
    }

    /**
     * Walks up the class hierarchy to the outermost class that still implements `NavEaseRoot`.
     *
     * For `Detail : AppScreens()` that is `AppScreens`. For a key under an intermediate sealed
     * layer — `Detail : Tab()`, `Tab : AppScreens()` — it is still `AppScreens`, which is the
     * root a host is opened for.
     */
    private fun resolveRoot(keyDecl: KSClassDeclaration): KSClassDeclaration {
        var current = keyDecl
        while (true) {
            val superClass = current.superTypes
                .map { it.resolve().declaration }
                .filterIsInstance<KSClassDeclaration>()
                .firstOrNull { it.classKind == ClassKind.CLASS }
                ?: return current
            if (!superClass.isNavEaseRoot()) return current
            current = superClass
        }
    }

    private fun KSClassDeclaration.isNavEaseRoot(): Boolean {
        if (qualifiedName?.asString() == NAV_EASE_ROOT_FQN) return true
        return superTypes.any {
            (it.resolve().declaration as? KSClassDeclaration)?.isNavEaseRoot() == true
        }
    }

    /** Reports every duplicate key rather than only the first, so one build surfaces them all. */
    private fun validate(entries: List<ScreenEntry>): Boolean {
        val duplicates = entries.groupBy { it.keyFqn }.filterValues { it.size > 1 }
        duplicates.forEach { (keyFqn, dupes) ->
            logger.error(
                "NavEase: key '$keyFqn' is claimed by ${dupes.size} @AutoRegister screens " +
                    "(${dupes.joinToString { it.screenSimpleName }}). Each key may be handled by " +
                    "exactly one screen.",
            )
        }
        return duplicates.isEmpty()
    }
}

/** Renders a type as a fully-qualified source expression, generics and nullability included. */
private fun KSType.render(): String {
    val fqn = declaration.qualifiedName?.asString() ?: declaration.simpleName.asString()
    val suffix = if (isMarkedNullable) "?" else ""
    if (arguments.isEmpty()) return "$fqn$suffix"

    val args = arguments.joinToString(", ") { arg ->
        val argType = arg.type?.resolve() ?: return@joinToString "*"
        val variance = arg.variance.label
        if (variance.isEmpty()) argType.render() else "$variance ${argType.render()}"
    }
    return "$fqn<$args>$suffix"
}
