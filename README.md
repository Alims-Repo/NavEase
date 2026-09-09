# NavEase

**NavEase** is a navigation library for **Kotlin Multiplatform + Compose Multiplatform**.

Annotate a screen, and it is wired. No reflection, no string routes, no manual registry, no red
underlines while you write.

> ✅ **Status:** published to Maven Central — latest version: **0.1.4**

---

## Table of Contents

- [How it works](#how-it-works)
- [Platform Support](#platform-support)
- [Setup](#setup)
- [Quick start](#quick-start)
- [NavEaseController](#naveasecontroller)
- [Transitions](#transitions)
- [Observing destination changes](#observing-destination-changes)
- [Back-with-result](#back-with-result)
- [Shared element transitions](#shared-element-transitions)
- [Nested navigation](#nested-navigation)
- [What KSP generates](#what-ksp-generates)
- [Constraints and gotchas](#constraints-and-gotchas)
- [Module structure](#module-structure)
- [Sample app](#sample-app)
- [FAQ](#faq)
- [License](#license)

---

## How it works

Three pieces, and nothing else:

| Piece | What it is |
|---|---|
| `sealed class AppScreens : NavEaseRoot` | Your destinations. One subclass per screen; `data class` for arguments |
| `@AutoRegister class HomeScreen : ActivityScreen<AppScreens.Home>()` | A screen. `Content()` receives its key **already typed** |
| `NavEaseHost<AppScreens>(start = AppScreens.Splash)` | The host. Finds every screen belonging to that root |

KSP discovers the annotated classes at build time and generates the registry, a `KSerializer` for
every key, and the `SavedStateConfiguration` the back stack restores from. You write none of it.

**What you don't write:** no `when (route)` factory, no serializers module, no `@Serializable` on
your key class, no route strings, no manual `add(...)` list.

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

### Recommended — the Gradle plugin

```kotlin
// shared/build.gradle.kts
plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")

    id("io.github.alims-repo.navease") version "0.1.4"   // ← all wiring done ✅
}
```

The plugin:

- applies the KSP and Kotlin Serialization compiler plugins (skipped if you already declare them)
- adds `navease-ksp` to `kspCommonMainMetadata`
- registers `build/generated/ksp/metadata/commonMain/kotlin` as a `commonMain` source directory
- makes every KMP compilation depend on `kspCommonMainKotlinMetadata`
- adds `navease-runtime` to `commonMain`

Optional configuration:

```kotlin
navease {
    version = "0.1.4"                      // pin the artifacts (default: same as the plugin)
    addRuntimeDependency = true            // false to manage navease-runtime yourself
    generatedPackage = "com.myapp.nav"     // where KSP writes — see the note in Constraints
}
```

### Manual setup

```kotlin
plugins {
    kotlin("multiplatform")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.serialization")
}

kotlin {
    sourceSets {
        commonMain {
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
            dependencies {
                implementation("io.github.alims-repo:navease-runtime:0.1.4")
            }
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", "io.github.alims-repo:navease-ksp:0.1.4")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>>().configureEach {
    if (name != "kspCommonMainKotlinMetadata") dependsOn("kspCommonMainKotlinMetadata")
}
```

---

## Quick start

### 1. Declare your destinations

```kotlin
import io.github.alimsrepo.navease.runtime.NavEaseRoot

sealed class AppScreens : NavEaseRoot {

    data object Splash : AppScreens()
    data object Home : AppScreens()

    data class Profile(val userId: String, val isEditable: Boolean = false) : AppScreens()
    data class Detail(val itemId: String, val title: String) : AppScreens()
}
```

No `@Serializable`, and no `: NavKey` — `NavEaseRoot` covers both. KSP writes a serializer for each
subclass from its constructor parameters.

### 2. Write the screens

```kotlin
import io.github.alimsrepo.navease.runtime.annotations.AutoRegister
import io.github.alimsrepo.navease.runtime.navigation.NavEaseController
import io.github.alimsrepo.navease.runtime.screen.ActivityScreen

@AutoRegister
class HomeScreen : ActivityScreen<AppScreens.Home>() {

    @Composable
    override fun Content(navKey: AppScreens.Home, navEaseController: NavEaseController) {
        Button(onClick = { navEaseController.navigate(AppScreens.Profile(userId = "alim")) }) {
            Text("Open profile")
        }
    }
}

@AutoRegister
class ProfileScreen : ActivityScreen<AppScreens.Profile>() {

    @Composable
    override fun Content(navKey: AppScreens.Profile, navEaseController: NavEaseController) {
        Text("User ${navKey.userId}")            // ← typed. No casting, no args() extension.
        if (navKey.isEditable) EditForm()
    }
}
```

`@AutoRegister` takes no arguments. The start destination is declared at the host, so a nested host
can pick its own.

### 3. Host it

```kotlin
@Composable
fun App() {
    MaterialTheme {
        NavEaseHost<AppScreens>(
            start = AppScreens.Splash,
            onExitRequest = { finish() },        // back pressed on the root screen
        )
    }
}
```

That is the whole setup. Adding a screen later means writing the class and rebuilding — nothing
else to update.

---

## NavEaseController

Received as the second parameter of `Content()`, and available from any composable below the host
via `LocalNavEaseController.current`.

```kotlin
/** Push a screen onto the back stack. */
fun navigate(
    navKey: NavKey,
    finish: Boolean = false,
    singleTop: Boolean = false,
    navTransition: NavTransition? = null,
)

/** Pop one screen. At the root, calls the host's onExitRequest instead. */
fun back()

/** Pop until [key]'s screen. Matched by runtime class — argument values are ignored. */
fun popUpTo(key: NavKey, inclusive: Boolean = false)

/** Pop everything above [index] (0 = root). */
fun popToIndex(index: Int)

/** The current back stack, oldest entry first. */
fun getHistory(): List<NavKey>
```

| Call | Effect |
|---|---|
| `navigate(key)` | Push |
| `navigate(key, finish = true)` | Push and drop the screen underneath — replace, as a splash does |
| `navigate(key, singleTop = true)` | Skip if that screen class is already on top |
| `navigate(key, navTransition = NavTransition.Fade)` | Override the animation for this one navigation |
| `back()` | Pop, or `onExitRequest` at the root |

Reaching it from deep in a tree:

```kotlin
@Composable
fun DeepWidget() {
    val nav = LocalNavEaseController.current ?: return   // null outside a host, e.g. in a preview
    Button(onClick = { nav.back() }) { Text("Back") }
}
```

---

## Transitions

```kotlin
NavEaseHost<AppScreens>(start = AppScreens.Splash, navTransition = NavTransition.Depth)
```

| Transition | Motion |
|---|---|
| `Push` *(default)* | Horizontal slide with parallax — the native-mobile feel |
| `Fade` | Symmetric cross-dissolve |
| `Rise` | Rises from the bottom; good for sheet-like steps |
| `Zoom` | Scales up from ~86% while fading in |
| `Depth` | Material Z-axis shared-axis motion |
| `Instant` | No animation |

The host value is the default; any single navigation can override it:

```kotlin
nav.navigate(AppScreens.Detail(id, title), navTransition = NavTransition.Rise)
```

The transition is recorded per push, so the matching reverse animation plays on the way back —
including for the predictive-back gesture.

---

## Observing destination changes

Every host takes an optional `onDestinationChanged`, called whenever the top of the back stack
changes — including for the start destination on first composition.

It is for the concerns that belong to the graph rather than to any one screen. Screen-view
analytics is the obvious one: done per screen it is a line every screen has to remember, and
nothing catches a new screen that forgets it.

```kotlin
NavEaseHost<AppScreens>(
    start = AppScreens.Splash,
    onDestinationChanged = { navKey ->
        analytics.logScreenView(navKey.screenName)
    },
)
```

`NavEaseController` is the **receiver**, not a second parameter. The host owns its controller, so
code outside it has no other way to reach one — a hook that only observes ignores the receiver, and
a hook that needs to navigate uses it:

```kotlin
NavEaseHost<AppScreens>(
    start = AppScreens.Splash,
    onDestinationChanged = { navKey ->
        if (navKey !is AppScreens.Main && deepLink.isPending) {
            navigate(AppScreens.Main)          // `this` is the NavEaseController
        }
    },
)
```

On a nested host it answers "which inner screen am I on", which is what tells an outer shell
whether to show its bottom bar:

```kotlin
NavEaseHost<HomeNav>(
    start = HomeNav.Root,
    onDestinationChanged = { navKey -> hideBottomBar(navKey !is HomeNav.Root) },
)
```

The callback runs outside composition, so call plain functions from it — not composables.

---

## Back-with-result

Send a value back to the screen underneath.

```kotlin
import io.github.alimsrepo.navease.runtime.navigation.backWithResult
import io.github.alimsrepo.navease.runtime.navigation.resultOf

data class ProfileResult(val saved: Boolean)

// On the screen returning a value:
@AutoRegister
class ProfileScreen : ActivityScreen<AppScreens.Profile>() {
    @Composable
    override fun Content(navKey: AppScreens.Profile, navEaseController: NavEaseController) {
        Button(onClick = { navEaseController.backWithResult(ProfileResult(saved = true)) }) {
            Text("Save")
        }
    }
}

// On the screen waiting for it:
@AutoRegister
class HomeScreen : ActivityScreen<AppScreens.Home>() {
    @Composable
    override fun Content(navKey: AppScreens.Home, navEaseController: NavEaseController) {
        val result by navEaseController.resultOf<ProfileResult>()

        LaunchedEffect(result) {
            if (result?.saved == true) showSnackbar("Profile saved")
        }
    }
}
```

Results are held in a snapshot-state map scoped to that host's controller, so writing one
recomposes the reader. `resultOf<T>()` consumes the entry as it reads it — it will not fire twice —
and nothing is shared between independent hosts.

---

## Shared element transitions

**Off by default.** When `enableSharedTransitions = false`, no `SharedTransitionLayout` is created
and `LocalNavEaseSharedTransitionScope` is `null` — zero overhead, and screens that don't use
shared elements need no experimental opt-in.

### Without shared elements

Nothing to do. This is the default:

```kotlin
NavEaseHost<AppScreens>(start = AppScreens.Splash)
```

### With shared elements

Turn it on at the host that owns the transition:

```kotlin
NavEaseHost<AppScreens>(
    start = AppScreens.Splash,
    enableSharedTransitions = true,
)
```

Then, in the two screens that share the element, read both scopes and match the key exactly:

```kotlin
import androidx.compose.animation.ExperimentalSharedTransitionApi
import io.github.alimsrepo.navease.internal.navigation.ui.LocalNavAnimatedContentScope
import io.github.alimsrepo.navease.runtime.composition.LocalNavEaseSharedTransitionScope

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Avatar(userId: String) {
    val sharedScope = LocalNavEaseSharedTransitionScope.current
    val animScope = LocalNavAnimatedContentScope.current

    val modifier = if (sharedScope != null) {
        with(sharedScope) {
            Modifier.sharedBounds(
                sharedContentState = rememberSharedContentState(key = "avatar_$userId"),
                animatedVisibilityScope = animScope,
            )
        }
    } else Modifier

    Box(modifier.size(88.dp)) { /* … */ }
}
```

**Rules:**

- The key is any `Any` — a string like `"avatar_$userId"`, or a data class. It must match
  **exactly** on both screens, or nothing morphs.
- `sharedBounds` for content that changes size or shape; `sharedElement` for content that doesn't.
- Guard on `sharedScope != null` so the same composable still renders when the flag is off.
- A shared element only morphs **within one host**. Two screens in different hosts each have their
  own `SharedTransitionLayout`, so enable the flag on the host that actually contains both screens.

---

## Nested navigation

A nested graph is just another sealed root with its own screens and its own host. Entries are
filtered by root type, so the two graphs never see each other's screens.

```kotlin
sealed class WizardStep : NavEaseRoot {
    data object PickRole : WizardStep()
    data class Confirm(val role: String) : WizardStep()
}

@AutoRegister
class PickRoleScreen : ActivityScreen<WizardStep.PickRole>() { /* … */ }

@AutoRegister
class ConfirmScreen : ActivityScreen<WizardStep.Confirm>() { /* … */ }
```

```kotlin
// Inside a screen of the outer graph:
NavEaseHost<WizardStep>(
    start = WizardStep.PickRole,
    // Back on the nested root leaves the whole feature.
    onExitRequest = { outerController.back() },
)
```

Each host owns an independent back stack, controller and result store, so nothing leaks between
them. Put as many side by side as you need — bottom tabs, a wizard inside a screen, a detail pane.

---

## What KSP generates

After a build, in `build/generated/ksp/metadata/commonMain/kotlin/`:

| File | Contents |
|---|---|
| `AutoRegisterScreens.kt` | A `KSerializer` per key, `NavEaseAutoInit` registering every screen, and `navEaseBootstrap()` |
| `NavEaseHostOverloads.kt` | One `NavEaseHost` overload per sealed root, which bootstraps the registry and then hosts |

Generated registration looks like this:

```kotlin
private object NavEaseAutoInit {
    init {
        NavEaseAutoRegistry.addEntry(HomeScreen(), Home::class, AppScreens::class, NavEase_Home_Ser)
        NavEaseAutoRegistry.addEntry(ProfileScreen(), Profile::class, AppScreens::class, NavEase_Profile_Ser)
        // … one line per @AutoRegister screen
    }
}
```

**The generated overload is what initialises the registry.** `NavEaseHost<AppScreens>(...)` resolves
to it in preference to the library's generic overload, and it calls `navEaseBootstrap()` before
creating the host. This is what makes the registry work identically on every platform — it does not
depend on JVM reflection or on a Kotlin/Native eager-init anchor.

---

## Constraints and gotchas

**Screens must extend `ActivityScreen` directly.** KSP reads the key type `K` from the class's
direct supertypes, so an intermediate base class of your own — a `TrackedScreen<K>` that centralises
analytics, say — fails with *"@AutoRegister class must extend ActivityScreen<K>"*. Put shared
behaviour in a composable you call from `Content()`, or use
[onDestinationChanged](#observing-destination-changes) for anything graph-wide.

**One screen per key.** Registering two `@AutoRegister` screens for the same key is a build error.

**Key arguments must be serializable.** Kotlin primitives and `String` work as they are; anything
else must be `@Serializable`. Sealed hierarchies are fine — the generated serializer delegates to
the type's own.

**R8 / ProGuard.** Generated serializers are not the compiler plugin's `$$serializer` classes, so a
rule matching those will not cover them. Keep the generated package:

```proguard
-keep class com.myapp.nav.** { *; }
```

**`generatedPackage` and the Android fallback.** `NavEaseAutoRegistry.generatedClassHint` defaults
to `io.github.alimsrepo.navease.generated.AutoRegisterScreensKt`. Setting a custom
`generatedPackage` — or relying on the plugin's default, which appends the module name — means that
`Class.forName` fallback no longer matches. It does not normally matter, because the generated
overload above bootstraps first, but set the hint in `Application.onCreate()` if you want the
fallback to stay live:

```kotlin
NavEaseAutoRegistry.generatedClassHint = "com.myapp.nav.AutoRegisterScreensKt"
```

**Rebuild after adding a screen.** `@AutoRegister` is processed at build time, so a new screen needs
`./gradlew :shared:kspCommonMainKotlinMetadata` (or any build) before the host can route to it.

---

## Module structure

```
NavEase/
├── navease-runtime/            ← KMP library, all platforms
│   └── io/github/alimsrepo/navease/runtime/
│       ├── NavEaseRoot.kt              ← marker for your sealed key class
│       ├── annotations/AutoRegister.kt
│       ├── screen/ActivityScreen.kt    ← base class for a screen
│       ├── host/NavEaseHost.kt         ← the host overloads
│       ├── navigation/                 ← NavEaseController + back-with-result
│       ├── transition/                 ← NavTransition + the animations
│       ├── composition/                ← LocalNavEaseController, LocalNavEaseSharedTransitionScope
│       └── registry/                   ← NavEaseAutoRegistry
│
├── navease-ksp/                ← JVM KSP processor
├── navease-gradle-plugin/      ← id("io.github.alims-repo.navease")
│
├── shared/                     ← sample app (commonMain)
├── androidApp/  desktopApp/  webApp/  iosApp/
```

---

## Sample app

`shared/` is a small multi-screen demo built exactly the way this README describes — six
destinations, typed arguments, shared element transitions and a splash that replaces itself.

| Screen | Key | Shows |
|---|---|---|
| `SplashScreen` | `Splash` | Auto-navigate with `finish = true` |
| `HomeScreen` | `Home` | The hub; shared bounds into detail |
| `ProfileScreen` | `Profile(userId, isEditable)` | Typed arguments with a default |
| `SettingsScreen` | `Settings` | Plain destination |
| `GalleryScreen` | `Gallery` | List navigation |
| `DetailScreen` | `Detail(itemId, title)` | Typed arguments; shared element target |

```bash
./gradlew :androidApp:installDebug   # Android
./gradlew :desktopApp:run            # Desktop
```

---

## FAQ

**Do I have to register screens anywhere?**
No. Annotate with `@AutoRegister` and rebuild. There is no factory to update and no list to keep in
sync.

**Does my key class need `@Serializable`?**
No. `NavEaseRoot` is enough — KSP writes the serializers. Argument *types* still need to be
serializable.

**What happens on process death?**
The back stack is restored. NavEase assembles the `SavedStateConfiguration` from the generated
serializers, so a key carrying arguments — including a sealed type — round-trips intact.

**Can I have several graphs at once?**
Yes. See [Nested navigation](#nested-navigation). Each host is fully independent.

**How do I show a confirmation before exiting?**
Show it from `onExitRequest` and only finish on confirm:

```kotlin
NavEaseHost<AppScreens>(start = AppScreens.Splash, onExitRequest = { showExitDialog = true })
```

**Is the back gesture handled?**
Yes, including predictive back on Android. Back is only consumed while there is something to pop —
at the root it falls through to the system, so the app closes normally unless you handle
`onExitRequest`.

**Do shared elements work outside Android?**
Yes. `SharedTransitionLayout` is Compose Multiplatform, so iOS, Desktop and Web behave the same.

**Do I need KSP?**
Yes, for `@AutoRegister`. The Gradle plugin applies and wires it for you.

---

## License

```
Copyright 2026 NavEase Contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0
```
