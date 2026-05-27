package io.github.alimsrepo.navease.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.validate
import java.io.PrintWriter

/**
 * KSP processor for the NavEase library.
 *
 * Scans the compilation unit for all functions annotated with
 * `@io.github.alimsrepo.navease.NavEaseScreen` and generates two artifacts in
 * `io.github.alimsrepo.navease.generated`:
 *
 * 1. **`NavEaseGeneratedFactory`** — an object implementing `NavEaseScreenFactory` that
 *    maps every registered route key to its composable content.
 * 2. **`NavEaseHost`** — a top-level `@Composable` function that wires in the factory so
 *    users never have to reference implementation details.
 *
 * ### Multi-round strategy
 * KSP may call [process] multiple times when symbols cannot be resolved on the first pass.
 * This processor accumulates valid screens across rounds. If any symbols remain permanently
 * unresolvable, [finish] generates the factory now with what we have.
 */
class NavEaseProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {

    companion object {
        private const val ANNOTATION_FQCN = "io.github.alimsrepo.navease.NavEaseScreen"
        private const val GENERATED_PACKAGE = "io.github.alimsrepo.navease.generated"
        private const val GENERATED_FILE = "NavEaseGeneratedFactory"
    }

    /** Accumulates screens discovered across all KSP processing rounds. */
    private val allScreens = mutableListOf<ScreenInfo>()
    private var generated = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (generated) return emptyList()

        val symbols = resolver.getSymbolsWithAnnotation(ANNOTATION_FQCN).toList()
        val deferred = symbols.filterNot { it.validate() }
        val validFunctions = symbols
            .filter { it.validate() }
            .filterIsInstance<KSFunctionDeclaration>()

        val newScreens = validFunctions.mapNotNull { fn -> extractScreenInfo(fn) }
        allScreens.addAll(newScreens)

        logger.info(
            "NavEaseProcessor [round]: found ${newScreens.size} screens, " +
                    "${deferred.size} deferred, ${allScreens.size} total.",
        )

        if (deferred.isEmpty()) {
            // All symbols resolved — generate immediately
            if (allScreens.isNotEmpty()) {
                logger.info("NavEaseProcessor: generating factory for ${allScreens.size} screen(s).")
                generateCode(allScreens)
                generated = true
            } else {
                logger.warn("NavEaseProcessor: no @NavEaseScreen annotated functions found.")
            }
            return emptyList()
        }

