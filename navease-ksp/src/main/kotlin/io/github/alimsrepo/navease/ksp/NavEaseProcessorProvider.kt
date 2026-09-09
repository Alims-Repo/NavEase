package io.github.alimsrepo.navease.ksp

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

/**
 * Entry point KSP loads to create the NavEase processor.
 *
 * ## Options
 *
 * | Option | Default | Meaning |
 * |---|---|---|
 * | `navease.generatedPackage` | `io.github.alimsrepo.navease.generated` | Package for `AutoRegisterScreens.kt`. |
 *
 * The NavEase Gradle plugin sets this for you, appending the module name so that several
 * modules never generate into the same package. Set it by hand only in a manual setup:
 *
 * ```kotlin
 * ksp {
 *     arg("navease.generatedPackage", "com.example.app.navigation")
 * }
 * ```
 */
class NavEaseProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        NavEaseProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger,
            generatedPackage = environment.options["navease.generatedPackage"]
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?: NavEaseProcessor.DEFAULT_GENERATED_PACKAGE,
        )
}
