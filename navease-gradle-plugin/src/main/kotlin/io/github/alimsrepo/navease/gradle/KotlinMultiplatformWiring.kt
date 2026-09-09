package io.github.alimsrepo.navease.gradle

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

/**
 * Every use of a Kotlin Gradle plugin type lives here rather than on [NavEasePlugin].
 *
 * Gradle resolves the types named in a plugin class's own method signatures when it
 * decorates that class. Keeping them off [NavEasePlugin] means a build without the Kotlin
 * Gradle plugin fails with the message below instead of a `NoClassDefFoundError` raised
 * before the plugin's own code ever runs.
 */
internal object KotlinMultiplatformWiring {

    /**
     * Everything that compiles or processes Kotlin must run after the common-metadata KSP
     * task, which is what produces `AutoRegisterScreens.kt`.
     *
     * Two hooks are needed: per-target KSP tasks are `KspAATask`s, not
     * `KotlinCompilationTask`s, so one `withType` does not reach both.
     */
    fun wireTaskOrdering(project: Project, kspMetadataTask: String) {
        project.tasks.withType(KotlinCompilationTask::class.java).configureEach { task ->
            if (task.name != kspMetadataTask) task.dependsOn(kspMetadataTask)
        }
        project.tasks.configureEach { task ->
            if (task.name != kspMetadataTask && task.name.startsWith("ksp")) {
                task.dependsOn(kspMetadataTask)
            }
        }
    }

    /** Registers the KSP output directory as a `commonMain` source directory. */
    fun addGeneratedSourceDir(project: Project, path: String) {
        val dir = project.layout.buildDirectory.dir(path)
        commonMain(project).kotlin.srcDir(dir)
        project.logger.info("[NavEase] Registered generated source dir: $path")
    }

    /** Adds the runtime to `commonMain`. */
    fun addRuntimeDependency(project: Project, dependency: Any) {
        commonMain(project).dependencies { implementation(dependency) }
        project.logger.info("[NavEase] Added runtime: $dependency")
    }

    private fun kmpOrNull(project: Project): KotlinMultiplatformExtension? =
        project.extensions.findByType(KotlinMultiplatformExtension::class.java)

    private fun commonMain(project: Project) =
        kmpOrNull(project)?.sourceSets?.getByName("commonMain")
            ?: throw GradleException("[NavEase] commonMain source set not found.")
}
