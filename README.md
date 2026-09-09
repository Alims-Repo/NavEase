# NavEase

Navigation for **Kotlin Multiplatform + Compose Multiplatform**.

Annotate a screen, and it is wired. No reflection, no string routes, no manual registry, and no
red underlines while you write.

> **Status:** published to Maven Central — latest version **0.2.0**

---

## Table of contents

- [How it works](#how-it-works)
- [Platform support](#platform-support)
- [Setup](#setup)
- [Quick start](#quick-start)
- [NavEaseController](#naveasecontroller)
- [Transitions](#transitions)
- [Observing destination changes](#observing-destination-changes)
- [Back with result](#back-with-result)
- [Shared element transitions](#shared-element-transitions)
- [Nested navigation](#nested-navigation)
- [Multi-module projects](#multi-module-projects)
- [What KSP generates](#what-ksp-generates)
- [Constraints](#constraints)
- [Troubleshooting](#troubleshooting)
- [Module structure](#module-structure)
- [Sample app](#sample-app)
- [FAQ](#faq)
- [License](#license)

---

## How it works

Three pieces, and nothing else:

| Piece | What it is |
|---|---|
| `sealed class AppScreens : NavEaseRoot` | Your destinations. One subclass per screen; `data class` when it carries arguments |
| `@AutoRegister class HomeScreen : ActivityScreen<AppScreens.Home>()` | A screen. `Content()` receives its key **already typed** |
| `NavEaseHost<AppScreens>(start = AppScreens.Splash)` | The host. Serves every screen belonging to that root |

At build time KSP finds the annotated classes and writes the registry, a `KSerializer` for every
key, and the `SavedStateConfiguration` the back stack restores from.

**What you don't write:** no `when (route)` factory, no serializers module, no `@Serializable` on
your key class, no route strings, no manual `add(...)` list.

---

## Platform support

| Platform | Target | Entry point |
|---|---|---|
| Android | `android` | `MainActivity` → `setContent { App() }` |
| iOS | `iosArm64`, `iosSimulatorArm64` | `MainViewController()` → `ComposeUIViewController { App() }` |
| Desktop | `jvm` | `application { Window { App() } }` |
| Web (JS) | `js { browser() }` | `ComposeViewport { App() }` |
| Web (Wasm) | `wasmJs { browser() }` | `ComposeViewport { App() }` |

No per-platform initialisation. The same `App()` works everywhere.

---

## Setup

### With the Gradle plugin

```kotlin
// shared/build.gradle.kts
plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")

    id("io.github.alims-repo.navease") version "0.2.0"
}
```

That is the whole build change. The plugin:

- applies KSP and the Kotlin serialization compiler plugin, skipping either one your build
  already declares;
- adds `navease-ksp` to `kspCommonMainMetadata` and `navease-runtime` to `commonMain`;
- registers `build/generated/ksp/metadata/commonMain/kotlin` as a `commonMain` source directory;
- makes every compilation depend on `kspCommonMainKotlinMetadata`;
- gives the module its own generated package, so several modules can use NavEase side by side.

Optional configuration:

```kotlin
navease {
    // Pin the artifacts. Defaults to the plugin's own version, so the three always match.
    version = "0.2.0"

    // Set false to declare navease-runtime yourself.
    addRuntimeDependency = false

    // Where KSP writes. Defaults to io.github.alimsrepo.navease.generated.<module name>.
    generatedPackage = "com.example.app.navigation"
}
```

### Without the plugin

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
                implementation("io.github.alims-repo:navease-runtime:0.2.0")
            }
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", "io.github.alims-repo:navease-ksp:0.2.0")
}

ksp {
    // Required in a multi-module build: two modules generating into one package produce
    // duplicate classes.
    arg("navease.generatedPackage", "com.example.app.navigation")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>>().configureEach {
    if (name != "kspCommonMainKotlinMetadata") dependsOn("kspCommonMainKotlinMetadata")
}
tasks.configureEach {
    if (name != "kspCommonMainKotlinMetadata" && name.startsWith("ksp")) {
        dependsOn("kspCommonMainKotlinMetadata")
    }
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

No `@Serializable`, and no `: NavKey`. `NavEaseRoot` covers both — KSP writes a serializer for
each subclass from its constructor parameters.

### 2. Write the screens

```kotlin
import androidx.compose.runtime.Composable
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
        Text("User ${navKey.userId}")     // typed — no casting, no args() helper
        if (navKey.isEditable) EditForm()
    }
}
```

`@AutoRegister` takes no arguments. The start destination is declared at the host, so a nested
host can pick its own.

### 3. Host it

```kotlin
@Composable
fun App() {
    MaterialTheme {
        NavEaseHost<AppScreens>(
            start = AppScreens.Splash,
            onExitRequest = { finish() },     // back pressed on the root screen
        )
    }
}
```

That is the whole setup. Adding a screen later means writing the class and rebuilding.

---

## NavEaseController

Received as the second parameter of `Content()`, and available from any composable below the host
through `LocalNavEaseController.current`.

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

From deep in a tree:

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

The transition is recorded per push, so the matching reverse animation plays on the way back,
including for the predictive-back gesture.

---

## Observing destination changes

Every host takes an optional `onDestinationChanged`, called whenever the top of the back stack
changes — including for the start destination on first composition.

It is for concerns that belong to the graph rather than to any one screen. Screen-view analytics
is the obvious one: done per screen it is a line every screen has to remember, and nothing catches
a new screen that forgets it.

```kotlin
NavEaseHost<AppScreens>(
    start = AppScreens.Splash,
    onDestinationChanged = { navKey ->
        analytics.logScreenView(navKey::class.simpleName.orEmpty())
    },
)
```

`NavEaseController` is the **receiver**, not a second parameter. The host owns its controller, so
code outside it has no other way to reach one — a hook that only observes ignores the receiver,
and a hook that needs to navigate uses it:

```kotlin
NavEaseHost<AppScreens>(
    start = AppScreens.Splash,
    onDestinationChanged = { navKey ->
        if (navKey !is AppScreens.Home && deepLink.isPending) {
            navigate(AppScreens.Home)          // `this` is the NavEaseController
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

The callback runs outside composition, so call plain functions from it, not composables.

---

## Back with result

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

Results live in a snapshot-state map scoped to that host's controller, so writing one recomposes
the reader. `resultOf<T>()` consumes the entry as it reads it — it will not fire twice — and
nothing is shared between independent hosts.

Results are keyed by the result type's simple name, so it must be a named class, not an anonymous
or local one.

---

## Shared element transitions

**Off by default.** When `enableSharedTransitions = false`, no `SharedTransitionLayout` is created
and `LocalNavEaseSharedTransitionScope` is `null` — zero overhead, and screens that don't use
shared elements need no experimental opt-in.

Turn it on at the host that owns the transition:

```kotlin
NavEaseHost<AppScreens>(
    start = AppScreens.Splash,
    enableSharedTransitions = true,
)
```

Then read both scopes in the two screens that share the element, matching the key exactly:

```kotlin
import androidx.compose.animation.ExperimentalSharedTransitionApi
import io.github.alimsrepo.navease.runtime.composition.LocalNavEaseAnimatedContentScope
import io.github.alimsrepo.navease.runtime.composition.LocalNavEaseSharedTransitionScope

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Avatar(userId: String) {
    val sharedScope = LocalNavEaseSharedTransitionScope.current
    val animScope = LocalNavEaseAnimatedContentScope.current

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

Rules:

- The key is any `Any` — a string like `"avatar_$userId"`, or a data class. It must match
  **exactly** on both screens, or nothing morphs.
- `sharedBounds` for content that changes size or shape; `sharedElement` for content that doesn't.
- Guard on `sharedScope != null` so the same composable still renders when the flag is off.
- A shared element only morphs **within one host**. Two screens in different hosts each have their
  own `SharedTransitionLayout`, so enable the flag on the host that contains both screens.

---

## Nested navigation

A nested graph is another sealed root with its own screens and its own host. Entries are filtered
by root, so the two graphs never see each other's screens.

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

Each host owns an independent back stack, controller and result store, and gets its own screen
instances, so nothing leaks between them. Put as many side by side as you need — bottom tabs, a
wizard inside a screen, a detail pane.

Two roots may safely declare keys with the same name; `AppScreens.Detail` and `WizardStep.Detail`
are distinct throughout the generated code.

---

## Multi-module projects

Apply the plugin to every module that declares `@AutoRegister` screens. Each module gets its own
generated package (`io.github.alimsrepo.navease.generated.<module name>`) and its own bootstrap,
so nothing collides.

One rule: **all the screens for a given sealed root must live in one module.** The root's host
overload is generated by the module that owns those screens, and two modules generating an
overload for the same root would produce an ambiguous call. A root per feature module is the
natural shape:

```
:app         AppScreens        — the shell
:feature-cart CartScreens      — nested host, its own root
:feature-account AccountScreens — nested host, its own root
```

The module that hosts a nested graph needs a dependency on the module that declares it, as it
would for any other type.

---

## What KSP generates

After a build, in `build/generated/ksp/metadata/commonMain/kotlin/`:

| File | Contents |
|---|---|
| `<generatedPackage>/AutoRegisterScreens.kt` | A `KSerializer` per key, and `navEaseBootstrap()` registering every screen |
| `io/github/alimsrepo/navease/runtime/host/NavEaseHostOverloads_<module>.kt` | One `NavEaseHost` overload per sealed root |

Registration looks like this:

```kotlin
private object NavEaseAutoInit {
    init {
        NavEaseAutoRegistry.addEntry(
            AppScreens.Home::class,
            AppScreens::class,
            NavEaseSer_com_example_AppScreens_Home,
        ) { HomeScreen() }
        // … one line per @AutoRegister screen
    }
}
```

Screens are registered as **factories**, so each host builds its own instances and two hosts of
one root never share a screen object.

**The generated overload is what initialises the registry.** `NavEaseHost<AppScreens>(...)`
resolves to it in preference to the runtime's generic overload, because its `start` parameter is
the more specific type, and it calls `navEaseBootstrap()` before creating the host. That is what
makes the registry work identically on every platform — it depends on neither JVM reflection nor
a Kotlin/Native eager-init anchor.

The overloads are generated into the runtime's `host` package so that your ordinary
`import io.github.alimsrepo.navease.runtime.host.NavEaseHost` picks them up. The file name carries
the module's generated package, so several modules never collide.

Generated names are fully qualified and entries are sorted, so the output is byte-identical
between builds.

---

## Constraints

**Screens must extend `ActivityScreen` directly.** KSP reads the key type `K` from the class's
direct supertypes, so an intermediate base class of your own — a `TrackedScreen<K>` that
centralises analytics, say — fails the build. Put shared behaviour in a composable you call from
`Content()`, or use [onDestinationChanged](#observing-destination-changes) for anything
graph-wide.

**Screens must be constructible with no arguments.** Generated code calls `HomeScreen()`. Obtain
dependencies inside `Content()` — from a composition local, or your DI framework's composable
accessor — rather than through the constructor.

**One screen per key.** Two `@AutoRegister` screens handling the same key is a build error, and
the message names both.

**Key arguments must be serializable.** Kotlin primitives and `String` work as they are; anything
else must be `@Serializable`. Sealed hierarchies are fine — the generated serializer delegates to
the type's own.

**Changing a key's parameters invalidates a saved back stack.** NavEase writes every parameter, so
restoring state saved before a parameter was added fails with a message naming that parameter.
Removing one is safe: unknown elements are skipped.

**Rebuild after adding a screen.** `@AutoRegister` is processed at build time, so a new screen
needs `./gradlew :shared:kspCommonMainKotlinMetadata` — or any build — before the host can route
to it.

**R8 / ProGuard.** Generated serializers are not the compiler plugin's `$$serializer` classes, so
a rule matching those will not cover them. Keep the generated package:

```proguard
-keep class com.example.app.navigation.** { *; }
```

---

## Troubleshooting

**"the screen registry is empty"**

The host did not bind to the generated overload. Either the module has not been built since the
screens were added, or the NavEase plugin is not applied to the module that declares them. Run
`./gradlew :yourModule:kspCommonMainKotlinMetadata` and check that
`build/generated/ksp/metadata/commonMain/kotlin` contains `AutoRegisterScreens.kt`.

**"no @AutoRegister screens found for root 'X'"**

The registry is populated, but nothing is registered under that root. The message lists the roots
that *are* registered. The usual cause is a key nested under an intermediate sealed layer in a
different root than expected — NavEase matches on the outermost sealed class of the key.

**"must extend ActivityScreen&lt;K&gt; directly"**

An intermediate base class hides `K` from KSP. See [Constraints](#constraints).

**Duplicate class `NavEaseHostOverloads…` in a multi-module build**

Two modules were given the same `generatedPackage`. Remove the explicit `generatedPackage` and let
the plugin derive one per module, or give each module a distinct value.

---

## Module structure

```
NavEase/
├── navease-runtime/            ← KMP library, all platforms
│   └── io/github/alimsrepo/navease/
│       ├── runtime/
│       │   ├── NavEaseRoot.kt          ← marker for your sealed key class
│       │   ├── annotations/            ← @AutoRegister
│       │   ├── screen/                 ← ActivityScreen<K>
│       │   ├── host/                   ← NavEaseHost + the display engine
│       │   ├── navigation/             ← NavEaseController, back-with-result
│       │   ├── transition/             ← NavTransition and the animations
│       │   ├── composition/            ← LocalNavEaseController and the scopes
│       │   └── registry/               ← NavEaseAutoRegistry
│       └── internal/                   ← vendored AndroidX Navigation 3; not public API
│
├── navease-ksp/                ← JVM KSP processor
├── navease-gradle-plugin/      ← id("io.github.alims-repo.navease")
│
├── shared/                     ← sample app (commonMain)
├── androidApp/  desktopApp/  webApp/  iosApp/
```

Everything under `io.github.alimsrepo.navease.internal` is a fork of AndroidX Navigation 3. It is
public for technical reasons but is not a supported surface, and it may change in any release.
Nothing you write should import from it.

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
No. Annotate with `@AutoRegister` and rebuild. There is no factory to update and no list to keep
in sync.

**Does my key class need `@Serializable`?**
No. `NavEaseRoot` is enough — KSP writes the serializers. Argument *types* still need to be
serializable.

**What happens on process death?**
The back stack is restored. NavEase assembles the `SavedStateConfiguration` from the generated
serializers, so a key carrying arguments — including a sealed type — round-trips intact.

**Can I have several graphs at once?**
Yes. See [Nested navigation](#nested-navigation). Each host is fully independent.

**Can a screen take constructor dependencies?**
No — generated code calls `HomeScreen()`. Read them inside `Content()` instead. A screen is
created per host, so it may hold state for the life of that host.

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
Yes. The Gradle plugin applies and wires it for you.

---

## License

```
Copyright 2026 NavEase Contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0
```

Sources under `navease-runtime/.../navease/internal/` are derived from
[AndroidX Navigation 3](https://github.com/androidx/androidx), Copyright The Android Open Source
Project, also under Apache 2.0. See [NOTICE](NOTICE).
