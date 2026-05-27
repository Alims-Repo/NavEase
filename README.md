# NavEase

**NavEase** is a lightweight, KSP-powered Android navigation library built on Jetpack Compose.  
Annotate your screens with `@NavEaseScreen` — KSP does the rest. No manual screen registration, no reflection, no ceremony.

---

## Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Setup](#setup)
- [Quick Start](#quick-start)
- [API Reference](#api-reference)
- [KSP-Generated Code](#ksp-generated-code)
- [Sample App](#sample-app)
- [FAQ](#faq)
- [Module Structure](#module-structure)

---

## Features

| Feature | Details |
|---|---|
| **Zero boilerplate** | KSP generates the screen factory from `@NavEaseScreen` annotations |
| **Type-safe routes** | Routes are sealed classes — no strings, no stringly-typed args |
| **Typed arguments** | Arguments live in the route key — no `Bundle` hacks |
| **Back-with-result** | Pass a typed result back via `backWithResult()` / `getResult()` |
| **Built-in transitions** | `SLIDE`, `FADE`, `NONE` — per-screen customisation |
| **Debug overlay** | Translucent live back-stack panel (debug builds only) |
| **Compose-native** | Backed by `AnimatedContent` + `SnapshotStateList` |

---

## Architecture

```
┌────────────────────────────────────────────────────────┐
│  Your App                                              │
│  AppScreens : NavEaseKey       ← you define this       │
│  @NavEaseScreen fun FooScreen  ← you annotate this     │
│  NavEaseHost(AppScreens.Foo)   ← generated entry point │
└─────────────────────┬──────────────────────────────────┘
                      │ KSP generates at compile time
                      ▼
┌────────────────────────────────────────────────────────┐
│  NavEaseGeneratedFactory : NavEaseScreenFactory        │
│  fun NavEaseHost(startDestination, …) { … }            │
└─────────────────────┬──────────────────────────────────┘
                      │ calls
                      ▼
┌────────────────────────────────────────────────────────┐
│  navease-runtime                                       │
│  NavEaseHostInternal  — AnimatedContent back stack     │
│  NavEaseControllerImpl — SnapshotStateList<NavEaseKey> │
│  DebugBackStackOverlay (debug builds only)             │
└────────────────────────────────────────────────────────┘
```

---

## Setup

### 1. Add to your app module `build.gradle.kts`

```kotlin
plugins {
    // … existing plugins …
    id("com.google.devtools.ksp") version "<ksp-version>"
    id("org.jetbrains.kotlin.plugin.serialization") version "<kotlin-version>"
}

dependencies {
    implementation("io.github.alimsrepo:navease-runtime:1.0.0")
    ksp("io.github.alimsrepo:navease-ksp:1.0.0")
}
```

> **KSP version** must match your Kotlin version.  
> See https://github.com/google/ksp/releases

### 2. Generate factory

```bash
./gradlew :app:kspDebugKotlin
```

NavEase generates `NavEaseGeneratedFactory` + `NavEaseHost` into  
`build/generated/ksp/debug/kotlin/io/github/alimsrepo/navease/generated/`.

---

## Quick Start

### Step 1 — Define routes

```kotlin
@Serializable
sealed class AppScreens : NavEaseKey {

    @Serializable data object Splash   : AppScreens()
    @Serializable data object Home     : AppScreens()

    @Serializable
    data class Detail(val itemId: Int, val title: String) : AppScreens()

    @Serializable data object Settings : AppScreens()
}
```

- Must implement `NavEaseKey`
- `@Serializable` is recommended (enables future state-restoration support)
- Sealed hierarchy keeps routes in one file

---

### Step 2 — Annotate composable screens

```kotlin
// Splash — fade in, auto-navigate to Home
@NavEaseScreen(route = AppScreens.Splash::class, transition = Transition.FADE)
@Composable
fun SplashScreen(navKey: AppScreens.Splash, nav: NavEaseController) {
    LaunchedEffect(Unit) {
        delay(1_500)
        nav.popUpTo(AppScreens.Splash, inclusive = true)
        nav.navigate(AppScreens.Home)
    }
}

// Home — default SLIDE transition, reads result from Detail
@NavEaseScreen(route = AppScreens.Home::class)
@Composable
fun HomeScreen(navKey: AppScreens.Home, nav: NavEaseController) {
    val result = nav.getResult<String>()  // one-shot consume

    Button(onClick = {
        nav.navigate(AppScreens.Detail(itemId = 42, title = "Hello"))
    }) { Text("Open Detail") }
}

// Detail — typed args, passes result back to Home
@NavEaseScreen(route = AppScreens.Detail::class)
@Composable
fun DetailScreen(navKey: AppScreens.Detail, nav: NavEaseController) {
    Text("Item ${navKey.itemId}: ${navKey.title}")   // typed args ✅

    Button(onClick = {
        nav.backWithResult("Visited ${navKey.itemId}")
    }) { Text("Back with result") }
}
```

**Rules for annotated functions**
1. First parameter — the exact `NavEaseKey` subtype matching `route`
2. Second parameter — `NavEaseController`
3. Must be `@Composable`

---

### Step 3 — Launch NavEaseHost

```kotlin
import io.github.alimsrepo.navease.generated.NavEaseHost   // auto-generated

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                NavEaseHost(
                    startDestination = AppScreens.Splash,
                    debugOverlay = BuildConfig.DEBUG,
                )
            }
        }
    }
}
```

---

## API Reference

### NavEaseKey

```kotlin
interface NavEaseKey
```

Marker interface. **The only coupling point** between your code and NavEase.

---

### @NavEaseScreen

```kotlin
annotation class NavEaseScreen(
    val route: KClass<out NavEaseKey>,
    val transition: Transition = Transition.SLIDE,
)
```

| Parameter | Description |
|---|---|
| `route` | The `NavEaseKey` subclass this composable handles |
| `transition` | Entry/exit animation (`SLIDE` default) |

---

### NavEaseController

```kotlin
interface NavEaseController {
    val backStack: List<NavEaseKey>

    fun navigate(key: NavEaseKey, singleTop: Boolean = false)
    fun back(): Boolean
    fun popUpTo(key: NavEaseKey, inclusive: Boolean = false)
    fun backWithResult(result: Any?)
    fun <T> getResult(): T?
}
```

| Method | Description |
|---|---|
| `navigate(key)` | Push screen onto the back stack |
| `navigate(key, singleTop = true)` | Skip if top entry is already the same type |
| `back()` | Pop current screen; `false` if at root |
| `popUpTo(key)` | Pop all entries above `key` |
| `popUpTo(key, inclusive = true)` | Also remove `key` from the stack |
| `backWithResult(result)` | Go back and deliver a result |
| `getResult<T>()` | One-shot consume result from a child screen |

---

### Transitions

| Value | Behaviour |
|---|---|
| `Transition.SLIDE` | Horizontal slide (iOS-style). Forward: right-in/left-out. Back: reversed. |
| `Transition.FADE` | Cross-fade |
| `Transition.NONE` | Instant switch |

---

### NavEaseHost (generated)

```kotlin
@Composable
fun NavEaseHost(
    startDestination: NavEaseKey,
    modifier: Modifier = Modifier,
    debugOverlay: Boolean = false,
)
```

### NavEaseHostInternal (runtime, for testing/manual use)

```kotlin
@Composable
fun NavEaseHostInternal(
    startDestination: NavEaseKey,
    factory: NavEaseScreenFactory,
    modifier: Modifier = Modifier,
    debugOverlay: Boolean = false,
)
```

### LocalNavEaseController

Access the controller from any composable inside the host:

```kotlin
val nav = LocalNavEaseController.current
```

### Debug Overlay

```kotlin
NavEaseHost(
    startDestination = AppScreens.Splash,
    debugOverlay = BuildConfig.DEBUG,   // ← toggle here
)
```

Shows index, class name, and `toString()` of every back-stack entry.

---

## KSP-Generated Code

After `./gradlew kspDebugKotlin`, NavEase writes one file:

```
build/generated/ksp/debug/kotlin/
  io/github/alimsrepo/navease/generated/
    NavEaseGeneratedFactory.kt
```

Example output:

```kotlin
// ⚠️  AUTO-GENERATED BY NAVEASE KSP — DO NOT EDIT.

object NavEaseGeneratedFactory : NavEaseScreenFactory {

    @Composable
    override fun Content(key: NavEaseKey, nav: NavEaseController) {
        when (key) {
            is AppScreens.Splash   -> SplashScreen(key, nav)
            is AppScreens.Home     -> HomeScreen(key, nav)
            is AppScreens.Detail   -> DetailScreen(key, nav)
            is AppScreens.Settings -> SettingsScreen(key, nav)
            else -> { /* no-op */ }
        }
    }

    override fun transitionFor(key: NavEaseKey): Transition = when (key) {
        is AppScreens.Splash   -> Transition.FADE
        is AppScreens.Home     -> Transition.SLIDE
        is AppScreens.Detail   -> Transition.SLIDE
        is AppScreens.Settings -> Transition.FADE
        else                   -> Transition.SLIDE
    }
}

@Composable
fun NavEaseHost(startDestination: NavEaseKey, modifier: Modifier = Modifier, debugOverlay: Boolean = false) {
    NavEaseHostInternal(startDestination, NavEaseGeneratedFactory, modifier, debugOverlay)
}
```

---

## Sample App

The `androidApp` module shows a complete 4-screen flow:

| Screen | Route | Transition | Demonstrates |
|---|---|---|---|
| `SplashScreen` | `AppScreens.Splash` | FADE | Auto-navigate, `popUpTo` |
| `HomeScreen` | `AppScreens.Home` | SLIDE | List, `getResult()` |
| `DetailScreen` | `AppScreens.Detail(id, title)` | SLIDE | Typed args, `backWithResult()` |
| `SettingsScreen` | `AppScreens.Settings` | FADE | Fade transition, `back()` |

```bash
./gradlew :androidApp:installDebug
```

---

## FAQ

**Q: Do I need to register screens somewhere?**  
No. Annotate with `@NavEaseScreen`, rebuild, done.

**Q: What if I add a new screen?**  
Add the route to your sealed class, annotate the composable, rebuild. The factory updates automatically.

**Q: Can I use NavEase without KSP?**  
Yes — implement `NavEaseScreenFactory` and call `NavEaseHostInternal` directly.

**Q: Does NavEase support deep links?**  
Not in v1.0. You can handle them by calling `nav.navigate(key)` after parsing the incoming `Intent`.

**Q: Nested navigation / bottom tabs?**  
Each tab/pane can host its own `NavEaseHostInternal` with an independent controller.

---

## Module Structure

```
NavEase/
├── navease-runtime/          ← Android library
│   └── src/main/kotlin/io/github/alimsrepo/navease/
│       ├── NavEaseKey.kt
│       ├── NavEaseScreen.kt          — @NavEaseScreen annotation
│       ├── Transition.kt
│       ├── NavEaseScreenFactory.kt
│       ├── NavEaseController.kt
│       ├── NavEaseHost.kt            — NavEaseHostInternal + LocalNavEaseController
│       └── DebugBackStackOverlay.kt
│
├── navease-ksp/              ← JVM/KSP processor
│   └── src/main/kotlin/io/github/alimsrepo/navease/ksp/
│       ├── NavEaseProcessorProvider.kt
│       └── NavEaseProcessor.kt
│
└── androidApp/               ← Sample app
    └── src/main/kotlin/com/alim/navease/
        ├── AppScreens.kt
        ├── MainActivity.kt
        └── screens/
            ├── SplashScreen.kt
            ├── HomeScreen.kt
            ├── DetailScreen.kt
            └── SettingsScreen.kt
```

---

## License

```
Copyright 2026 NavEase Contributors

Licensed under the Apache License, Version 2.0
```