        // Symbols still pending — defer to next round
        return deferred
    }

    /**
     * Called by KSP after all rounds complete.
     * If there are accumulated screens but generation was deferred due to unresolvable symbols,
     * generate the factory now with what we have.
     */
    override fun finish() {
        if (!generated && allScreens.isNotEmpty()) {
            logger.warn(
                "NavEaseProcessor (finish): generating factory with ${allScreens.size} screen(s). " +
                        "Some symbols may have been permanently unresolvable.",
            )
            generateCode(allScreens)
            generated = true
        }
    }

    // ──────────────────────────────────────────────────────────────────────────────────────────
    // Extraction
    // ──────────────────────────────────────────────────────────────────────────────────────────

    private fun extractScreenInfo(fn: KSFunctionDeclaration): ScreenInfo? {
        val annotation = fn.annotations.find { ann ->
            ann.annotationType.resolve().declaration.qualifiedName?.asString() == ANNOTATION_FQCN
        } ?: return null

        val routeArg = annotation.arguments.find { it.name?.asString() == "route" }
        val routeFqcn = (routeArg?.value as? KSType)
            ?.declaration?.qualifiedName?.asString()
            ?: run {
                logger.error(
                    "NavEaseProcessor: 'route' argument missing/invalid on " +
                            "${fn.qualifiedName?.asString()}",
                )
                return null
            }

        val transitionArg = annotation.arguments.find { it.name?.asString() == "transition" }
        val transitionName = transitionArg?.value?.toString()?.substringAfterLast('.') ?: "SLIDE"

        val fnFqcn = fn.qualifiedName?.asString() ?: run {
            logger.error("NavEaseProcessor: cannot get qualified name for ${fn.simpleName.asString()}")
            return null
        }

        return ScreenInfo(fnFqcn = fnFqcn, routeFqcn = routeFqcn, transitionName = transitionName)
    }

    // ──────────────────────────────────────────────────────────────────────────────────────────
    // Code generation
    // ──────────────────────────────────────────────────────────────────────────────────────────

    private fun generateCode(screens: List<ScreenInfo>) {
        val outputFile = codeGenerator.createNewFile(
            dependencies = Dependencies(aggregating = true),
            packageName = GENERATED_PACKAGE,
            fileName = GENERATED_FILE,
        )
        PrintWriter(outputFile).use { w ->
            w.writeFileHeader()
            w.writeFactory(screens)
            w.writeSeparator()
            w.writeHostWrapper()
        }
    }

    private fun PrintWriter.writeFileHeader() {
        println("// ⚠️  THIS FILE IS AUTO-GENERATED BY NAVEASE KSP — DO NOT EDIT.")
        println("// Re-run the KSP processor (clean + build) to regenerate after changing screens.")
        println()
        println("package $GENERATED_PACKAGE")
        println()
        println("import io.github.alimsrepo.navease.NavEaseKey")
        println("import io.github.alimsrepo.navease.NavEaseController")
        println("import io.github.alimsrepo.navease.NavEaseScreenFactory")
        println("import io.github.alimsrepo.navease.NavEaseHostInternal")
        println("import io.github.alimsrepo.navease.Transition")
        println("import androidx.compose.runtime.Composable")
        println("import androidx.compose.ui.Modifier")
        println()
    }

    private fun PrintWriter.writeFactory(screens: List<ScreenInfo>) {
        println("/**")
        println(" * Auto-generated [NavEaseScreenFactory] that maps every route registered")
        println(" * with `@NavEaseScreen` to its composable content.")
        println(" *")
        println(" * Registered screens:")
        screens.forEach { println(" *  - [${it.routeFqcn}] → `${it.fnFqcn}`") }
        println(" */")
        println("object NavEaseGeneratedFactory : NavEaseScreenFactory {")
        println()
        println("    @Composable")
        println("    override fun Content(key: NavEaseKey, nav: NavEaseController) {")
        println("        when (key) {")
        screens.forEach { screen ->
            println("            is ${screen.routeFqcn} -> ${screen.fnFqcn}(key, nav)")
        }
        println("            else -> { /* unknown key — no-op */ }")
        println("        }")
        println("    }")
        println()
        println("    override fun transitionFor(key: NavEaseKey): Transition = when (key) {")
        screens.forEach { screen ->
            println("        is ${screen.routeFqcn} -> Transition.${screen.transitionName}")
        }
        println("        else -> Transition.SLIDE")
        println("    }")
        println("}")
    }

    private fun PrintWriter.writeSeparator() {
        println()
        println("// ─────────────────────────────────────────────────────────────────────────────")
        println()
    }

    private fun PrintWriter.writeHostWrapper() {
        println("/**")
        println(" * Auto-generated NavEase entry-point composable.")
        println(" *")
        println(" * Place this at the root of your `setContent { }` block.")
        println(" *")
        println(" * @param startDestination  The initial route key shown on launch.")
        println(" * @param modifier          Optional [Modifier] for the root container.")
        println(" * @param debugOverlay      Show the debug back-stack overlay (debug builds only).")
        println(" */")
        println("@Composable")
        println("fun NavEaseHost(")
        println("    startDestination: NavEaseKey,")
        println("    modifier: Modifier = Modifier,")
        println("    debugOverlay: Boolean = false,")
        println(") {")
        println("    NavEaseHostInternal(")
        println("        startDestination = startDestination,")
        println("        factory = NavEaseGeneratedFactory,")
        println("        modifier = modifier,")
        println("        debugOverlay = debugOverlay,")
        println("    )")
        println("}")
    }

    // ──────────────────────────────────────────────────────────────────────────────────────────
    // Data
    // ──────────────────────────────────────────────────────────────────────────────────────────

    /**
     * @property fnFqcn        Fully-qualified name of the annotated composable function.
     * @property routeFqcn     Fully-qualified name of the route key class.
     * @property transitionName Simple name of the [Transition] enum constant (e.g. `"SLIDE"`).
     */
    private data class ScreenInfo(
        val fnFqcn: String,
        val routeFqcn: String,
        val transitionName: String,
    )
}
