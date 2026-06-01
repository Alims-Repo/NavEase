package com.alim.navease.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.alimsrepo.navease.runtime.annotations.AutoRegister
import io.github.alimsrepo.navease.runtime.host.NavEaseHost
import io.github.alimsrepo.navease.runtime.navigation.NavEaseController
import io.github.alimsrepo.navease.runtime.screen.ActivityScreen
import io.github.alimsrepo.navease.runtime.transition.NavTransition

// ─────────────────────────────────────────────────────────────────────────────
// Outer screen — auto-registered in the global NavEaseAutoRegistry
// ─────────────────────────────────────────────────────────────────────────────

/**
 * CompositionLocal that carries the **outer** (root) [NavEaseController] into the inner
 * `NavEaseHost<WizardStep>` so that wizard step screens can navigate to root-level screens
 * without needing constructor args.
 *
 * Provided by [NestedNavDemoScreen.Content] before the inner host is composed.
 * Consumed by [SelectLevelStep] and [SelectTechStep] for the stack-persistence test buttons.
 */
val LocalOuterNavEaseController = compositionLocalOf<NavEaseController?> { null }

/**
 * Demonstrates a **nested NavEaseHost** with a 4-step developer-profile wizard.
 *
 * Architecture:
 * ```
 * NavEaseHost(start = AppScreens.Splash)           // ← root host
 *   └─ NestedNavDemoScreen                         // ← this outer screen
 *       └─ NavEaseHost<WizardStep>(                // ← inner host
 *              start = WizardStep.SelectRole,      //   own start key
 *              onExitRequest = { outerNav.back() } //   bridges to root
 *          )
 * ```
 *
 * Each wizard step carries typed data forward via [WizardStep] constructor args.
 * Steps 2 and 3 contain a **"Stack Persistence Test"** button that navigates the *outer*
 * (root) controller to a completely different root screen. Pressing back returns to
 * exactly the step and inner state that was active before, proving that the inner
 * back-stack is isolated from — and not affected by — the root back-stack.
 */
@AutoRegister
class NestedNavDemoScreen : ActivityScreen<AppScreens.NestedNavDemo>() {

    // ── Step 1 : Select Role ──────────────────────────────────────────────────

