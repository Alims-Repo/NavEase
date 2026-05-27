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

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (generated) return emptyList()

        val symbols = resolver.getSymbolsWithAnnotation("io.github.alimsrepo.navease.runtime.NavEaseScreen")
            .filterIsInstance<KSClassDeclaration>()
            .toList()

        if (symbols.isEmpty()) return emptyList()

        generated = true

        data class ScreenEntry(val route: String, val fqName: String)

        val entries = symbols.map { cls ->
            val route = cls.annotations.first().arguments.first().value as String
            val fqName = cls.qualifiedName!!.asString()
            ScreenEntry(route, fqName)
        }

        val imports = entries.joinToString("\n") { "import ${it.fqName}" }
        val whenBranches = entries.joinToString("\n            ") {
            "AppScreens.${it.route} -> ${it.fqName.substringAfterLast('.')}()"
        }

        val file = codeGenerator.createNewFile(
            Dependencies(false),
            "io.github.alimsrepo.navease.generated",
            "ScreenFactory"
        )
        file.bufferedWriter().use { writer ->
            writer.write("""
                package io.github.alimsrepo.navease.generated

                import io.github.alimsrepo.navease.runtime.domain.AppScreens
                import io.github.alimsrepo.navease.runtime.domain.NavScreen
                $imports

                object ScreenFactory {
                    @Suppress("UNCHECKED_CAST")
                    fun createScreen(appScreen: AppScreens): NavScreen<AppScreens> {
                        return when (appScreen) {
                            $whenBranches
                        } as NavScreen<AppScreens>
                    }
                }
            """.trimIndent())
        }

        return emptyList()
    }
}