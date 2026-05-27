package io.github.alimsrepo.navease.runtime.annotations

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