    /** Entry point of the inner host. Typed data flows forward via [WizardStep.SelectLevel.role]. */
    @AutoRegister
    class SelectRoleStep : ActivityScreen<WizardStep.SelectRole>() {
        @Composable
        override fun Content(navKey: WizardStep.SelectRole, navEaseController: NavEaseController) {
            WizardStepScaffold(
                stepNumber = 1,
                totalSteps = 4,
                title      = "What's your role?",
                subtitle   = "Your pick travels to the next step as a typed NavKey argument — no strings, no bundles.",
                breadcrumb = "WizardStep.SelectRole",
            ) {
                WizardCodePanel(
                    "// Typed forward navigation:\n" +
                    "navEaseController.navigate(\n" +
                    "    WizardStep.SelectLevel(role = \"Mobile\"),\n" +
                    "    navTransition = NavTransition.Push,\n" +
                    ")"
                )
                Spacer(Modifier.height(16.dp))

                val roles = listOf(
                    "Frontend"   to "🎨",
                    "Backend"    to "⚙️",
                    "Full Stack" to "🔗",
                    "Mobile"     to "📱",
                )
                roles.forEachIndexed { index, (role, emoji) ->
                    val container   = containerColorAt(index)
                    val onContainer = onContainerColorAt(index)
                    val accent      = accentColorAt(index)
                    OutlinedButton(
                        onClick  = {
                            navEaseController.navigate(
                                WizardStep.SelectLevel(role = role),
                                navTransition = NavTransition.Push,
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(14.dp),
                        colors   = ButtonDefaults.outlinedButtonColors(
                            containerColor = container.copy(alpha = 0.50f),
                        ),
                    ) {
                        Text(emoji, style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            role,
                            style      = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color      = onContainer,
                            modifier   = Modifier.weight(1f),
                        )
                        Text("›", style = MaterialTheme.typography.titleLarge, color = accent)
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }

    // ── Step 2 : Select Level ─────────────────────────────────────────────────

    /**
     * Receives [WizardStep.SelectLevel.role] from Step 1 and carries role + level forward to
     * [WizardStep.SelectTech].
     *
     * Contains **Stack Persistence Test #1**: navigates the outer root controller to
     * [AppScreens.Transitions]. Pressing back returns to this exact step — [navKey.role]
     * is still alive inside the inner back-stack.
     *
     * The outer controller is obtained from [LocalOuterNavEaseController] — no constructor arg needed.
     */
    @AutoRegister
    class SelectLevelStep : ActivityScreen<WizardStep.SelectLevel>() {
        @Composable
        override fun Content(navKey: WizardStep.SelectLevel, navEaseController: NavEaseController) {
            // Outer (root) controller — provided by NestedNavDemoScreen via CompositionLocal
            val outerNav = LocalOuterNavEaseController.current ?: navEaseController
            WizardStepScaffold(
                stepNumber = 2,
                totalSteps = 4,
                title      = "Experience level",
                subtitle   = "\"${navKey.role}\" arrived in this step's NavKey — fully typed, zero casting.",
                breadcrumb = "WizardStep.SelectLevel(role = \"${navKey.role}\")",
                onBack     = { navEaseController.back() },
            ) {
                val levels = listOf(
                    Triple("Junior", "0 – 2 years",  "🌱"),
                    Triple("Mid",    "2 – 5 years",  "🌿"),
                    Triple("Senior", "5 – 10 years", "🌳"),
                    Triple("Lead",   "10+ years",    "🏆"),
                )
                levels.forEachIndexed { index, (level, years, emoji) ->
                    val container   = containerColorAt(index + 1)
                    val onContainer = onContainerColorAt(index + 1)
                    val accent      = accentColorAt(index + 1)
                    OutlinedButton(
                        onClick  = {
                            navEaseController.navigate(
                                WizardStep.SelectTech(role = navKey.role, level = level),
                                navTransition = NavTransition.Rise,
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(14.dp),
                        colors   = ButtonDefaults.outlinedButtonColors(
                            containerColor = container.copy(alpha = 0.50f),
                        ),
                    ) {
                        Text(emoji, style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                level,
                                style      = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color      = onContainer,
                            )
                            Text(
                                years,
                                style = MaterialTheme.typography.labelSmall,
                                color = onContainer.copy(alpha = 0.65f),
                            )
                        }
                        Text("›", style = MaterialTheme.typography.titleLarge, color = accent)
                    }
                    Spacer(Modifier.height(8.dp))
                }

                Spacer(Modifier.height(20.dp))

                StackTestPanel(
                    heading     = "🧪  Stack Persistence Test — inner stack has 2 entries",
                    description =
                        "Inner back-stack right now:\n" +
                        "  [1] WizardStep.SelectRole\n" +
                        "  [2] WizardStep.SelectLevel  ←  you are here\n\n" +
                        "Tap below to navigate the ROOT host to Transitions.\n" +
                        "Come back with the system back gesture — you'll land on\n" +
                        "this exact step with \"${navKey.role}\" still in the NavKey.",
                    buttonLabel = "→  Jump to Transitions (then press back!)",
                    onTest      = {
                        outerNav.navigate(AppScreens.Transitions, navTransition = NavTransition.Fade)
                    },
                )
            }
        }
    }

    // ── Step 3 : Select Tech Stack ────────────────────────────────────────────

    /**
     * Receives [WizardStep.SelectTech.role] + [WizardStep.SelectTech.level] from Step 2
     * and forwards all three values to [WizardStep.Summary].
     *
     * Contains **Stack Persistence Test #2**: navigates the outer root controller to
     * [AppScreens.Home]. Pressing back returns to this step with role + level still intact.
     *
     * The outer controller is obtained from [LocalOuterNavEaseController] — no constructor arg needed.
     */
    @AutoRegister
    class SelectTechStep : ActivityScreen<WizardStep.SelectTech>() {
        @Composable
        override fun Content(navKey: WizardStep.SelectTech, navEaseController: NavEaseController) {
            val outerNav = LocalOuterNavEaseController.current ?: navEaseController
            WizardStepScaffold(
                stepNumber = 3,
                totalSteps = 4,
                title      = "Preferred tech stack",
                subtitle   = "\"${navKey.role}\" and \"${navKey.level}\" are both alive in the inner back-stack.",
                breadcrumb = "WizardStep.SelectTech(role, level)",
                onBack     = { navEaseController.back() },
            ) {
                WizardCodePanel(
                    "// Both args arrived through the inner back-stack:\n" +
                    "navKey.role   // \"${navKey.role}\"\n" +
                    "navKey.level  // \"${navKey.level}\""
                )
                Spacer(Modifier.height(16.dp))

                val techs = listOf(
                    "Kotlin Multiplatform" to "🚀",
                    "Jetpack Compose"      to "🎨",
                    "SwiftUI"              to "🍎",
                    "React Native"         to "⚛️",
                    "Flutter"              to "🐦",
                )
                techs.forEachIndexed { index, (tech, emoji) ->
                    val container   = containerColorAt(index)
                    val onContainer = onContainerColorAt(index)
                    val accent      = accentColorAt(index)
                    OutlinedButton(
                        onClick  = {
                            navEaseController.navigate(
                                WizardStep.Summary(
                                    role  = navKey.role,
                                    level = navKey.level,
                                    tech  = tech,
                                ),
                                navTransition = NavTransition.Rise,
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(14.dp),
                        colors   = ButtonDefaults.outlinedButtonColors(
                            containerColor = container.copy(alpha = 0.50f),
                        ),
                    ) {
                        Text(emoji, style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            tech,
                            style      = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color      = onContainer,
                            modifier   = Modifier.weight(1f),
                        )
                        Text("›", style = MaterialTheme.typography.titleLarge, color = accent)
                    }
                    Spacer(Modifier.height(8.dp))
                }

                Spacer(Modifier.height(20.dp))

                StackTestPanel(
                    heading     = "🧪  Stack Persistence Test — inner stack has 3 entries",
                    description =
                        "Inner back-stack right now:\n" +
                        "  [1] WizardStep.SelectRole\n" +
                        "  [2] WizardStep.SelectLevel(\"${navKey.role}\")\n" +
                        "  [3] WizardStep.SelectTech  ←  you are here\n\n" +
                        "Navigate to Home (a completely different root screen) and come back.\n" +
                        "All 3 entries — including role and level values — will still be intact.",
                    buttonLabel = "→  Jump to Home (then press back!)",
                    onTest      = {
                        outerNav.navigate(AppScreens.Home, navTransition = NavTransition.Zoom)
                    },
                )
            }
        }
    }

    // ── Step 4 : Summary ──────────────────────────────────────────────────────

    /**
     * Final step. Shows all three typed values that flowed through the inner back-stack.
     *
     * The outer controller is obtained from [LocalOuterNavEaseController] — no constructor arg needed.
     * `onFinish` calls `outerNav.back()` to pop [AppScreens.NestedNavDemo].
     */
    @AutoRegister
    class SummaryStep : ActivityScreen<WizardStep.Summary>() {
        @Composable
        override fun Content(navKey: WizardStep.Summary, navEaseController: NavEaseController) {
            val outerNav = LocalOuterNavEaseController.current ?: navEaseController
            WizardStepScaffold(
                stepNumber = 4,
                totalSteps = 4,
                title      = "Profile Complete 🎉",
                subtitle   = "All 3 choices flowed through the inner back-stack — none were lost.",
                breadcrumb = "WizardStep.Summary(role, level, tech)",
                onBack     = { navEaseController.back() },
            ) {
                // ── Profile card ────────────────────────────────────────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(20.dp),
                    colors   = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                ) {
                    Column(Modifier.padding(24.dp)) {
                        Text(
                            "DEVELOPER PROFILE",
                            style      = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.65f),
                        )
                        Spacer(Modifier.height(16.dp))
                        ProfileRow(label = "Role",  value = navKey.role)
                        Spacer(Modifier.height(10.dp))
                        ProfileRow(label = "Level", value = navKey.level)
                        Spacer(Modifier.height(10.dp))
                        ProfileRow(label = "Stack", value = navKey.tech)
                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f),
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text =
                                "// All 3 args arrived via typed NavKey:\n" +
                                "WizardStep.Summary(\n" +
                                "    role  = \"${navKey.role}\",\n" +
                                "    level = \"${navKey.level}\",\n" +
                                "    tech  = \"${navKey.tech}\",\n" +
                                ")",
                            style      = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color      = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.80f),
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // ── Primary exit: finish wizard ─────────────────────────────
                Button(
                    onClick  = { outerNav.back() },
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Text(
                        "✓  Finish Wizard & Exit",
                        fontWeight = FontWeight.Bold,
                        modifier   = Modifier.padding(vertical = 4.dp),
                    )
                }

                Spacer(Modifier.height(10.dp))

                // ── Secondary exit: jump to root Home ───────────────────────
                OutlinedButton(
                    onClick  = {
                        outerNav.navigate(AppScreens.Home, navTransition = NavTransition.Zoom)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(14.dp),
                ) {
                    Text("🏠  Jump to Home (skips outer back-pop)")
                }

                Spacer(Modifier.height(10.dp))

                // ── Inner back ──────────────────────────────────────────────
                OutlinedButton(
                    onClick  = { navEaseController.back() },
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(14.dp),
                ) {
                    Text("← Back to Step 3")
                }

                Spacer(Modifier.height(20.dp))

                // ── Explanation of exit paths ────────────────────────────────
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(12.dp),
                    color    = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text(
                            "Two exit paths — same outer back-stack:",
                            style      = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text =
                                "// ✓ Finish Wizard:\n" +
                                "outerNav.back()  // pops NestedNavDemo\n\n" +
                                "// 🏠 Jump to Home:\nouterNav.navigate(\n" +
                                "    AppScreens.Home,\n" +
                                "    navTransition = NavTransition.Zoom,\n" +
                                ")",
                            style      = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color      = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }

    // ── Outer screen Content ──────────────────────────────────────────────────

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content(
        navKey: AppScreens.NestedNavDemo,
        navEaseController: NavEaseController,   // ← root (outer) controller
    ) {
        val outerNav = navEaseController

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("Nested NavEaseHost", fontWeight = FontWeight.SemiBold)
                            Text(
                                "4-step wizard · isolated inner back-stack · stack-persistence tests",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                    navigationIcon = { NavBackButton(onClick = { outerNav.back() }) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                )
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                // ── Architecture diagram ────────────────────────────────────
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                ) {
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Text(
                            "// NavEaseHost architecture",
                            style      = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.60f),
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text =
                                "NavEaseHost(start = AppScreens.Splash) {  // root host\n" +
                                "    ...\n" +
                                "    NestedNavDemoScreen()  // ← this outer screen\n" +
                                "    //  NavEaseHost<WizardStep>(  // inner host\n" +
                                "    //      start = WizardStep.SelectRole,\n" +
                                "    //      onExitRequest = { outerNav.back() }\n" +
                                "    //  )",
                            style      = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color      = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }

                // ── Inner NavEaseHost ───────────────────────────────────────
                // CompositionLocalProvider makes outerNav available to all inner
                // wizard step screens via LocalOuterNavEaseController — no constructor
                // args required, enabling full @AutoRegister auto-discovery.
                Box(modifier = Modifier.fillMaxSize()) {
                    CompositionLocalProvider(LocalOuterNavEaseController provides outerNav) {
                        NavEaseHost<WizardStep>(
                            start         = WizardStep.SelectRole,
                            onExitRequest = { outerNav.back() },
                            navTransition = NavTransition.Push,
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Shared wizard UI helpers (private to this file)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Layout scaffold shared by all wizard steps. Provides:
 * - Breadcrumb label showing the current [WizardStep] key
 * - Segmented progress bar (filled = completed, half = current, outline = upcoming)
 * - "Step N of M" counter with an optional back [TextButton]
 * - Title + subtitle
 * - Vertically scrollable [content] area
 */
@Composable
private fun WizardStepScaffold(
    stepNumber: Int,
    totalSteps: Int,
    title: String,
    subtitle: String,
    breadcrumb: String,
    onBack: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        // ── Breadcrumb ──────────────────────────────────────────────────────
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.60f),
        ) {
            Text(
                text       = "AppScreens.NestedNavDemo  ›  $breadcrumb",
                style      = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color      = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier   = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            )
        }

        Spacer(Modifier.height(16.dp))

        // ── Segmented progress bar ──────────────────────────────────────────
        Row(
            modifier             = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment    = Alignment.CenterVertically,
        ) {
            repeat(totalSteps) { i ->
                val seg = i + 1
                Box(
                    modifier = Modifier
                        .height(7.dp)
                        .weight(1f)
                        .background(
                            color = when {
                                seg < stepNumber  -> MaterialTheme.colorScheme.primary
                                seg == stepNumber -> MaterialTheme.colorScheme.primary.copy(alpha = 0.50f)
                                else              -> MaterialTheme.colorScheme.outlineVariant
                            },
                            shape = RoundedCornerShape(50),
                        ),
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // ── "Step N of M" + optional back button ───────────────────────────
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Text(
                text       = "Step $stepNumber of $totalSteps",
                style      = MaterialTheme.typography.labelMedium,
                color      = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
            if (onBack != null) {
                TextButton(
                    onClick        = onBack,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                ) {
                    Text("← Back", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text       = title,
            style      = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text  = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(24.dp))

        content()
    }
}

/** Monospace code block illustrating the NavEase API call between wizard steps. */
@Composable
private fun WizardCodePanel(code: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(12.dp),
        color    = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Text(
            text       = code,
            style      = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color      = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier   = Modifier.padding(14.dp),
        )
    }
}

/**
 * A prominently styled "tertiary container" panel that explains the stack-persistence
 * concept and provides a button to navigate to a root screen so the user can confirm
 * first-hand that the inner host's back-stack is preserved independently.
 */
@Composable
private fun StackTestPanel(
    heading: String,
    description: String,
    buttonLabel: String,
    onTest: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        ),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text       = heading,
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onTertiaryContainer,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text  = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f),
            )
            Spacer(Modifier.height(14.dp))
            Button(
                onClick  = onTest,
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(10.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor   = MaterialTheme.colorScheme.onTertiary,
                ),
            ) {
                Text(buttonLabel, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

/** A labelled value row for the Summary profile card. */
@Composable
private fun ProfileRow(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.10f),
        ) {
            Text(
                text       = label.uppercase(),
                style      = MaterialTheme.typography.labelSmall,
                color      = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.65f),
                fontWeight = FontWeight.Bold,
                modifier   = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text       = value,
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}
