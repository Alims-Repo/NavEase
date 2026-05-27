package io.github.alimsrepo.navease.runtime

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class NavEaseScreen(
    val route: String,
    val startDestination: Boolean = false
)

/**
 * Annotate a nested data class inside a [NavEaseScreen] class to declare
 * route arguments. KSP will generate a `data class` route instead of a `data object`.
 *
 * Example:
 * ```
 * @NavEaseScreen(route = "Profile")
 * class ProfileScreen : NavScreen<AppScreens.Profile>() {
 *
 *     @NavEaseArgs
 *     data class Args(val userId: String, val age: Int)
 * }
 * ```
 * Generates: `@Serializable data class Profile(val userId: String, val age: Int) : AppScreens()`
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class NavEaseArgs

/**
 * Annotate a nested data class inside a [NavEaseScreen] class to declare
 * the result this screen can return. KSP generates typed `backWithXxxResult()`
 * and `xxxResult()` extension functions on [io.github.alimsrepo.navease.runtime.data.NavController].
 *
 * Example:
 * ```
 * @NavEaseScreen(route = "ImagePicker")
 * class ImagePickerScreen : NavScreen<AppScreens.ImagePicker>() {
 *
 *     @NavEaseResult
 *     data class Result(val imageUri: String, val success: Boolean)
 * }
 * ```
 * Generated extensions:
 * ```
 * fun NavController.backWithImagePickerResult(imageUri: String, success: Boolean)
 * @Composable fun NavController.imagePickerResult(): State<ImagePickerResult?>
 * ```
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class NavEaseResult

