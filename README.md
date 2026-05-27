# NavEase

**NavEase** is a KSP-powered, annotation-driven navigation library for **Kotlin Multiplatform + Compose Multiplatform**.  
Annotate your screen classes → KSP generates the route hierarchy, screen factory, typed results, and the nav host.  
No manual registration. No reflection. No string routes.

> ⚠️ **Status:** pre-release — not yet published to Maven Central.

---

## Table of Contents

- [Features](#features)
- [How It Works](#how-it-works)
- [Platform Support](#platform-support)
- [Setup](#setup)
- [Quick Start](#quick-start)
- [Annotation Reference](#annotation-reference)
- [NavController API](#navcontroller-api)
- [Back-with-Result](#back-with-result)
- [KSP-Generated Code](#ksp-generated-code)
- [Module Structure](#module-structure)
- [Sample App](#sample-app)
- [FAQ](#faq)
- [License](#license)

---

## Features

| Feature | Details |
|---|---|
| **Zero boilerplate** | KSP generates the entire route sealed class, screen factory, and typed extensions at compile time |
| **Typed arguments** | `@NavEaseArgs` generates typed `navKey.xxxArgs()` extensions — access fields instantly |
| **Typed results** | `@NavEaseResult` generates strongly-typed `backWithXxxResult()` and `xxxResult()` extensions |
| **Class-based screens** | Extend plain `NavScreen` — no generics, no generated types in your screen signatures |
| **KMP-native** | Backed by JetBrains Navigation3 — runs on Android, iOS, Desktop, Web (JS, WASM) |
| **State restoration** | `SavedStateConfiguration` is generated and wired automatically |
| **Exit hook** | `onExitRequest` lambda on `NavEaseHost` — show a dialog or finish the Activity/window |
| **`singleTop` navigation** | `navigate(key, singleTop = true)` — prevents duplicate back-stack entries |
| **`popUpTo` navigation** | `popUpTo(key, inclusive)` — clear screens down to a typed destination |
| **Composition local** | `LocalNavEaseController.current` — access `NavController` from any nested composable |

---

## How It Works

```
You write:
  @NavEaseScreen(route = "Profile", startDestination = false)
  class ProfileScreen : NavScreen() {
      @NavEaseArgs data class Args(val userId: String)
      @NavEaseResult data class Result(val updated: Boolean)
      …
  }

                    ↓  KSP runs at compile time  ↓

KSP generates (5 files in io.github.alimsrepo.navease.generated):
  ┌─ AppScreens.kt        — sealed class AppScreens : NavKey { … }
  ├─ ScreenFactory.kt     — object ScreenFactory { fun createScreen(key) }
  ├─ NavEaseExtensions.kt — navigateToXxx() + xxxArgs() typed extensions
  ├─ NavEaseResults.kt    — data class ProfileResult + typed result extensions
  └─ NavEaseHost.kt       — @Composable fun NavEaseHost(onExitRequest)

Runtime (navease-runtime, commonMain):
  NavEaseHost → NavEaseNavGraph
    └─ NavDisplay (org.jetbrains.androidx.navigation3) — KMP back stack
         └─ NavController — navigate / back / popUpTo / singleTop / result store
```

---

## Platform Support

| Platform | Target | Entry point |
|---|---|---|
| Android | `android` | `MainActivity` → `setContent { App() }` |
| iOS | `iosArm64`, `iosSimulatorArm64` | `MainViewController()` → `ComposeUIViewController { App() }` |
| Desktop (JVM) | `jvm` | `application { Window { App() } }` |
| Web (JS) | `js { browser() }` | `ComposeViewport { App() }` |
| Web (WASM) | `wasmJs { browser() }` | `ComposeViewport { App() }` |

---

## Setup

### 1. Apply plugins in your shared KMP module

```kotlin
// shared/build.gradle.kts
plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")     // new KMP library plugin
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}
```

### 2. Add dependencies

```kotlin
// shared/build.gradle.kts
kotlin {
    sourceSets {
        commonMain {
            // Point KSP metadata output to commonMain so generated code is visible to all targets
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")

            dependencies {
                implementation("io.github.alimsrepo:navease-runtime:<version>")
            }
        }
    }
}

dependencies {
    // KSP runs once against commonMain metadata — covers all platform targets
    add("kspCommonMainMetadata", "io.github.alimsrepo:navease-ksp:<version>")
}

// All compilations must wait for KSP to finish first
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>>().configureEach {
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}
```

### 3. Trigger code generation

```bash
./gradlew :shared:kspCommonMainKotlinMetadata
```

NavEase generates five files into:
```
shared/build/generated/ksp/metadata/commonMain/kotlin/
  io/github/alimsrepo/navease/generated/
    AppScreens.kt
    ScreenFactory.kt
    NavEaseExtensions.kt  ← navigateToXxx() + xxxArgs() typed extensions
    NavEaseResults.kt     ← only if any @NavEaseResult exists
    NavEaseHost.kt
```

---

## Quick Start

### Step 1 — Annotate your screens

Each screen is a class that:
1. Is annotated with `@NavEaseScreen`
2. Extends plain `NavScreen` (no generics — no IDE red underlines while writing)
3. Overrides the `Content()` composable

```kotlin
import io.github.alimsrepo.navease.runtime.annotations.NavEaseScreen
import io.github.alimsrepo.navease.runtime.domain.NavScreen
import io.github.alimsrepo.navease.runtime.navigation.NavController
import androidx.navigation3.runtime.NavKey
// Generated imports (available after first KSP build):
import io.github.alimsrepo.navease.generated.navigateToProfile
import io.github.alimsrepo.navease.generated.profileArgs

// ── Screen with no arguments ──────────────────────────────────────────────────

@NavEaseScreen(route = "Splash", startDestination = true)
class SplashScreen : NavScreen() {

    @Composable
    override fun Content(navKey: NavKey, navController: NavController) {
        LaunchedEffect(Unit) {
            delay(1_000)
            navController.navigateToHome()   // generated extension ✅
        }
        // … UI …
    }
}

// ── Screen with typed arguments ───────────────────────────────────────────────

@NavEaseScreen(route = "Profile")
class ProfileScreen : NavScreen() {

    @NavEaseArgs
    data class Args(val userId: String, val age: Int)

    @Composable
    override fun Content(navKey: NavKey, navController: NavController) {
        val args = navKey.profileArgs()   // generated extension — typed cast inside ✅
        Text("User: ${args.userId}, age: ${args.age}")
        Button(onClick = { navController.back() }) { Text("Back") }
    }
}
```

**Rules:**
- `@NavEaseScreen(route = "…")` — route name must be unique across all screens; becomes the sealed subclass name
- `startDestination = true` on exactly one screen — KSP sets this as the initial back-stack entry
- Extend plain `NavScreen` — no type parameter needed; `navKey: NavKey` always resolves without a build
- `@NavEaseArgs` — declare a nested `data class` — KSP generates a `data class` route instead of `data object`

### Step 2 — Launch `NavEaseHost`

```kotlin
// Android
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                NavEaseHost(
                    onExitRequest = { finish() }   // called when user presses back at root
                )
            }
        }
    }
}
```

```kotlin
// iOS (shared iosMain)
fun MainViewController() = ComposeUIViewController {
    MaterialTheme {
        NavEaseHost()   // no exit request needed on iOS
    }
}
```

```kotlin
// Desktop (JVM)
fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        MaterialTheme {
            NavEaseHost(onExitRequest = ::exitApplication)
        }
    }
}
```

That's it. Build and run.

---

## Annotation Reference

### `@NavEaseScreen`

```kotlin
@Target(AnnotationTarget.CLASS)
annotation class NavEaseScreen(
    val route: String,
    val startDestination: Boolean = false
)
```

| Parameter | Description |
|---|---|
| `route` | Unique name for this screen. Becomes the sealed subclass name in `AppScreens` |
| `startDestination` | `true` on exactly one screen. That screen is placed first on the back stack |

Applied to a class that extends plain `NavScreen`.

---

### `@NavEaseArgs`

```kotlin
@Target(AnnotationTarget.CLASS)
annotation class NavEaseArgs
```

Annotate a **nested `data class`** inside a `@NavEaseScreen` class to declare route arguments.

```kotlin
@NavEaseScreen(route = "Detail")
class DetailScreen : NavScreen() {

    @NavEaseArgs
    data class Args(val itemId: Int, val label: String)

    @Composable
    override fun Content(navKey: NavKey, navController: NavController) {
        val args = navKey.detailArgs()   // generated extension ✅
        Text("Item ${args.itemId}: ${args.label}")
    }
}
```

**Generated route:**
```kotlin
@Serializable data class Detail(val itemId: Int, val label: String) : AppScreens()
```

**Navigate with args (generated extension):**
```kotlin
navController.navigateToDetail(itemId = 42, label = "Hello")
```

Supported argument types: `String`, `Int`, `Long`, `Boolean`, `Double`, `Float`, and any custom `@Serializable` class (import is auto-generated).

---

### `@NavEaseResult`

```kotlin
@Target(AnnotationTarget.CLASS)
annotation class NavEaseResult
```

Annotate a **nested `data class`** inside a `@NavEaseScreen` class to declare the result this screen can return.

```kotlin
@NavEaseScreen(route = "ImagePicker")
class ImagePickerScreen : NavScreen() {

    @NavEaseResult
    data class Result(val imageUri: String, val success: Boolean)

    @Composable
    override fun Content(navKey: NavKey, navController: NavController) {
        Button(onClick = {
            navController.backWithImagePickerResult(
                imageUri = "content://…",
                success = true
            )
        }) { Text("Pick") }
    }
}
```

**What KSP generates:**
```kotlin
// Result holder
data class ImagePickerResult(val imageUri: String, val success: Boolean)

// On the child screen — call this instead of back()
fun NavController.backWithImagePickerResult(imageUri: String, success: Boolean)

// On the parent screen — observe the result
@Composable fun NavController.imagePickerResult(): State<ImagePickerResult?>
```

---

## NavController API

`NavController` is received as the second parameter in every screen's `Content()`.

```kotlin
class NavController {

    /** Navigate to a screen, pushing it onto the back stack. */
    fun navigate(navKey: NavKey, finish: Boolean = false, singleTop: Boolean = false)

    /**
     * Go back one screen. If at the root, calls the onExitRequest lambda
     * provided to NavEaseHost (e.g. finish the Activity).
     */
    fun back()

    /**
     * Pop the back stack until [key]'s screen is found. Matching is by
     * runtime class — argument values are ignored.
     *
     * @param inclusive When `true`, the destination screen is also removed.
     */
    fun popUpTo(key: NavKey, inclusive: Boolean = false)

    /**
     * Pop all screens down to [index] in the back stack (0 = root).
     * Use [getHistory] to find the right index first.
     */
    fun popToIndex(index: Int)

    /** Returns the current back stack as an ordered list, oldest first. */
    fun getHistory(): List<NavKey>
}
```

| Method | Description |
|---|---|
| `navigate(key)` | Push screen onto back stack |
| `navigate(key, finish = true)` | Push new screen and remove the current one (replace) |
| `navigate(key, singleTop = true)` | Push only if key class is not already the top screen |
| `back()` | Pop current screen; calls `onExitRequest` if at root |
| `popUpTo(key)` | Pop everything above the destination (non-inclusive) |
| `popUpTo(key, inclusive = true)` | Pop including the destination screen |
| `popToIndex(index)` | Pop everything above the given back-stack index |
| `getHistory()` | Inspect the current back stack |

---

## Back-with-Result

NavEase generates strongly-typed result extensions from `@NavEaseResult`.  
You do **not** use raw `backWithResult()` / `resultOf()` in application code — the generated typed extensions wrap them.

### Passing a result (child screen)

```kotlin
// Generated for a @NavEaseResult data class Result(val value: Int) in MainScreen
navController.backWithMainResult(value = 42)
```

This posts the result and calls `back()` in one step.

### Receiving a result (parent screen)

```kotlin
@NavEaseScreen(route = "Home")
class HomeScreen : NavScreen() {

    @Composable
    override fun Content(navKey: NavKey, navController: NavController) {
        // Generated for MainScreen's @NavEaseResult
        val result by navController.mainResult()

        result?.let {
            Text("Received: ${it.value}")
        }
    }
}
```

`mainResult()` returns a `State<MainResult?>` that:
- Is `null` until the child screen calls `backWithMainResult(…)`
- Becomes non-null exactly once (one-shot consume)
- Is scoped to the `NavController` instance — no cross-contamination between multiple hosts

---

## KSP-Generated Code

After running `./gradlew :shared:kspCommonMainKotlinMetadata`, NavEase writes these files for the sample app (2 screens: Splash, Main with args + result):

### `AppScreens.kt`

```kotlin
// ⚠️ AUTO-GENERATED BY NAVEASE KSP — DO NOT EDIT.

@Stable
@Serializable
sealed class AppScreens : NavKey {
    @Serializable data object Splash : AppScreens()
    @Serializable data class Main(val userId: String, val age: Int) : AppScreens()

    companion object {
        val startDestination: AppScreens get() = Splash

        val savedStateConfig = SavedStateConfiguration {
            serializersModule = SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(Splash::class)
                    subclass(Main::class)
                }
            }
        }
    }
}
```

### `ScreenFactory.kt`

```kotlin
object ScreenFactory {
    fun createScreen(appScreen: NavKey): NavScreen {
        return when (appScreen) {
            is AppScreens.Splash -> SplashScreen()
            is AppScreens.Main   -> MainScreen()
            else -> error("NavEase: No screen registered for key type '…'.")
        }
    }
}
```

### `NavEaseExtensions.kt`

```kotlin
// ── navigateToXxx() extensions on NavController ─────────────────────────────

fun NavController.navigateToSplash(finish: Boolean = false) {
    navigate(AppScreens.Splash, finish = finish)
}

fun NavController.navigateToMain(userId: String, age: Int, finish: Boolean = false) {
    navigate(AppScreens.Main(userId = userId, age = age), finish = finish)
}

// ── xxxArgs() extensions on NavKey ───────────────────────────────────────────

fun NavKey.mainArgs(): MainScreen.Args {
    val key = this as AppScreens.Main
    return MainScreen.Args(userId = key.userId, age = key.age)
}
```

### `NavEaseResults.kt`

```kotlin
data class MainResult(val value: Int)

fun NavController.backWithMainResult(value: Int) {
    backWithResult(MainResult(value))
}

@Composable
fun NavController.mainResult(): State<MainResult?> =
    resultOf(MainResult::class)
```

### `NavEaseHost.kt`

```kotlin
@Composable
fun NavEaseHost(onExitRequest: () -> Unit = {}) {
    AppNavGraph(
        initialScreen = AppScreens.startDestination,
        savedStateConfig = AppScreens.savedStateConfig,
        screenFactory = ScreenFactory::createScreen,
        onExitRequest = onExitRequest
    )
}
```

---

## Module Structure

```
NavEase/
├── navease-runtime/                    ← KMP library (all platforms via commonMain)
│   └── src/commonMain/kotlin/io/github/alimsrepo/navease/runtime/
│       ├── annotations/
│       │   ├── NavEaseScreen.kt        ← @NavEaseScreen annotation
│       │   ├── NavEaseArgs.kt          ← @NavEaseArgs annotation
│       │   └── NavEaseResult.kt        ← @NavEaseResult annotation
│       ├── domain/
│       │   └── NavScreen.kt            ← abstract class NavScreen (non-generic)
│       ├── navigation/
│       │   ├── NavController.kt        ← navigate / back / popUpTo / popToIndex / singleTop
│       │   └── NavControllerExtensions.kt ← backWithResult() / resultOf() extensions
│       └── presentation/
│           ├── NavEaseNavGraph.kt      ← @Composable NavEaseNavGraph(…) — core host
│           ├── LocalNavEaseController.kt ← CompositionLocal<NavController?>
│           └── Animations.kt           ← slide transition spec (450 ms)
│
├── navease-ksp/                        ← JVM KSP annotation processor
│   └── src/main/kotlin/io/github/alimsrepo/navease/ksp/
│       ├── NavEaseProcessor.kt         ← generates AppScreens, ScreenFactory, Extensions, Results, Host
│       └── NavEaseProcessorProvider.kt
│
├── shared/                             ← Sample app — KMP shared module
│   └── src/commonMain/kotlin/com/alim/navease/screens/
│       ├── App.kt                      ← MaterialTheme { NavEaseHost() }
│       ├── SplashScreen.kt             ← start destination, receives result
│       └── MainScreen.kt               ← args + result example
│
├── androidApp/                         ← Android sample entry point
├── desktopApp/                         ← Desktop (JVM) sample entry point
├── webApp/                             ← Web (JS/WASM) sample entry point
└── iosApp/                             ← iOS sample entry point (Xcode project)
```

---

## Sample App

The `shared` module contains a 2-screen sample demonstrating the full NavEase lifecycle:

| Screen | Route | `startDestination` | Demonstrates |
|---|---|---|---|
| `SplashScreen` | `"Splash"` | ✅ yes | Auto-navigation with `LaunchedEffect` + receiving a typed result |
| `MainScreen` | `"Main"` | ❌ no | `@NavEaseArgs` (userId, age) + `@NavEaseResult` (value) + `backWithMainResult()` |

**Run on Android:**
```bash
./gradlew :androidApp:installDebug
```

**Run on Desktop:**
```bash
./gradlew :desktopApp:run
```

---

## FAQ

**Q: Do I need to manually register screens anywhere?**  
No. Annotate your class with `@NavEaseScreen` and rebuild. KSP updates `AppScreens` and `ScreenFactory` automatically.

**Q: What happens when I add a new screen?**  
Add `@NavEaseScreen` to your class, add `@NavEaseArgs` / `@NavEaseResult` if needed, and run `./gradlew :shared:kspCommonMainKotlinMetadata`. The factory regenerates.

**Q: How does the `startDestination` work?**  
Exactly one screen class should have `startDestination = true`. KSP sets `AppScreens.startDestination` to that route. If none is marked, KSP picks the first screen it encounters (non-deterministic — always mark one explicitly).

**Q: Can I use NavEase without KSP?**  
Yes. Implement `AppNavGraph` directly with your own `screenFactory` lambda and manage `AppScreens` manually. This is useful for testing or unusual setups.

**Q: Can I have nested navigation (e.g. bottom tabs)?**  
Each `AppNavGraph` call creates an independent `NavController` and back stack. Place multiple `AppNavGraph` composables side-by-side for parallel nav graphs.

**Q: How do I handle Android back-press at the root?**  
Pass an `onExitRequest` lambda to `NavEaseHost`:
```kotlin
NavEaseHost(onExitRequest = { finish() })
```
For a confirmation dialog, show it inside `onExitRequest` and only call `finish()` on confirm.

**Q: How do I access NavController from a deeply nested composable?**  
Use `LocalNavEaseController` — it is provided automatically by `NavEaseHost`:
```kotlin
import io.github.alimsrepo.navease.runtime.presentation.LocalNavEaseController

@Composable
fun DeepNestedWidget() {
    val navController = LocalNavEaseController.current ?: return
    Button(onClick = { navController.back() }) { Text("Back") }
}
```

**Q: Does NavEase support deep links?**  
Not natively in the current version. Parse your `Intent` URI manually and call `navController.navigate(AppScreens.SomeScreen(…))` from your `MainActivity`.

**Q: What argument types can I use in `@NavEaseArgs`?**  
Primitives (`String`, `Int`, `Long`, `Boolean`, `Double`, `Float`) are handled automatically. Custom classes are supported — NavEase generates the correct `import` for them — but they must be `@Serializable` so the back stack can survive process death.

---

## License

```
Copyright 2026 NavEase Contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0
```
