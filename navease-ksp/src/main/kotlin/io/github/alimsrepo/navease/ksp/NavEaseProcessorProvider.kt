package io.github.alimsrepo.navease.ksp

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

/**
 * KSP entry-point that instantiates [NavEaseProcessor].
 *
 * This class is registered via `META-INF/services` and discovered automatically by KSP.
 */
class NavEaseProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        NavEaseProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger,
        )
}

