package io.github.alimsrepo.navease.ksp

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

/**
 * KSP processor provider for NavEase.
 *
 * ## KSP options
 *
 * | Option | Default | Description |
 * |---|---|---|
 * | `navease.generatedPackage` | `io.github.alimsrepo.navease.generated` | Package for all generated files. Override to avoid namespace collisions or to align with your app package. |
 *
 * ### Configuring in `build.gradle.kts`
 *
 * ```kotlin
 * ksp {
 *     arg("navease.generatedPackage", "com.myapp.navigation.generated")
 * }
 * ```
 */
class NavEaseProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        val generatedPackage = environment.options["navease.generatedPackage"]
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: "io.github.alimsrepo.navease.generated"

        return NavEaseProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger,
            generatedPackage = generatedPackage,
        )
    }
}