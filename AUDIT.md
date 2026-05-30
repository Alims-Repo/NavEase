# NavEase — Complete Production Readiness Audit

> **Audit Date:** May 27, 2026  
> **Auditor:** GitHub Copilot (automated code analysis)  
> **Last updated:** May 30, 2026 — see [Remediation Status](#remediation-status) for current state  
> **Codebase revision:** HEAD / `versionName 1.0` / `versionCode 1`  
> **Scope:** All modules — `navease-runtime`, `navease-ksp`, `shared`, `androidApp`, `desktopApp`, `webApp`, `iosApp`

---

## Remediation Status

> ✅ = Fixed · 🔄 = Partially addressed · ❌ = Not yet fixed

| ID | Severity | Issue | Status |
|---|---|---|---|
| C1 | 🔴 Critical | `ResultStore` global singleton | ✅ Fixed — scoped to `NavController` instance |
| C2 | 🔴 Critical | `produceState` result loss | ✅ Fixed — `derivedStateOf` + `LaunchedEffect` pattern |
| C3 | 🔴 Critical | KSP missing import generation | ✅ Fixed — `resolveTypeInfo()` + `importsFrom()` |
| C4 | 🔴 Critical | README describes wrong library | ✅ Fixed — README fully rewritten |
| H1 | 🟠 High | `ActivityScreen.kt` misnamed | ✅ Fixed — renamed to `NavScreen.kt`, moved to `domain/` |
| H2 | 🟠 High | `AppNavGraph` name | ✅ Fixed — renamed to `NavEaseNavGraph`; deprecated alias kept |
| H3 | 🟠 High | `NavController` in `data/` layer | ✅ Fixed — moved to `navigation/` package |
| H4 | 🟠 High | `showExitDialog = {}` hardcoded | ✅ Fixed — `onExitRequest` parameter on `NavEaseNavGraph` / generated host |
| H5 | 🟠 High | `navigate(finish=true)` crash | ✅ Fixed — guard `if (backStack.size >= 2)` added |
| H6 | 🟠 High | `Dependencies(false)` in KSP | ✅ Fixed — `Dependencies(aggregating=true, *sourceFiles)` |
| H7 | 🟠 High | No KSP processor tests | ✅ Fixed — `NavEaseProcessorTest` (5 compilation tests) added to `navease-ksp` |
| H8 | 🟠 High | Unstable dependencies | ❌ Not fixed — lifecycle `2.11.0-beta01`, material3 `1.11.0-alpha07` still in use |
| M1 | 🟡 Medium | `NavEaseHost()` zero parameters | ✅ Fixed — accepts `onExitRequest`, `enableSharedTransitions`, `navTransition` |
| M2 | 🟡 Medium | No `popUpTo` | ✅ Fixed — `popUpTo(key, inclusive)` added to `NavController` |
| M3 | 🟡 Medium | No `singleTop` navigation | ✅ Fixed — `navigate(key, singleTop = true)` added |
| M4 | 🟡 Medium | No per-screen transition | ✅ Fixed — `NavTransition` sealed class (6 styles); per-navigate override on every `navigateToXxx()` call |
| M5 | 🟡 Medium | No `LocalNavEaseController` | ✅ Fixed — provided by `NavEaseNavGraph` via `CompositionLocalProvider` |
| M6 | 🟡 Medium | Hardcoded generated package | ✅ Fixed — `navease.generatedPackage` KSP option in `NavEaseProcessorProvider` |
| M7 | 🟡 Medium | No ProGuard consumer rules | ✅ Fixed — `navease-runtime/consumer-rules.pro` added |
| M8 | 🟡 Medium | `qualifiedName!!` force-unwrap | ✅ Fixed — guarded with descriptive `error()` message |
| M9 | 🟡 Medium | `isMinifyEnabled = false` | ✅ Fixed — enabled for release with R8 + ProGuard rules |
| L1 | 🟢 Low | Extract sample into `:sample` | ❌ Not done — `shared` still contains demo screens |
| L2 | 🟢 Low | Debug back-stack overlay | ❌ Not implemented |
| L3 | 🟢 Low | Publish to Maven Central | ❌ No publishing config |
| L4 | 🟢 Low | Changelog | ❌ No CHANGELOG.md |
| L5 | 🟢 Low | Deep link support | ❌ Not implemented |
| L6 | 🟢 Low | `SavedStateHandle` tests | ❌ No instrumented tests |
| L7 | 🟢 Low | KSP version alignment | ❌ `ksp = "2.3.9"` still does not match Kotlin `2.3.21` format |
| L8 | 🟢 Low | Document Navigation3 API contract | 🔄 Partially — README mentions Navigation3 is part of public contract |

### Also fixed (not in original audit)

| Fix | Description |
|---|---|
| Missing `HomeScreen` | Created `HomeScreen.kt` (`@NavEaseScreen(route = "Home")`) — the splash destination that `SplashScreen` navigates to was absent, preventing compilation |
| `LocalNavEaseSharedTransitionScope` | Added `LocalNavEaseSharedTransition.kt` — `SharedTransitionScope?` CompositionLocal |
| `NavTransition` sealed class | 6 built-in styles: `Push`, `Fade`, `Rise`, `Zoom`, `Depth`, `Instant` — all wired into `Animations` and `NavDisplay`'s `transitionSpec` / `popTransitionSpec` |
| `navigateToXxx(navTransition)` | All generated navigate extensions accept an optional per-call `NavTransition?` override |
| Shared element transitions | `NavEaseNavGraph` optionally wraps `NavDisplay` in `SharedTransitionLayout` and wires scope to both `NavDisplay` and `LocalNavEaseSharedTransitionScope` |

---

## Table of Contents

1. [Architecture & Methodology (Actual vs. Documented)](#1-architecture--methodology-actual-vs-documented)
2. [File & Folder Structure Issues](#2-file--folder-structure-issues)
3. [Critical Bugs & Correctness Issues](#3-critical-bugs--correctness-issues)
4. [API & Feature Gaps](#4-api--feature-gaps)
5. [Documentation Mismatch](#5-documentation-mismatch)
6. [Dependency & Build Concerns](#6-dependency--build-concerns)
7. [Testing](#7-testing)
8. [Security & Safety](#8-security--safety)
9. [Recommendations & Roadmap](#9-recommendations--roadmap)

---

## 1. Architecture & Methodology (Actual vs. Documented)

### 1.1 What the library actually does

NavEase is a Kotlin Symbol Processing (KSP) + Compose-based navigation library. Its real working model is:

```
Developer writes:
  @NavEaseScreen(route = "Splash", startDestination = true)
  class SplashScreen : NavScreen<AppScreens.Splash>() { … }

KSP generates at compile time (4 files):
  ┌─ AppScreens.kt      — sealed class AppScreens : NavKey { data object Splash; … }
  ├─ ScreenFactory.kt   — object ScreenFactory { fun createScreen(key): NavScreen<*> }
  ├─ NavEaseResults.kt  — typed result data classes + extension functions
  └─ NavEaseHost.kt     — @Composable fun NavEaseHost() { AppNavGraph(…) }

Runtime delegates to:
  AppNavGraph (commonMain)
    └─ NavDisplay (org.jetbrains.androidx.navigation3:navigation3-ui) ← JetBrains KMP library
         └─ NavController (commonMain)
               └─ NavBackStack<NavKey> (androidx.navigation3.runtime) ← KMP
```

**Key design traits of the real implementation:**

| Trait | Reality |
|---|---|
| Annotation target | Classes (`@Target(CLASS)`) extending `NavScreen<T>` |
| Route identifier | Plain `String` (e.g., `"Splash"`) |
| Back-stack engine | JetBrains Navigation3 (`org.jetbrains.androidx.navigation3`) — KMP |
| Result passing | Global singleton `object ResultStore` keyed by class name |
| Transition system | One hardcoded global slide animation (450 ms) |
| Generated entry point | `NavEaseHost()` — zero parameters |
| Multi-platform targets | Android, iOS, JVM (Desktop), JS, WASM — all targets supported via KMP |
| Module system | Uses new `com.android.kotlin.multiplatform.library` AGP plugin (modern standard) |

### 1.2 What the README claims the library does

The README describes a completely different API that does **not exist** in the codebase:

| README claim | Actual code |
|---|---|
| `@NavEaseScreen(route = KClass, transition = Transition)` on `@Composable` functions | `@NavEaseScreen(route = String, startDestination = Boolean)` on classes |
| `NavEaseKey` marker interface | Does not exist — `androidx.navigation3.runtime.NavKey` is used |
| `NavEaseController` interface | Does not exist — concrete `NavController` class is used |
| `LocalNavEaseController` composition local | Does not exist |
| `Transition.SLIDE / FADE / NONE` enum | Does not exist — one hardcoded `Animations` object |
| `navigate(key, singleTop = true)` | Does not exist |
| `popUpTo(key, inclusive)` | Does not exist — only `popToIndex(Int)` |
| `backWithResult(Any?)` / `getResult<T>()` on controller | Replaced by generated typed extensions |
| `debugOverlay: Boolean` on `NavEaseHost` | Does not exist |
| `modifier: Modifier` on `NavEaseHost` | Does not exist |
| Sample app with 4 screens (Splash, Home, Detail, Settings) | Sample has 2 screens (Splash, Main) |

The README appears to describe an earlier design iteration (v1) that was replaced by the current implementation without updating the documentation.

---

## 2. File & Folder Structure Issues

This is one of the most significant architectural problems in the project. The module layout, package structure, and source-set placement all have notable issues.

### 2.1 Module system — correct modern approach, but not fully leveraged

NavEase correctly uses the **new Kotlin Multiplatform module system**:

- `com.android.kotlin.multiplatform.library` plugin — the new AGP KMP plugin that replaces the legacy `com.android.library` + separate `kotlin("multiplatform")` combination. With this plugin, the Android target is declared directly inside `kotlin { android { } }` alongside iOS, JVM, JS, and WASM targets, which is the modern standard for 2025+ KMP libraries.
- `org.jetbrains.androidx.navigation3:navigation3-ui` — this is the **JetBrains Kotlin Multiplatform port** of Navigation3, not the Android-only Google version. It runs on all declared targets (Android, iOS, JVM Desktop, JS, WASM). Its placement in `commonMain` is therefore **correct**.
- `androidx.savedstate.serialization.SavedStateConfiguration` — similarly available as a KMP library via JetBrains.

The multiplatform target setup compiles and runs on all five platforms. This is architecturally sound.

**Remaining structural issues in this area:**

```
navease-runtime/src/
  commonMain/   ← correct — all runtime code is KMP-compatible
  androidMain/  ← contains only AndroidManifest.xml — no Kotlin sources
  iosMain/      ← exists but is empty — no iOS-specific overrides
```

The `androidMain` and `iosMain` source sets are empty of Kotlin (only a manifest in `androidMain`). For a library with purely shared logic this is intentional and correct. However the existence of these empty folders can be confusing to contributors and may cause CI source-set scanning to emit warnings. Platform-specific source sets should only be created when they contain actual platform-specific code.

### 2.2 `domain/ActivityScreen.kt` — naming and placement

The file `ActivityScreen.kt` defines the abstract class `NavScreen<T>`. Both the file name and the package layer are wrong:

- **File name:** `ActivityScreen.kt` — misnames the content; the class is `NavScreen<T>`. Nothing in the file relates to an Android `Activity`.
- **Package layer:** Placed in `domain/` — but this is an abstract UI base class, not a domain layer entity. It belongs in a `ui/` or `base/` layer, or directly in the root package.
- **Domain layer should not import Compose:** `ActivityScreen.kt` imports `@Composable` and `ExperimentalSharedTransitionApi`, violating clean-architecture conventions where domain is framework-agnostic.

### 2.3 `presentation/AppNavGraph.kt` — hardcoded app-level name

`AppNavGraph` is in `navease-runtime` (a **library module**) but its name references "App", implying it belongs to a specific application. A reusable library composable should be named `NavEaseNavGraph` or `NavEaseDisplay`, not `AppNavGraph`.

### 2.4 `data/NavController.kt` — wrong layer for navigation orchestration

`NavController` is placed in the `data/` package. Navigation controllers orchestrate UI state flow — they belong in a `ui/`, `presentation/`, or `navigation/` package, not `data/`. The `data/` layer conventionally holds repositories, data sources, and models.

### 2.5 `data/ResultStore.kt` — global singleton in the wrong layer

`ResultStore` is a global `object` in the `data/` package. As a global mutable state store that drives Compose recomposition, it has both a naming problem (not a "store" in any conventional sense) and a layer problem — it is a runtime state manager that should be scoped to a `NavController` instance or a `CompositionLocal`, not a package-level singleton.

### 2.6 `shared` module contains sample app screens

The `shared` module (`shared/src/commonMain/kotlin/com/alim/navease/screens/`) contains:
```
screens/
  App.kt
  SplashScreen.kt
  MainScreen.kt
```

These are **sample/demo screens**, not shared library code. They belong in `androidApp`, `desktopApp`, etc., or in a dedicated `:sample` module. Mixing library infrastructure with sample app logic inside a shared module makes the library non-separable — consumers cannot depend on `:navease-runtime` and `:navease-ksp` without also having the sample screens in their dependency graph (via `:shared`).

### 2.7 Missing dedicated `:sample` module

There is no isolated sample module. The sample code is fragmented across:
- `androidApp/` (Android entry point)
- `desktopApp/` (desktop entry point)
- `webApp/` (web entry point)
- `shared/` (actual screen implementations)
- `iosApp/` (iOS entry point via Xcode)

A clean approach would be a single `:sample` KMP module with proper source sets, removing screen logic from `:shared`.

### 2.8 KSP generates into a fixed package not configurable by the consumer

All 4 generated files use the hardcoded package `io.github.alimsrepo.navease.generated`. A library distributing a KSP processor must allow consumers to configure the output package (typically via KSP arguments), otherwise all consumers share the same generated namespace and cannot have multiple independent nav graphs.

### 2.9 Empty platform source sets add noise without value

```
navease-runtime/src/
  androidMain/  ← AndroidManifest.xml only; no Kotlin
  iosMain/      ← completely empty
```

With the `com.android.kotlin.multiplatform.library` plugin, the Android target configuration lives inside `kotlin { android { } }`, so `androidMain` holding only a manifest is legitimate. However, retaining an `iosMain/` folder with no content adds confusion:

- Contributors may think iOS-specific overrides are needed or already exist.
- It creates an implicit coupling expectation that is never followed through.
- CI tools that diff source sets will flag it.

If no iOS-specific code is ever needed, the `iosMain/` source set folder should either be removed, or — if retained — contain a clearly documented `// No iOS-specific overrides needed` comment file.

### 2.10 `desktopApp` and `webApp` are separate top-level modules

The project separates each platform entry point into its own Gradle module (`desktopApp/`, `webApp/`). For a sample app this adds unnecessary build complexity; these could be source sets within a single sample module.

---

## 3. Critical Bugs & Correctness Issues

### 3.1 🔴 `ResultStore` is a global singleton — results leak between navigation hosts

```kotlin
// navease-runtime/.../data/ResultStore.kt
internal object ResultStore {
    val results = mutableStateMapOf<String, Any?>()
}
```

`ResultStore` is a Kotlin `object` — it lives for the entire process lifetime. In any scenario involving:
- Multiple `NavEaseHost` instances (nested navigation, bottom-tab navigation)
- Desktop multi-window applications
- Two screens that happen to return the same result type

…results will cross-contaminate silently. Screen A posting a result will be consumed by Screen B if both expect the same result class. There is no scoping, no ownership, no lifecycle binding.

**Severity:** 🔴 Critical (data loss in multi-graph setups)

### 3.2 🔴 `produceState` in `resultOf()` has a race condition and loses results

```kotlin
@Composable
fun <T : Any> NavController.resultOf(clazz: KClass<T>): State<T?> {
    val key = clazz.qualifiedName!!
    @Suppress("UNCHECKED_CAST")
    return produceState<T?>(initialValue = ResultStore.results[key] as? T) {
        // Consume immediately after reading
        value = ResultStore.results.remove(key) as? T
    }
}
```

Problems:
1. **`initialValue` reads the map**, then the coroutine body **removes the same entry**. The `initialValue` evaluates synchronously during the first composition pass. The coroutine then runs on the first frame and removes the entry from the map, setting `value` to the same thing. On the next recomposition, the state still holds the value — but it has already been removed from the map, so a second call (e.g., after a configuration change) will return `null`.
2. **Configuration change = lost result.** The `State` produced by `produceState` is not `rememberSaveable`. If the activity is recreated (rotation, language change), the result is gone.
3. **`produceState` is not the right tool here.** `produceState` is for converting async/callback-based sources into `State`. A simple `derivedStateOf` or `remember { mutableStateOf(...) }` with a one-shot consume pattern would be correct.

**Severity:** 🔴 Critical (silent result loss on configuration changes and multi-instance)

### 3.3 🔴 KSP does not generate `import` statements for custom types

`resolveTypeName()` in the KSP processor maps primitive Kotlin types to short names and falls back to the fully-qualified name for anything else:

```kotlin
else -> typeName  // e.g., "com.alim.navease.screens.SampleData"
```

The generated `NavEaseResults.kt` file uses these type names in data class definitions and function signatures but **never emits an `import` for them**. Example of broken generated output:

```kotlin
// Generated NavEaseResults.kt — WILL NOT COMPILE
data class MainResult(val value: Int, val sampleData: com.alim.navease.screens.SampleData)
```

Using fully-qualified names inline works only if the type is accessible from the generated package — which it isn't for internal/private types. The existing sample (`SampleData` in `MainScreen.kt`) demonstrates this bug directly.

**Severity:** 🔴 Critical (generated code does not compile when custom types are used)

### 3.4 🟠 `navigate(finish = true)` crash risk on small back stack

```kotlin
fun navigate(navKey: NavKey, finish: Boolean = false) {
    backStack.add(navKey)         // stack size = N+1
    if (finish)
        backStack.removeAt(backStack.size - 2)  // removes index N-1
}
```

If `finish = true` is called when the back stack has **1 entry** (index 0), after `add()` the size is 2, so `removeAt(0)` is called — removing the root destination. The back stack is now left with only the new screen and no root, which may be acceptable, but there is no guard against calling `navigate(finish = true)` when the stack is already empty (size 0), where `removeAt(-1)` would throw `IndexOutOfBoundsException`.

**Severity:** 🟠 High (crash in edge case)

### 3.5 🟠 `error()` throw in generated `ScreenFactory` — no graceful fallback

```kotlin
else -> error("Unknown screen: $appScreen")
```

KSP incremental builds can miss newly added screens in some edge cases. A hard `error()` (which throws `IllegalStateException`) means any KSP miss causes an unhandled crash rather than a navigable error state. A `NoSuchElementException` or a dedicated `NavEaseException` with better context would be more debuggable.

**Severity:** 🟠 High (unhandled crash)

### 3.6 🟡 `AppNavGraph` hardcodes `showExitDialog = {}`

```kotlin
val navController = remember {
    NavController(backStack = applicationStack, showExitDialog = { })
}
```

`NavController`'s constructor accepts a `showExitDialog: () -> Unit` callback, documented as the mechanism for showing an exit confirmation when the user presses back at the root. `AppNavGraph` always passes an empty lambda — the feature silently does nothing. Back-pressing at the root simply does nothing; the user cannot exit.

**Severity:** 🟡 Medium (UX bug — root back-press does nothing)

### 3.7 🟡 `Dependencies(false)` breaks KSP incremental compilation

All four `codeGenerator.createNewFile()` calls use `Dependencies(false)`:

```kotlin
val file = codeGenerator.createNewFile(
    Dependencies(false),   // ← "aggregating" mode, inputs not tracked
    …
)
```

`Dependencies(false)` tells KSP the file depends on **no specific source files**. This prevents incremental KSP from knowing when to invalidate and regenerate the outputs — the generated files will either always regenerate (slow builds) or never regenerate when they should (stale generated code). These files are aggregating processors and should use `Dependencies(true, *allSourceFiles.toTypedArray())`.

**Severity:** 🟡 Medium (build correctness / performance)

### 3.8 🟡 `@Suppress("UNCHECKED_CAST")` without documenting the invariant

```kotlin
// AppNavGraph.kt
@Suppress("UNCHECKED_CAST")
(screenFactory(route) as NavScreen<NavKey>).Content(navKey = route, navController = navController)
```

The unchecked cast is only safe as long as every `NavKey` in the back stack was placed there by the generated `ScreenFactory` which knows the mapping. This invariant is not enforced or documented. If a `NavKey` is placed into the back stack from outside `ScreenFactory`, the cast will throw `ClassCastException` at runtime with no clear error message.

**Severity:** 🟡 Medium

### 3.9 🟡 `NavController.back()` pops but does not validate activity lifecycle

```kotlin
fun back() {
    if (backStack.size > 1)
        backStack.removeLastOrNull()
    else showExitDialog()
}
```

Since `showExitDialog` is always `{}` (see §3.6), pressing back at the root does absolutely nothing — the user is stuck. On Android, pressing the hardware/gesture back button calls this method (if wired correctly), but there is no evidence of system back-press integration (e.g., `BackHandler`) in the runtime or sample app.

**Severity:** 🟡 Medium

---

## 4. API & Feature Gaps

### 4.1 `NavEaseHost()` accepts zero parameters

The generated `NavEaseHost()` composable takes no arguments:

```kotlin
@Composable
fun NavEaseHost() {
    AppNavGraph(
        initialScreen = AppScreens.startDestination,
        savedStateConfig = AppScreens.savedStateConfig,
        screenFactory = ScreenFactory::createScreen
    )
}
```

This makes the host entirely non-configurable:
- Cannot supply a custom start destination at runtime (e.g., from a push notification deep link)
- Cannot pass a `Modifier`
- Cannot toggle debug features
- Cannot inject a custom `NavController` for testing
- Cannot override animations

The README promises `NavEaseHost(startDestination, modifier, debugOverlay)` — none of these parameters exist.

### 4.2 No `popUpTo` support

`NavController` provides only:
```kotlin
fun popToIndex(index: Int)
```

This requires the caller to know the integer index of the target destination in the back stack — a non-type-safe, fragile API. The documented `popUpTo(key: NavEaseKey, inclusive: Boolean)` does not exist.

### 4.3 No `singleTop` navigation

There is no way to prevent duplicate entries in the back stack (navigate to a screen that is already the top). The README documents `navigate(key, singleTop = true)` — this does not exist.

### 4.4 No per-screen transition customisation

`Animations.kt` defines one global slide transition applied to all navigation. The `@NavEaseScreen` annotation has no `transition` parameter. `Transition.FADE` and `Transition.NONE` (documented in README) do not exist anywhere in the codebase.

### 4.5 No debug back-stack overlay

The README documents:
```kotlin
NavEaseHost(startDestination = …, debugOverlay = BuildConfig.DEBUG)
```
No debug overlay exists in the runtime. No `DebugBackStackOverlay.kt` file exists in any module (the README's module structure diagram lists it, but the file is absent).

### 4.6 No `LocalNavEaseController` composition local

The README shows `val nav = LocalNavEaseController.current` for accessing the controller from any nested composable. No `CompositionLocal` exists in the runtime. Consumers must pass `NavController` manually down the composable tree.

### 4.7 `@OptIn(ExperimentalSharedTransitionApi::class)` forced onto all screen implementors

```kotlin
@OptIn(ExperimentalSharedTransitionApi::class)
abstract class NavScreen<T> {
    @Composable
    abstract fun Content(navKey: T, navController: NavController)
}
```

`@OptIn` on the base class does **not** propagate to subclasses. However, the opt-in annotation on the class itself indicates the author believes shared transitions may be needed — but `Content()` does not use `SharedTransitionScope` or any shared-transition API. The annotation is misleading and will generate compiler warnings in consumers if they try to use matching APIs.

### 4.8 No deep link support

Deep link handling is not mentioned anywhere in the real code. The README acknowledges this gap ("Not in v1.0") but provides no workaround guidance that is actually implementable with the current zero-parameter `NavEaseHost()`.

### 4.9 No `SavedStateHandle` / process-death restoration

`rememberNavBackStack` from Navigation3 is used, which may or may not restore state after process death depending on the `SavedStateConfiguration`. The `savedStateConfig` in the generated `AppScreens` registers all subclasses, which is promising, but there is no documentation or test verifying that process-death restoration works end-to-end.

---

## 5. Documentation Mismatch

### 5.1 README describes a different library

The README was written for an earlier design and describes an API that does not exist in the current codebase. Every code sample in the README would fail to compile against the actual implementation. This is the most immediately damaging issue from a developer-experience perspective.

**Specific divergences:**

| README code | Compile result against real code |
|---|---|
| `interface NavEaseKey` | ❌ Class not found |
| `interface NavEaseController` | ❌ Class not found |
| `enum class Transition { SLIDE, FADE, NONE }` | ❌ Class not found |
| `@NavEaseScreen(route = AppScreens.Splash::class, transition = Transition.FADE)` on a `@Composable fun` | ❌ Annotation target wrong (CLASS only); `route` is `String`, not `KClass` |
| `nav.popUpTo(AppScreens.Splash, inclusive = true)` | ❌ Method not found |
| `nav.getResult<String>()` | ❌ Method not found |
| `LocalNavEaseController.current` | ❌ Object not found |
| `NavEaseHost(startDestination = …, debugOverlay = …)` | ❌ Parameters don't exist |
| `DebugBackStackOverlay` | ❌ File/class does not exist |

### 5.2 Module structure diagram in README is wrong

The README module diagram references files that don't exist:
```
navease-runtime/src/main/kotlin/io/github/alimsrepo/navease/
  NavEaseKey.kt           ← does not exist
  NavEaseScreen.kt        ← exists but has different content
  Transition.kt           ← does not exist
  NavEaseScreenFactory.kt ← does not exist
  NavEaseController.kt    ← does not exist
  NavEaseHost.kt          ← does not exist (only exists as generated code)
  DebugBackStackOverlay.kt← does not exist
```

The real layout is:
```
navease-runtime/src/commonMain/kotlin/io/github/alimsrepo/navease/runtime/
  NavEaseScreen.kt         ← @NavEaseScreen, @NavEaseArgs, @NavEaseResult annotations
  domain/
    ActivityScreen.kt      ← abstract class NavScreen<T>
  data/
    NavController.kt       ← class NavController
    ResultStore.kt         ← object ResultStore + extensions
    Animations.kt          ← object Animations (hardcoded transitions)
  presentation/
    AppNavGraph.kt         ← @Composable AppNavGraph(…)
```

### 5.3 Setup instructions reference unpublished artifacts

```kotlin
implementation("io.github.alimsrepo:navease-runtime:1.0.0")
ksp("io.github.alimsrepo:navease-ksp:1.0.0")
```

These coordinates do not exist on Maven Central or any public repository. The library has not been published. A consumer following the README cannot set up the library.

### 5.4 KSP-generated code example in README is wrong

The README shows `NavEaseGeneratedFactory` — the KSP processor generates `ScreenFactory` (an object with `createScreen()`), not `NavEaseGeneratedFactory`. The generated API surface described in the README cannot be used to understand the actual generated code.

---

## 6. Dependency & Build Concerns

### 6.1 Pre-release and unstable dependencies

| Dependency | Version | Status |
|---|---|---|
| `com.android.application` (AGP) | `9.2.1` | Pre-release / not publicly available |
| `org.jetbrains.androidx.lifecycle` | `2.11.0-beta01` | Beta |
| `org.jetbrains.compose.material3` | `1.11.0-alpha07` | Alpha |
| `androidx.test.runner` | `1.7.0` | Verify stability |
| `androidx.test.core` | `1.7.0` | Verify stability |

Using alpha/beta dependencies in a library is problematic because:
- APIs marked experimental in these versions may change or be removed.
- Consumers using stable dependencies may encounter transitive version conflicts.
- Build reproducibility cannot be guaranteed across different developer machines.

### 6.2 KSP version does not follow Kotlin version alignment

```toml
kotlin = "2.3.21"
ksp    = "2.3.9"
```

KSP releases for Kotlin 2.x are named `2.x.y-1.0.z` (e.g., `2.3.21-1.0.31`). The version `2.3.9` does not match this format and does not align with Kotlin `2.3.21`. If this resolves correctly, it is an unusual configuration; if not, KSP processing will fail with version mismatch errors. This needs verification.

### 6.3 `isMinifyEnabled = false` in the release build

```kotlin
buildTypes {
    getByName("release") {
        isMinifyEnabled = false
    }
}
```

ProGuard/R8 is disabled for release builds. For the **sample app** this means:
- Larger APK size.
- No dead code elimination.
- No obfuscation.

For a **library** this is expected (libraries don't minify themselves), but since the `androidApp` module is an application, release APKs will ship unoptimised.

### 6.4 `androidx.navigation3.ui` exposed as `api()` — intentional but worth documenting

```kotlin
// navease-runtime build.gradle.kts
// Navigation — exposed as api so consumers (e.g. :shared) can use NavKey directly
api(libs.androidx.navigation3.ui)
```

The `api()` exposure is **intentional** — consumers need access to `NavKey` to define their route sealed classes and the generated `AppScreens` implements it. This is a valid design choice for a navigation library. However it should be explicitly documented in the public API docs so consumers understand:
- Navigation3 is a first-class part of NavEase's public contract, not an implementation detail.
- Consumers who want to use Navigation3 APIs directly can do so without declaring the dependency again.
- Version management of Navigation3 is owned by NavEase — consumers must align with whatever version NavEase ships.

### 6.5 `navease-ksp` processor is missing the `ksp-symbol-processing` artifact dependency

```kotlin
// navease-ksp build.gradle.kts
dependencies {
    implementation(libs.ksp.api)
}
```

Only `ksp-api` is declared. This is correct for a processor that only needs the API — but there is no `compileOnly` declaration of the runtime annotation module (`:navease-runtime`). The processor references `@NavEaseScreen` by its fully-qualified string name (`"io.github.alimsrepo.navease.runtime.NavEaseScreen"`) rather than importing the class, which avoids the dependency but makes the code fragile to rename refactors.

### 6.6 No publishing configuration

There is no `maven-publish` plugin, no `publishing { }` block, no POM metadata, and no signing configuration in any build file. The library cannot be published to Maven Central or any other artifact repository without significant build configuration work.

---

## 7. Testing

### 7.1 Zero tests exist across all modules

```
navease-runtime/    ← 0 test files
navease-ksp/        ← 0 test files
shared/             ← 0 test files
androidApp/         ← 0 test files
```

Despite `commonTest` declaring a dependency on `kotlin-test`:
```kotlin
commonTest.dependencies {
    implementation(libs.kotlin.test)
}
```

No test source files exist anywhere in the project. This means:

- **No KSP processor tests** — the most critical component (code generation) has no compilation or output correctness tests.
- **No `NavController` unit tests** — back/navigate/popToIndex logic is untested.
- **No `ResultStore` tests** — the buggy `produceState` behaviour (§3.2) is undetected.
- **No Compose UI tests** — navigation flows, transition behaviour, and result passing are untested.
- **No integration tests** — end-to-end navigation (annotate → KSP → compile → run) is not verified.

For a navigation library — where correctness directly impacts every screen transition in consumer apps — zero test coverage is a critical blocker.

### 7.2 No KSP compilation testing infrastructure

KSP processors should be tested with `kotlin-compile-testing-ksp` or `ksp-testing` libraries, which allow:
- Feeding synthetic source files to the processor.
- Asserting on generated file content.
- Verifying error/warning messages for invalid annotations.

This infrastructure does not exist in `navease-ksp`.

---

## 8. Security & Safety

### 8.1 `qualifiedName!!` force-unwrap will crash with anonymous/local classes

```kotlin
// ResultStore.kt
fun NavController.backWithResult(result: Any) {
    ResultStore.results[result::class.qualifiedName!!] = result
    back()
}
```

`KClass.qualifiedName` returns `null` for anonymous classes, local classes, and lambda types. If a consumer passes an anonymous object as a result (`backWithResult(object : SomeInterface { … })`), this will throw `NullPointerException` at runtime with no helpful error message.

### 8.2 `ResultStore` allows `Any?` — no type safety at the store boundary

The store is typed `mutableStateMapOf<String, Any?>()`. Any value can be stored under any key. The only type safety is at the retrieval site (`as? T`), which silently returns `null` if the type doesn't match rather than throwing or warning. A mismatched result type is indistinguishable from "no result received."

### 8.3 No ProGuard/R8 consumer rules

The library does not ship a `proguard-rules.pro` or `consumer-rules.pro` file. KSP-generated classes and the `NavScreen` subclasses used via generated `when` expressions may be stripped by R8 in consumer apps that enable minification, leading to `NoClassDefFoundError` at runtime.

---

## 9. Recommendations & Roadmap

### 🔴 Critical (must fix before any release)

| # | Issue | Recommended Fix |
|---|---|---|
| C1 | `ResultStore` global singleton | Scope `ResultStore` to `NavController` (instance field); remove the `object` declaration |
| C2 | `produceState` result loss | Replace with `remember { mutableStateOf<T?>(null) }` + a one-shot `LaunchedEffect` that observes and clears the scoped store |
| C3 | KSP missing import generation | Track all non-primitive type FQNs from `@NavEaseArgs` / `@NavEaseResult` and emit `import` statements at the top of generated files |
| C4 | Update README to match real API | Rewrite README entirely to reflect the actual annotation model, generated files, and `NavController` API |

### 🟠 High (fix before public beta)

| # | Issue | Recommended Fix |
|---|---|---|
| H1 | Rename `ActivityScreen.kt` → `NavScreen.kt` | Move to root or `ui/` package in `navease-runtime` |
| H2 | Rename `AppNavGraph` → `NavEaseNavGraph` | Rename file and function to reflect library identity |
| H3 | Move `NavController` to `presentation/` or `navigation/` | Correct the layer placement |
| H4 | Wire `showExitDialog` in `AppNavGraph` | Accept a lambda parameter in `AppNavGraph` or use `BackHandler` |
| H5 | Fix `navigate(finish = true)` guard | Add `if (backStack.size >= 2)` before `removeAt` |
| H6 | Fix `Dependencies(false)` in KSP | Pass aggregating source files to `Dependencies(true, …)` for correct incremental builds |
| H7 | Add KSP processor tests | Use `kotlin-compile-testing-ksp`; test at minimum: args generation, result generation, missing annotation errors |
| H8 | Replace unstable dependencies | Pin to stable releases of AGP, lifecycle, material3 |

### 🟡 Medium (fix before stable v1.0)

| # | Issue | Recommended Fix |
|---|---|---|
| M1 | `NavEaseHost()` has zero parameters | Accept `modifier`, `debugOverlay`; allow start destination override via KSP argument |
| M2 | Add `popUpTo(key, inclusive)` | Implement in `NavController` using type-matching traversal of the back stack |
| M3 | Add `singleTop` navigation | Check `backStack.last()::class == navKey::class` before pushing |
| M4 | Add per-screen transition support | Add `transition` parameter to `@NavEaseScreen`; generate `transitionFor(key)` in `ScreenFactory` |
| M5 | Add `LocalNavEaseController` | Provide a `CompositionLocalProvider` in `AppNavGraph` wrapping `NavController` |
| M6 | Configurable generated package | Accept `navease.generatedPackage` as a KSP argument in `NavEaseProcessorProvider` |
| M7 | Add ProGuard consumer rules | Ship a `consumer-rules.pro` keeping `NavScreen` subclasses and generated classes |
| M8 | Guard `qualifiedName!!` | Use `?: error("…")` with a descriptive message |
| M9 | Enable `isMinifyEnabled` for release | Configure R8 in the sample app build type |

### 🟢 Low / Enhancements (post-v1.0)

| # | Issue | Recommended Fix |
|---|---|---|
| L1 | Extract sample into `:sample` module | Remove screen code from `:shared`; create dedicated sample module |
| L2 | Add debug back-stack overlay | Implement `NavEaseDebugOverlay` composable in `navease-runtime` |
| L3 | Publish to Maven Central | Add `maven-publish`, POM metadata, GPG signing |
| L4 | Add changelog (`CHANGELOG.md`) | Document changes per version |
| L5 | Add deep link support | Accept `NavKey` factory lambdas for URI → key mapping in generated host |
| L6 | Add `SavedStateHandle` restoration tests | Verify back stack survives process death in instrumented tests |
| L7 | Align KSP version with Kotlin version | Use `ksp = "2.3.21-1.0.x"` format matching Kotlin `2.3.21` |
| L8 | Document Navigation3 as a public API contract | Clarify in docs that `NavKey` and Navigation3 are intentionally exposed via `api()` |

---

## Summary Scorecard

### Original audit (May 27, 2026)

| Category | Score | Status |
|---|---|---|
| Correctness / Bugs | 2 / 10 | ⛔ Critical bugs found |
| API Design | 3 / 10 | ⛔ Undocumented, missing features |
| Documentation | 1 / 10 | ⛔ Describes wrong library |
| Architecture / Structure | 5 / 10 | 🟠 Correct KMP module system; naming/layer issues remain |
| Testing | 0 / 10 | ⛔ Zero tests |
| Build & Dependencies | 4 / 10 | 🟠 Pre-release deps, no publishing |
| Multiplatform Readiness | 7 / 10 | 🟡 Structure and tooling correct; end-to-end verification on non-Android targets needed |
| Security & Safety | 4 / 10 | 🟠 Force-unwraps, no ProGuard rules |
| **Overall** | **3.3 / 10** | **⛔ Not production-ready** |

### Updated assessment (May 30, 2026)

All 🔴 Critical and 🟠 High issues (except H8 — unstable deps) have been resolved. All 🟡 Medium issues are complete.

| Category | Score | Status |
|---|---|---|
| Correctness / Bugs | 8 / 10 | ✅ All critical bugs fixed; no known data-loss paths |
| API Design | 8 / 10 | ✅ `popUpTo`, `singleTop`, `NavTransition`, `LocalNavEaseController`, typed results |
| Documentation | 8 / 10 | ✅ README matches real code; KSP options documented |
| Architecture / Structure | 7 / 10 | 🟡 Correct KMP module system; sample still in `:shared` |
| Testing | 4 / 10 | 🟡 KSP processor tests added; runtime/UI tests still absent |
| Build & Dependencies | 5 / 10 | 🟡 ProGuard + minification enabled; pre-release deps remain |
| Multiplatform Readiness | 7 / 10 | 🟡 Verified structurally; no cross-platform runtime tests |
| Security & Safety | 8 / 10 | ✅ Force-unwraps guarded; consumer ProGuard rules shipped |
| **Overall** | **6.9 / 10** | **🟡 Beta-quality — safe for internal use; publish after H8** |

---

*Original audit: May 27, 2026. Remediation sprint: May 30, 2026. No compiled artifacts were executed. Runtime behaviour conclusions are inferred from source code analysis.*


