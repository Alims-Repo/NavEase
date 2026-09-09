package io.github.alimsrepo.navease.ksp

/**
 * One constructor parameter of a key class.
 *
 * @param name Parameter name, used both as the serial element name and as a named argument.
 * @param type Fully-qualified source type, e.g. `kotlin.String` or `kotlin.collections.List<kotlin.Int>`.
 */
internal data class KeyParam(val name: String, val type: String)

/**
 * One `@AutoRegister` screen, resolved and ready to emit.
 *
 * Every name is fully qualified. Generated code never relies on imports, so two sealed roots
 * that each declare a `Detail` cannot collide.
 *
 * @param screenFqn  The annotated screen class, e.g. `com.example.DetailScreen`.
 * @param keyFqn     The key it handles, e.g. `com.example.AppScreens.Detail`.
 * @param keyParams  Constructor parameters of the key; empty for a `data object`.
 * @param rootFqn    The outermost sealed root of [keyFqn], e.g. `com.example.AppScreens`.
 */
internal data class ScreenEntry(
    val screenFqn: String,
    val keyFqn: String,
    val keyParams: List<KeyParam>,
    val rootFqn: String,
) {
    /** Simple name of the screen class, for diagnostics. */
    val screenSimpleName: String get() = screenFqn.substringAfterLast('.')

    /** Name of the generated serializer object. Derived from the key FQN, so it is unique. */
    val serializerName: String get() = "NavEaseSer_${keyFqn.toIdentifier()}"
}

/** Turns a dotted name into a Kotlin identifier usable as a declaration or file name. */
internal fun String.toIdentifier(): String = replace(Regex("[^A-Za-z0-9]"), "_")
