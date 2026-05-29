package com.alim.navease.screens

import io.github.alimsrepo.navease.runtime.presentation.NavTransition

// ── Library category ──────────────────────────────────────────────────────────

enum class LibraryCategory(val label: String) {
    KMP("Kotlin Multiplatform"),
    CMP("Compose Multiplatform"),
    ANDROID("Android"),
}

// ── Data model ────────────────────────────────────────────────────────────────

data class Library(
    val id: String,
    val name: String,
    val emoji: String,
    val tagline: String,
    val description: String,
    val version: String,
    val groupId: String,
    val artifactIds: List<String>,
    val githubUrl: String,
    val websiteUrl: String,
    val platforms: List<String>,
    val category: LibraryCategory,
    val colorIndex: Int,
    val navTransition: NavTransition,
    val features: List<String>,
    val installCode: String,
    val quickUsageCode: String,
)

// ── Library catalogue ─────────────────────────────────────────────────────────

val allLibraries: List<Library> = listOf(

    Library(
        id = "navease",
        name = "NavEase",
        emoji = "🧭",
        tagline = "Annotation-driven KMP navigation",
        description = "NavEase is a KSP-powered, annotation-driven navigation library for Kotlin Multiplatform + Compose Multiplatform. Annotate your screen classes — KSP generates the route hierarchy, screen factory, typed arguments, typed results, and the nav host at compile time. No manual registration. No reflection. No string routes.",
        version = "pre-release",
        groupId = "io.github.alimsrepo",
        artifactIds = listOf("navease-runtime", "navease-ksp"),
        githubUrl = "https://github.com/Alims-Repo/NavEase",
        websiteUrl = "https://alims-repo.github.io/NavEase/",
        platforms = listOf("Android", "iOS", "Desktop", "Web"),
        category = LibraryCategory.KMP,
        colorIndex = 0,
        navTransition = NavTransition.Push,
        features = listOf(
            "Zero boilerplate — KSP generates everything at compile time",
            "Typed arguments via @NavEaseArgs — no casting",
            "Typed results via @NavEaseResult — strongly-typed one-shot state",
            "Class-based screens — extend plain NavScreen, no generics",
            "6 built-in NavTransition styles (Push, Fade, Rise, Zoom, Depth, Instant)",
            "Per-screen transition override on every navigateToXxx() call",
            "Shared element transitions via LocalNavEaseSharedTransitionScope",
            "singleTop and popUpTo navigation helpers",
            "CompositionLocal NavController for deeply nested composables",
        ),
        installCode = """// shared/build.gradle.kts
commonMain.dependencies {
    implementation("io.github.alimsrepo:navease-runtime:<version>")
}
dependencies {
    add("kspCommonMainMetadata", "io.github.alimsrepo:navease-ksp:<version>")
}

// Then trigger code generation:
// ./gradlew :shared:kspCommonMainKotlinMetadata""",
        quickUsageCode = """@NavEaseScreen(route = "Detail")
class DetailScreen : NavScreen() {

    @NavEaseArgs
    data class Args(val itemId: Int, val label: String)

    @NavEaseResult
    data class Result(val liked: Boolean)

    @Composable
    override fun Content(navKey: NavKey, navController: NavController) {
        val args = navKey.detailArgs()   // ✅ generated
        Button(onClick = {
            navController.backWithDetailResult(liked = true)  // ✅ generated
        }) { Text("Like ${'$'}{args.label}") }
    }
}""",
    ),

    Library(
        id = "securevault",
        name = "SecureVault KMP",
        emoji = "🔐",
        tagline = "Native secure secret storage for KMP",
        description = "A coroutine-first Kotlin Multiplatform library for storing secrets on Android (EncryptedSharedPreferences over the Android Keystore) and iOS (Keychain Services). One clean API, two native backends — no hand-rolled cryptography. An optional Compose Multiplatform module adds rememberSecureVault, rememberSecureValue, and LocalSecureVault.",
        version = "0.3.0",
        groupId = "io.github.alims-repo",
        artifactIds = listOf("secure-vault", "secure-vault-compose"),
        githubUrl = "https://github.com/Alims-Repo/SecureVault-KMP",
        websiteUrl = "https://alims-repo.github.io/SecureVault-KMP/",
        platforms = listOf("Android", "iOS"),
        category = LibraryCategory.KMP,
        colorIndex = 1,
        navTransition = NavTransition.Rise,
        features = listOf(
            "Android Keystore via EncryptedSharedPreferences",
            "iOS Keychain Services — native backend",
            "Coroutine-first suspend put / get / remove API",
            "observe(key): Flow<String?> — reactive secret binding",
            "Compose: rememberSecureVault + VaultState sealed type",
            "rememberSecureValue — MutableState bound to a vault key",
            "LocalSecureVault CompositionLocal",
            "Single VaultException sealed hierarchy",
            "No manual Context injection — uses androidx.startup",
        ),
        installCode = """// commonMain/dependencies
implementation("io.github.alims-repo:secure-vault:0.3.0")

// Optional — Compose Multiplatform integration:
implementation("io.github.alims-repo:secure-vault-compose:0.3.0")""",
        quickUsageCode = """val vault = SecureVault("com.myapp.auth")

// Store
vault.put("session_token", bearerToken)

// Retrieve
val token: String? = vault.get("session_token")

// Observe (Flow — re-emits on every write or remove)
vault.observe("session_token").collect { println("Token: ${'$'}it") }

// Remove
vault.remove("session_token")

// --- Compose integration ---
@Composable fun LoginScreen() {
    var token by rememberSecureValue("auth.token", default = "")
    OutlinedTextField(value = token, onValueChange = { token = it })
}""",
    ),

    Library(
        id = "flowtab",
        name = "FlowTab CMP",
        emoji = "🎨",
        tagline = "Beautiful animated bottom nav for CMP",
        description = "A beautiful, animated, and completely framework-agnostic bottom navigation bar for Jetpack Compose and Compose Multiplatform. Works with any navigation solution — NavEase, Navigation3, Decompose, Voyager, or plain Compose state. Supports glassmorphism blur via Haze, expandable search, notification badges, and three selection indicator styles.",
        version = "0.5.6-beta",
        groupId = "io.github.alims-repo",
        artifactIds = listOf("flowtab-cmp"),
        githubUrl = "https://github.com/Alims-Repo/FlowTab-CMP",
        websiteUrl = "https://alims-repo.github.io/FlowTab-CMP/",
        platforms = listOf("Android", "iOS"),
        category = LibraryCategory.CMP,
        colorIndex = 2,
        navTransition = NavTransition.Zoom,
        features = listOf(
            "100% framework agnostic — works with any nav library",
            "3 indicator styles: Ripple, Dot, Line",
            "Built-in expandable search bar (NavItemType.Search)",
            "Glassmorphism blur via Haze integration",
            "Badge support — count number or dot indicator",
            "Isolated item type for FAB-like action buttons",
            "Highly customizable NavConfig + NavColor",
            "Preset styles: Instagram, Pill, Floating Minimal",
            "Configurable animation duration and scale effects",
        ),
        installCode = """// commonMain/dependencies (or shared module)
implementation("io.github.alims-repo:flowtab-cmp:0.5.6-beta")

// libs.versions.toml:
// flowtab-cmp = "0.5.6-beta"
// flowtab-cmp = { module = "io.github.alims-repo:flowtab-cmp", version.ref = "flowtab-cmp" }""",
        quickUsageCode = """BottomNavigation(
    items = listOf(
        NavItem(id = "home", label = "Home",
                icon = Icons.Outlined.Home, selectedIcon = Icons.Filled.Home),
        NavItem(id = "search", label = "Search",
                icon = Icons.Default.Search, type = NavItemType.Search),
        NavItem(id = "profile", label = "Profile",
                icon = Icons.Outlined.Person,
                badge = BadgeData(count = 3)),  // badge!
    ),
    selectedId = selectedScreen,
    onItemSelected = { item -> selectedScreen = item.id },
    config = NavConfig(
        navIndicator = NavIndicator.Ripple(),   // or Dot / Line
        cornerRadius = 60.dp,
        enableBlur = true,
    )
)""",
    ),

    Library(
        id = "prayertimes",
        name = "Prayer Times KMM",
        emoji = "🕌",
        tagline = "Accurate Islamic prayer time calculations",
        description = "A lightweight, accurate, and highly customizable Kotlin Multiplatform library for calculating Islamic prayer times across Android, iOS, and JVM. Inspired by the Adhan algorithm with advanced solar time computations, seasonal adjustments, and high-latitude region handling. Supports 11+ calculation methods from major Islamic organizations.",
        version = "1.0.4-beta",
        groupId = "io.github.alims-repo",
        artifactIds = listOf("prayer-times-kmm"),
        githubUrl = "https://github.com/Alims-Repo/Prayer-Times-KMM",
        websiteUrl = "https://alims-repo.github.io/Prayer-Times-KMM/",
        platforms = listOf("Android", "iOS", "JVM"),
        category = LibraryCategory.KMP,
        colorIndex = 3,
        navTransition = NavTransition.Depth,
        features = listOf(
            "All 5 daily prayers: Fajr, Dhuhr, Asr, Maghrib, Isha",
            "11+ calculation methods (MWL, ISNA, Egypt, Makkah, Karachi…)",
            "2 Madhab options — Shafi and Hanafi for Asr time",
            "Current and next prayer detection",
            "High-latitude region handling (midnight sun, polar night)",
            "Per-prayer minute offset adjustments",
            "Flexible rounding: nearest, up, down, none",
            "Sunrise and sunset times with astronomical precision",
            "Type-safe and null-safe across all platforms",
        ),
        installCode = """// commonMain/dependencies (or androidMain, iosMain, jvmMain)
implementation("io.github.alims-repo:prayer-times-kmm:1.0.4-beta")""",
        quickUsageCode = """val prayerTimes = PrayerTimes(
    coordinates = Coordinates(
        latitude = 21.4225,   // Makkah
        longitude = 39.8262
    ),
    dateComponents = DateComponents(year = 2024, month = 3, day = 26),
    calculationParameters = CalculationParameters(
        method = CalculationMethod.UMM_AL_QURA,
        madhab = Madhab.SHAFI,
        rounding = RoundingType.NEAREST
    )
)

println("Fajr:    ${'$'}{prayerTimes.fajr}")
println("Sunrise: ${'$'}{prayerTimes.sunrise}")
println("Dhuhr:   ${'$'}{prayerTimes.dhuhr}")
println("Asr:     ${'$'}{prayerTimes.asr}")
println("Maghrib: ${'$'}{prayerTimes.maghrib}")
println("Isha:    ${'$'}{prayerTimes.isha}")""",
    ),

    Library(
        id = "crashguard",
        name = "CrashGuard",
        emoji = "🛡️",
        tagline = "Industry-grade Android crash handler",
        description = "An industry-grade Android library that provides beautiful, customizable crash screens. Built with Clean Architecture principles, it offers dual crash screens (user-friendly release view and developer debug view with full stack traces), persistent crash log storage, comprehensive device/memory data capture, and seamless analytics integration.",
        version = "1.0.0",
        groupId = "io.github.alims-repo",
        artifactIds = listOf("crash-guard"),
        githubUrl = "https://github.com/Alims-Repo/Crash-Guard",
        websiteUrl = "https://alims-repo.github.io/Crash-Guard/",
        platforms = listOf("Android"),
        category = LibraryCategory.ANDROID,
        colorIndex = 4,
        navTransition = NavTransition.Fade,
        features = listOf(
            "Dual crash screens: user-friendly + developer debug view",
            "Persistent crash log storage — up to 50 crashes by default",
            "Export crashes as JSON or plain text",
            "Full device capture: model, RAM, battery, network, orientation",
            "Activity stack trace in every crash report",
            "Auto-restart with configurable delay",
            "Crash interceptors for custom handling logic",
            "Analytics integration — Firebase, Crashlytics, Sentry",
            "Exception exclusion list and secure mode",
        ),
        installCode = """// build.gradle.kts (Android module)
dependencies {
    implementation("io.github.alims-repo:crash-guard:1.0.0")
}""",
        quickUsageCode = """// Application.onCreate() or early init
CrashGuard.install(this) {
    crashScreen {
        userScreen = MyFriendlyCrashActivity::class
        developerScreen = MyDebugCrashActivity::class
    }
    logging {
        maxCrashLogs = 50
        enablePersistence = true
    }
    analytics {
        onCrash { throwable, data ->
            Firebase.crashlytics.recordException(throwable)
        }
    }
    autoRestart {
        enabled = true
        delayMillis = 3_000L
    }
}""",
    ),

    Library(
        id = "pdfgenerator",
        name = "Pdf Generator",
        emoji = "📄",
        tagline = "Kotlin DSL for multi-page Android PDFs",
        description = "A lightweight, powerful Kotlin DSL library for generating multi-page PDF documents on Android. Create professional PDFs with text, tables, images, QR codes, checkboxes, headers/footers, watermarks, and more — all with automatic pagination. Supports A3, A4, A5, A6, Letter, Legal, and custom page sizes.",
        version = "1.0.6-beta",
        groupId = "io.github.alims-repo",
        artifactIds = listOf("pdf-generator"),
        githubUrl = "https://github.com/Alims-Repo/Pdf-Generator",
        websiteUrl = "https://alims-repo.github.io/Pdf-Generator/",
        platforms = listOf("Android"),
        category = LibraryCategory.ANDROID,
        colorIndex = 5,
        navTransition = NavTransition.Push,
        features = listOf(
            "Kotlin DSL — clean, intuitive builder syntax",
            "Automatic multi-page pagination",
            "Full-featured tables with headers and page splitting",
            "Rich text: title, heading, paragraph with custom fonts",
            "QR code generation — URL, WiFi, contact, email, SMS…",
            "Images with flexible sizing and alignment",
            "Checkboxes for forms, surveys, and checklists",
            "Info / warning / error / success box elements",
            "Customizable headers, footers, page numbers, and watermarks",
        ),
        installCode = """// build.gradle.kts (Android module)
dependencies {
    implementation("io.github.alims-repo:pdf-generator:1.0.6-beta")
}""",
        quickUsageCode = """val pdfFile = File(context.cacheDir, "report.pdf")

pdf {
    pageSize(PageSize.A4)
    margins(PageMargins.NORMAL)

    header { text("Confidential — Q1 2026") }
    footer { pageNumber() }

    title("Sales Report Q1 2026")
    spacer(16f)

    table {
        header("Product", "Units", "Revenue")
        row("Widget A", "1,200", "\$24,000")
        row("Widget B", "850",   "\$17,000")
    }

    heading("Highlights")
    bulletList("Exceeded target by 12%", "3 new markets opened")
    qrCode(QRData.Url("https://company.com/report"))

    watermark("DRAFT", alpha = 0.08f)
}.saveToFile(pdfFile)""",
    ),
)

fun libraryById(id: String): Library? = allLibraries.find { it.id == id }




