# NavEase

**NavEase** is a KMP navigation library for **Kotlin Multiplatform + Compose Multiplatform** that offers three levels of integration — pick the one that fits your workflow.

No reflection. No string routes. No red underlines while writing.

> ✅ **Status:** published to Maven Central — latest version: **0.1.2**

---

## Table of Contents

- [Features](#features)
- [Three Approaches](#three-approaches)
- [Platform Support](#platform-support)
- [Setup](#setup)
- [Approach 1 — KSP Annotations](#approach-1--ksp-annotations)
- [Approach 2 — navEaseGraph DSL](#approach-2--naveasegraph-dsl)
- [Approach 3 — ActivityScreen\<K\>](#approach-3--activityscreenk)
- [NavController API](#navcontroller-api)
- [Back-with-Result](#back-with-result)
- [Shared Element Transitions](#shared-element-transitions)
- [KSP-Generated Code](#ksp-generated-code)
- [Module Structure](#module-structure)
- [Sample App](#sample-app)
- [FAQ](#faq)
- [License](#license)

---

## Features

| Feature | KSP Annotations | navEaseGraph DSL | ActivityScreen\<K\> |
|---|:---:|:---:|:---:|
| **Zero boilerplate** | ✅ KSP-generated | ✅ Pure Kotlin DSL | ✅ Class registration |
| **Typed navKey** | `navKey.xxxArgs()` cast | `(key: K) -> …` lambda | `navKey: K` parameter |
| **Typed arguments** | `@NavEaseArgs` → `xxxArgs()` | Direct field access on `K` | Direct field access on `K` |
| **Typed results** | `@NavEaseResult` → generated | `backWithResult()` / `resultOf<T>()` | `backWithResult()` / `resultOf<T>()` |
| **Rebuild required** | After every new screen | ❌ Never | ❌ Never |
| **IDE navigation** | Generated extensions | Sealed-class members | Sealed-class members |
| **Shared transitions** | ✅ | ✅ | ✅ |
| **State restoration** | ✅ Auto-generated | ✅ Auto-built | ✅ Auto-built |
| **Back stack** | ✅ Navigation3 | ✅ Navigation3 | ✅ Navigation3 |

---

## Three Approaches

```
┌─────────────────────────────────────────────────────────────────────────┐
│  Approach 1 — KSP Annotations                                           │
│  @NavEaseScreen / @NavEaseArgs / @NavEaseResult → KSP generates         │
│  AppScreens, ScreenFactory, navigateToXxx(), xxxArgs(), xxxResult()     │
│  ✓ Zero manual boilerplate after annotation                              │
│  ✗ Requires a rebuild after adding / changing a screen                   │
├─────────────────────────────────────────────────────────────────────────┤
│  Approach 2 — navEaseGraph DSL                                           │
│  navEaseGraph { screen<K> { … } } + NavEaseHost(graph)                  │
│  ✓ No rebuild ever — IDE sees types immediately                          │
│  ✓ Typed lambda receives K — direct field access                         │
│  ✓ Back-with-result via backWithResult() / resultOf<T>()                 │
├─────────────────────────────────────────────────────────────────────────┤
│  Approach 3 — ActivityScreen<K>                                          │
│  class MyScreen : ActivityScreen<AppScreens.MyKey>() { … }              │
│  NavEaseHost<AppScreens>(start) { add(MyScreen()) }                     │
│  ✓ Class-based — familiar OOP structure                                  │
│  ✓ Typed navKey in Content() — no casting, no extensions                 │
│  ✓ Back-with-result via nested Result data class                         │
└─────────────────────────────────────────────────────────────────────────┘
```

All three share the same `NavController`, shared-element support, and `NavDisplay` engine.

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

> **TL;DR — use the Gradle plugin** to skip all the boilerplate below.

### Recommended: Gradle plugin (zero boilerplate)

```kotlin
// shared/build.gradle.kts
plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("io.github.alims-repo.navease") version "0.1.2"  // ← all wiring done ✅
}
```

The `navease` plugin automatically:
- applies the KSP and Kotlin Serialization compiler plugins
- adds `navease-ksp` to `kspCommonMainMetadata`
- registers `build/generated/ksp/metadata/commonMain/kotlin` as a `commonMain` srcDir
- makes every KMP compilation depend on `kspCommonMainKotlinMetadata`
- adds `navease-runtime` to `commonMain` dependencies

Optional configuration via the `navease { }` extension:
```kotlin
navease {
    version = "0.1.2"                        // pin a specific version (default: same as plugin)
    addRuntimeDependency = true              // set false to manage navease-runtime yourself
    generatedPackage = "com.myapp.nav"       // custom package for generated files
}
```

> For Approach 2 (`navEaseGraph`) and Approach 3 (`ActivityScreen<K>`) **only `navease-runtime` is required** — no KSP needed. If you don't use annotations, omit the KSP plugin and the `ksp` dependency.

---

### Manual setup (if you prefer full control)

#### 1. Apply plugins in your shared KMP module

```kotlin
// shared/build.gradle.kts
plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")   // only needed for Approach 1
}
```

#### 2. Add dependencies

```kotlin
// shared/build.gradle.kts
kotlin {
    sourceSets {
        commonMain {
            // Point KSP metadata output to commonMain — only required for Approach 1:
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")

            dependencies {
                implementation("io.github.alims-repo:navease-runtime:0.1.2")
            }
        }
    }
}

// Only required for Approach 1:
dependencies {
    add("kspCommonMainMetadata", "io.github.alims-repo:navease-ksp:0.1.2")
}

// Only required for Approach 1:
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>>().configureEach {
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}
```

#### 3. (Optional) Configure KSP options

```kotlin
ksp {
    // Override the package for all generated files.
    // Default: "io.github.alimsrepo.navease.generated"
    arg("navease.generatedPackage", "com.myapp.navigation.generated")
}
```

---

## Approach 1 — KSP Annotations

The **fully automated** approach: annotate your screen classes and let KSP generate everything.

### Step 1 — Annotate your screens

```kotlin
import io.github.alimsrepo.navease.runtime.annotations.NavEaseScreen
import io.github.alimsrepo.navease.runtime.domain.NavScreen
import io.github.alimsrepo.navease.runtime.navigation.NavController
import androidx.navigation3.runtime.NavKey
// Generated imports (available after first KSP build):
import io.github.alimsrepo.navease.generated.navigateToMain
import io.github.alimsrepo.navease.generated.mainArgs

// ── Screen with no arguments ──────────────────────────────────────────────────

@NavEaseScreen(route = "Splash", startDestination = true)
class SplashScreen : NavScreen() {

    @Composable
    override fun Content(navKey: NavKey, navController: NavController) {
        LaunchedEffect(Unit) {
            delay(1_000)
            navController.navigateToMain(userId = "alim", age = 28)   // generated ✅
        }
    }
}

// ── Screen with typed arguments ───────────────────────────────────────────────

@NavEaseScreen(route = "Main")
class MainScreen : NavScreen() {

    @NavEaseArgs
    data class Args(val userId: String, val age: Int)

    @NavEaseResult
    data class Result(val updated: Boolean)

    @Composable
    override fun Content(navKey: NavKey, navController: NavController) {
        val args = navKey.mainArgs()   // generated extension ✅
        Text("Hello, ${args.userId}!")
        Button(onClick = { navController.backWithMainResult(updated = true) }) {
            Text("Save")
        }
    }
}
```

**Annotation rules:**
- `@NavEaseScreen(route = "…")` — unique name per screen; becomes the sealed subclass name
- `startDestination = true` on exactly one screen
- Extend plain `NavScreen` — no type parameter needed
- `@NavEaseArgs` on a nested `data class` — KSP adds those fields to the sealed subclass
- `@NavEaseResult` on a nested `data class` — KSP generates `backWithXxxResult()` + `xxxResult()`

### Step 2 — Trigger code generation

```bash
./gradlew :shared:kspCommonMainKotlinMetadata
```

### Step 3 — Launch `NavEaseHost`

```kotlin
// commonMain
@Composable
fun App() {
    MaterialTheme {
        NavEaseHost(enableSharedTransitions = true)
    }
}

// Android
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                NavEaseHost(onExitRequest = { finish() })
            }
        }
    }
}
```

---

## Approach 2 — navEaseGraph DSL

The **zero-rebuild** alternative. Define a plain Kotlin sealed class for your routes and wire screens in a DSL block — no annotations, no code generation, no rebuild required after adding a screen.

### Step 1 — Define NavKeys

```kotlin
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed class AppScreen : NavKey {
    @Serializable data object Home   : AppScreen()
    @Serializable data object About  : AppScreen()
    @Serializable data class  Detail(val id: String) : AppScreen()
}
```

### Step 2 — Build the graph

```kotlin
import io.github.alimsrepo.navease.runtime.presentation.navEaseGraph

val appGraph = navEaseGraph(start = AppScreen.Home) {
    screen<AppScreen.Home>   { HomeScreen() }
    screen<AppScreen.About>  { AboutScreen() }
    screen<AppScreen.Detail> { key -> DetailScreen(id = key.id) }
    //                         ^^^  typed — direct field access, no casting
}
```

`screen<K>` receives the typed `K` key so you can forward route arguments directly.

### Step 3 — Host the graph

```kotlin
@Composable
fun App() {
    MaterialTheme {
        NavEaseHost(
            graph = appGraph,
            enableSharedTransitions = true,
        )
    }
}
```

### Step 4 — Navigate

```kotlin
val nav = LocalNavEaseController.current ?: return

nav.navigate(AppScreen.Detail(id = "abc"))
nav.navigate(AppScreen.Home, finish = true)       // replace current screen
nav.navigate(AppScreen.About, singleTop = true)   // skip if already on top
```

### Back-with-Result (navEaseGraph)

```kotlin
data class DetailResult(val confirmed: Boolean)

// Child screen:
nav.backWithResult(DetailResult(confirmed = true))

// Parent screen:
val result by nav.resultOf<DetailResult>()
result?.let { Text("Confirmed: ${it.confirmed}") }
```

---

## Approach 3 — ActivityScreen\<K\>

The **class-based, typed** approach. Each screen extends `ActivityScreen<K>` where `K` is the specific `NavKey` subclass it handles — `Content()` receives a fully-typed navKey, no casting or generated extensions needed.

### Step 1 — Define NavKeys

Same sealed class as Approach 2:

```kotlin
@Serializable
sealed class AppScreens : NavKey {
    @Serializable data object Home   : AppScreens()
    @Serializable data object About  : AppScreens()
    @Serializable data class  Detail(val id: String) : AppScreens()
}
```

**Optional @AutoRegister path (no @Serializable needed):**

If you annotate screens with `@AutoRegister`, NavEase generates serializers internally.
You can skip `@Serializable` and extend `NavEaseRoot` instead of `NavKey`:

```kotlin
import io.github.alimsrepo.navease.runtime.NavEaseRoot

sealed class AppScreens : NavEaseRoot {
    data object Home   : AppScreens()
    data object About  : AppScreens()
    data class  Detail(val id: String) : AppScreens()
}
```

### Step 2 — Define screens

```kotlin
import io.github.alimsrepo.navease.runtime.presentation.ActivityScreen

class HomeScreen : ActivityScreen<AppScreens.Home>() {

    @Composable
    override fun Content(navKey: AppScreens.Home, navController: NavController) {
        Button(onClick = { navController.navigate(AppScreens.Detail(id = "abc")) }) {
            Text("Open Detail")
        }
    }
}

class DetailScreen : ActivityScreen<AppScreens.Detail>() {

    @Composable
    override fun Content(navKey: AppScreens.Detail, navController: NavController) {
        Text(navKey.id)   // ← typed! direct access, no cast, no xxxArgs()
        Button(onClick = { navController.back() }) { Text("Back") }
    }
}
```

### Step 3 — Host with NavEaseHost\<Root\>

```kotlin
@Composable
fun App() {
    MaterialTheme {
        NavEaseHost<AppScreens>(
            start = AppScreens.Home,
            enableSharedTransitions = true,
        ) {
            add(HomeScreen())
            add(AboutScreen())
            add(DetailScreen())
        }
    }
}
```

`add(Screen())` registers each screen — the reified type captures `K` automatically from the class's type parameter.

### Back-with-Result (ActivityScreen\<K\>)

Nest the result class inside the child screen for clear ownership:

```kotlin
// ── Child screen ──────────────────────────────────────────────────────────────

class ProfileScreen : ActivityScreen<AppScreens.Profile>() {

    data class Result(val selectedUserId: Int)

    @Composable
    override fun Content(navKey: AppScreens.Profile, navController: NavController) {
        Button(onClick = {
            navController.backWithResult(Result(selectedUserId = 42))
        }) { Text("Select User") }
    }
}

// ── Parent screen ─────────────────────────────────────────────────────────────

class HomeScreen : ActivityScreen<AppScreens.Home>() {

    @Composable
    override fun Content(navKey: AppScreens.Home, navController: NavController) {

        val result by navController.resultOf<ProfileScreen.Result>()

        result?.let { Text("Selected user #${it.selectedUserId}") }

        Button(onClick = { navController.navigate(AppScreens.Profile) }) {
            Text("Open Profile")
        }
    }
}
```

---

## NavController API

`NavController` is the same across all three approaches. It is received as a parameter in `Content()` and is also available via `LocalNavEaseController.current` from any nested composable.

```kotlin
class NavController {

    /** Navigate to a screen, pushing it onto the back stack. */
    fun navigate(navKey: NavKey, finish: Boolean = false, singleTop: Boolean = false, navTransition: NavTransition? = null)

    /** Go back one screen. If at the root, calls the onExitRequest lambda. */
    fun back()

    /** Pop until [key]'s screen. Matching is by runtime class — args ignored. */
    fun popUpTo(key: NavKey, inclusive: Boolean = false)

    /** Pop all screens down to [index] in the back stack (0 = root). */
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
| `navigate(key, navTransition = NavTransition.Fade)` | Override the transition for this navigation only |
| `back()` | Pop current screen; calls `onExitRequest` if at root |
| `popUpTo(key)` | Pop everything above the destination (non-inclusive) |
| `popUpTo(key, inclusive = true)` | Pop including the destination screen |
| `popToIndex(index)` | Pop everything above the given back-stack index |
| `getHistory()` | Inspect the current back stack |

### Per-navigate transitions

```kotlin
navController.navigate(
    AppScreens.Detail(id = "abc"),
    navTransition = NavTransition.Fade,   // only this navigation uses Fade
)
```

Available transitions: `Push` (iOS-style slide) · `Fade` · `Rise` (slide up) · `Zoom` · `Depth` · `Instant`

---

## Back-with-Result

NavEase provides a type-safe back-with-result pattern across all three approaches. Results are scoped to the `NavController` instance — no global state, no cross-contamination.

### Low-level API (all approaches)

```kotlin
// Child screen — post a result before popping:
navController.backWithResult(MyResult(value = 42))

// Parent screen — observe the result (consumed exactly once):
val result by navController.resultOf<MyResult>()
```

`resultOf<T>()` returns a `State<T?>` backed by a `SnapshotStateMap` inside `NavController`. Recomposition happens automatically the frame the child calls `backWithResult()`. The value is consumed in the same frame via `SideEffect` — no one-frame null window.

### Approach 1 — KSP-generated typed extensions

KSP wraps the low-level API with named functions so you never touch `backWithResult()` / `resultOf()` directly:

```kotlin
// Child screen (from @NavEaseResult data class Result(val starred: Boolean)):
navController.backWithLibraryDetailResult(starred = true)

// Parent screen:
val result by navController.libraryDetailResult()   // State<LibraryDetailResult?>
result?.let { if (it.starred) Text("⭐ Starred!") }
```

### Approach 2 — navEaseGraph DSL

```kotlin
data class PickerResult(val imageUri: String)

// Child:
navController.backWithResult(PickerResult(imageUri = "content://…"))

// Parent:
val pickerResult by navController.resultOf<PickerResult>()
```

### Approach 3 — ActivityScreen\<K\>

```kotlin
class ImagePickerScreen : ActivityScreen<AppScreens.ImagePicker>() {
    data class Result(val imageUri: String)
    // …
    navController.backWithResult(Result(imageUri = "content://…"))
}

// Parent:
val pickerResult by navController.resultOf<ImagePickerScreen.Result>()
```

---

## Shared Element Transitions

NavEase has **optional** shared element transition support. It is **off by default** — zero overhead when not used.

### Enable shared transitions

```kotlin
// All three approaches accept the same parameter:
NavEaseHost(enableSharedTransitions = true)                              // Approach 1
NavEaseHost(graph, enableSharedTransitions = true)                      // Approach 2
NavEaseHost<Root>(start, enableSharedTransitions = true) { … }         // Approach 3
```

When `true`, NavEase wraps `NavDisplay` in a `SharedTransitionLayout` and provides the scope via `LocalNavEaseSharedTransitionScope`.

### Use shared elements in screens

```kotlin
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import io.github.alimsrepo.navease.runtime.presentation.LocalNavEaseSharedTransitionScope

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MyCard(username: String) {
    // Both locals return null when enableSharedTransitions = false (safe)
    val sharedScope   = LocalNavEaseSharedTransitionScope.current
    val animatedScope = LocalNavAnimatedContentScope.current

    val avatarModifier = if (sharedScope != null && animatedScope != null) {
        with(sharedScope) {
            Modifier.sharedBounds(
                sharedContentState = rememberSharedContentState(key = "avatar_$username"),
                animatedVisibilityScope = animatedScope,
            )
        }
    } else Modifier

    Box(modifier = avatarModifier.size(88.dp)) { /* avatar */ }
}
```

**Rules for shared element keys:**
- Keys are plain `Any` values — use a string like `"avatar_$username"` or a data class
- The key must match **exactly** between the source and destination screen
- Use `sharedBounds` for containers that change size/shape; use `sharedElement` for same-size content

---

## KSP-Generated Code

**Approach 1 only.** After running `./gradlew :shared:kspCommonMainKotlinMetadata`, NavEase writes five files:

```
shared/build/generated/ksp/metadata/commonMain/kotlin/
  io/github/alimsrepo/navease/generated/
    AppScreens.kt         ← @Serializable sealed class NavKey hierarchy
    ScreenFactory.kt      ← maps NavKey → NavScreen
    NavEaseExtensions.kt  ← navigateToXxx() + xxxArgs() typed extensions
    NavEaseResults.kt     ← result data classes + backWithXxxResult() + xxxResult()
    NavEaseHost.kt        ← @Composable NavEaseHost(…) entry point
```

### `AppScreens.kt`

```kotlin
@Stable
@Serializable
sealed class AppScreens : NavKey {
    @Serializable data object Splash : AppScreens()
    @Serializable data object Home : AppScreens()
    @Serializable data class LibraryDetail(val libId: String, val libName: String) : AppScreens()
    // … one subclass per @NavEaseScreen

    companion object {
        val startDestination: AppScreens get() = Splash
        val savedStateConfig = SavedStateConfiguration { … }
    }
}
```

### `NavEaseExtensions.kt`

```kotlin
fun NavController.navigateToHome(finish: Boolean = false, navTransition: NavTransition? = null) { … }
fun NavController.navigateToLibraryDetail(
    libId: String, libName: String,
    finish: Boolean = false, navTransition: NavTransition? = null
) { … }

fun NavKey.libraryDetailArgs(): LibraryDetailScreen.Args { … }
```

### `NavEaseResults.kt`

```kotlin
data class LibraryDetailResult(val starred: Boolean)

fun NavController.backWithLibraryDetailResult(starred: Boolean) { … }

@Composable fun NavController.libraryDetailResult(): State<LibraryDetailResult?> = …
```

### `NavEaseHost.kt`

```kotlin
@Composable
fun NavEaseHost(
    onExitRequest: () -> Unit = {},
    enableSharedTransitions: Boolean = false,
    navTransition: NavTransition = NavTransition.Push,
) {
    NavEaseNavGraph(
        initialScreen = AppScreens.startDestination,
        savedStateConfig = AppScreens.savedStateConfig,
        screenFactory = ScreenFactory::createScreen,
        onExitRequest = onExitRequest,
        enableSharedTransitions = enableSharedTransitions,
        navTransition = navTransition,
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
│       │   ├── NavEaseScreen.kt        ← @NavEaseScreen annotation (Approach 1)
│       │   ├── NavEaseArgs.kt          ← @NavEaseArgs annotation (Approach 1)
│       │   ├── NavEaseResult.kt        ← @NavEaseResult annotation (Approach 1)
│       │   └── AutoRegister.kt         ← @AutoRegister annotation (Approach 3 auto-discover)
│       ├── NavEaseRoot.kt              ← hides NavKey + @Serializable for @AutoRegister keys
│       ├── domain/
│       │   └── NavScreen.kt            ← abstract class NavScreen — Approach 1 base
│       ├── navigation/
│       │   ├── NavController.kt        ← navigate / back / popUpTo / popToIndex / singleTop
│       │   └── NavControllerExtensions.kt ← backWithResult() / resultOf<T>() — all approaches
│       └── presentation/
│           ├── ActivityScreen.kt       ← ActivityScreen<K> — Approach 3 base class
│           ├── NavEaseGraph.kt         ← NavEaseGraph / navEaseGraph() DSL — Approach 2
│           ├── NavEaseHost.kt          ← NavEaseHost overloads (graph DSL + ActivityScreen)
│           ├── NavEaseNavGraph.kt      ← core host (KSP + graph DSL variants)
│           ├── NavTransition.kt        ← Push / Fade / Rise / Zoom / Depth / Instant
│           ├── Animations.kt           ← ContentTransform pairs for each NavTransition
│           ├── LocalNavEaseController.kt   ← CompositionLocal<NavController?>
│           └── LocalNavEaseSharedTransition.kt ← CompositionLocal<SharedTransitionScope?>
│
├── navease-ksp/                        ← JVM KSP processor (Approach 1 only)
│   └── src/main/kotlin/io/github/alimsrepo/navease/ksp/
│       ├── NavEaseProcessor.kt         ← generates AppScreens, ScreenFactory, Extensions, Results, Host
│       └── NavEaseProcessorProvider.kt
│
├── navease-gradle-plugin/              ← Gradle plugin (id: io.github.alims-repo.navease)
│   └── src/main/kotlin/io/github/alimsrepo/navease/gradle/
│       ├── NavEasePlugin.kt            ← Plugin<Project> — wires KSP, srcDir, task deps
│       ├── NavEaseExtension.kt         ← navease { } DSL extension block
│       └── NavEaseVersion.kt           ← bundled artifact version constants
│
├── shared/                             ← Sample app — KMP shared module (Approach 1 demo)
│   └── src/commonMain/kotlin/com/alim/navease/screens/
│       ├── App.kt                      ← MaterialTheme { NavEaseHost(enableSharedTransitions = true) }
│       ├── LibraryData.kt              ← Library data class + allLibraries catalogue
│       ├── ThemeUtils.kt               ← containerColorAt / accentColorAt helpers
│       ├── SharedComposables.kt        ← NavBackButton shared composable
│       ├── SplashScreen.kt             ← start destination; animated branding; navigates to Home
│       ├── HomeScreen.kt               ← library catalogue list; shared bounds; @NavEaseResult banner
│       ├── LibraryDetailScreen.kt      ← @NavEaseArgs + @NavEaseResult; sharedBounds; per-lib navTransition
│       ├── NavEaseDemoScreen.kt        ← 6-transition picker; per-navigate NavTransition override
│       ├── TransitionPreviewScreen.kt  ← @NavEaseArgs; live transition preview
│       ├── SecureVaultDemoScreen.kt    ← SecureVault KMP library demo
│       ├── FlowTabDemoScreen.kt        ← FlowTab CMP library demo
│       ├── PrayerTimesDemoScreen.kt    ← Prayer Times KMM library demo
│       ├── CrashGuardDemoScreen.kt     ← CrashGuard library demo
│       └── PdfDemoScreen.kt            ← Pdf Generator library demo
│
├── androidApp/                         ← Android sample entry point
├── desktopApp/                         ← Desktop (JVM) sample entry point
├── webApp/                             ← Web (JS/WASM) sample entry point
└── iosApp/                             ← iOS sample entry point (Xcode project)
```

---

## Sample App

The `shared` module contains a 10-screen demo app — the **alims-repo library catalogue** — showcasing Approach 1 (KSP annotations) with shared element transitions, typed arguments, typed results, and per-navigate transition overrides.

| Screen | Route | `startDestination` | Demonstrates |
|---|---|---|---|
| `SplashScreen` | `"Splash"` | ✅ yes | Animated branding · auto-navigate with `finish = true` |
| `HomeScreen` | `"Home"` | ❌ no | Library list · shared bounds · `@NavEaseResult` observe banner |
| `LibraryDetailScreen` | `"LibraryDetail"` | ❌ no | `@NavEaseArgs` · `@NavEaseResult` · `sharedBounds` from home card · per-lib `navTransition` |
| `NavEaseDemoScreen` | `"NavEaseDemo"` | ❌ no | 6-transition picker · per-navigate `NavTransition` override |
| `TransitionPreviewScreen` | `"TransitionPreview"` | ❌ no | `@NavEaseArgs` · live transition preview |
| `SecureVaultDemoScreen` | `"SecureVaultDemo"` | ❌ no | Library feature demo |
| `FlowTabDemoScreen` | `"FlowTabDemo"` | ❌ no | Library feature demo |
| `PrayerTimesDemoScreen` | `"PrayerTimesDemo"` | ❌ no | Library feature demo |
| `CrashGuardDemoScreen` | `"CrashGuardDemo"` | ❌ no | Library feature demo |
| `PdfDemoScreen` | `"PdfDemo"` | ❌ no | Library feature demo |

### Navigation flow

```
SplashScreen ──(auto, finish=true)──▶ HomeScreen ──(shared bounds)──▶ LibraryDetailScreen
                                                                              │
                                           ┌──────────────────────────────────┤
                                           ▼                                  │
                                     NavEaseDemoScreen                        │
                                           │                                  │
                                           ▼                                  │
                                 TransitionPreviewScreen              demo screens…
                                                            (SecureVaultDemo, FlowTabDemo,
                                                             PrayerTimesDemo, CrashGuardDemo,
                                                             PdfDemo)
```

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

**Q: Which approach should I choose?**

| If you want… | Use |
|---|---|
| Maximum automation, typed extensions, no manual route management | Approach 1 — KSP Annotations |
| No annotation processing, instant IDE feedback, functional style | Approach 2 — navEaseGraph DSL |
| Class-based OOP, typed navKey in Content(), no code generation | Approach 3 — ActivityScreen\<K\> |

All three co-exist — you can mix them or progressively migrate.

---

**Q: Do I need to manually register screens anywhere?**  
- **Approach 1:** No. Annotate with `@NavEaseScreen` and rebuild.
- **Approach 2:** Register with `screen<K> { … }` in the `navEaseGraph` block.
- **Approach 3:** Register with `add(ScreenInstance())` in the `NavEaseHost<Root> { }` block.

**Q: What happens when I add a new screen?**  
- **Approach 1:** Add `@NavEaseScreen`, run `./gradlew :shared:kspCommonMainKotlinMetadata`.
- **Approach 2/3:** Add a subclass to your sealed class, register it — no build step required.

**Q: Can I have nested navigation (e.g. bottom tabs)?**  
Yes. Each `NavEaseNavGraph` / `NavEaseHost` call creates an independent `NavController` and back stack. Place multiple hosts side-by-side for parallel nav graphs.

**Q: How do I handle Android back-press at the root?**

```kotlin
NavEaseHost(onExitRequest = { finish() })
```

For a confirmation dialog, show it inside `onExitRequest` and only call `finish()` on confirm.

**Q: How do I access NavController from a deeply nested composable?**

```kotlin
@Composable
fun DeepNestedWidget() {
    val navController = LocalNavEaseController.current ?: return
    Button(onClick = { navController.back() }) { Text("Back") }
}
```

**Q: What argument types can I use in `@NavEaseArgs` (Approach 1)?**  
Primitives (`String`, `Int`, `Long`, `Boolean`, `Double`, `Float`) work automatically. Custom types must be `@Serializable`.

**Q: Can NavEase be used without KSP (Approaches 2 and 3)?**  
Yes — only `navease-runtime` is required. No KSP plugin, no annotation processor dependency.

**Q: Are shared element transitions required?**  
No. `enableSharedTransitions` defaults to `false`. When false, `LocalNavEaseSharedTransitionScope` returns `null` — zero overhead.

**Q: Do shared element transitions work on iOS/Desktop/Web?**  
Yes. `SharedTransitionLayout` is part of Compose Multiplatform and works on all supported platforms.

**Q: How does back-with-result work internally?**  
Results are stored in a `SnapshotStateMap` inside `NavController`. Writing a result triggers recomposition. `resultOf<T>()` reads the map reactively via `derivedStateOf` and removes the entry via `SideEffect` in the same frame — no one-frame null window.

**Q: Can I mix approaches in the same project?**  
Yes. Approach 1 uses KSP-generated `NavEaseHost()` with `AppScreens`. Approaches 2 and 3 use their own sealed classes and `NavEaseHost` overloads. Each approach gets its own independent `NavController` instance.

---

## License

```
Copyright 2026 NavEase Contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0
```
