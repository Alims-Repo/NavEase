package io.github.alimsrepo.navease

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A translucent debug overlay that displays the current navigation back stack.
 *
 * **Only include this in debug builds** — it has no production value and will show
 * internal route information to end users.
 *
 * ### Usage
 * ```kotlin
 * NavEaseHostInternal(
 *     startDestination = AppScreens.Splash,
 *     factory = GeneratedNavEaseFactory,
 *     debugOverlay = BuildConfig.DEBUG,
 * )
 * ```
 *
 * @param backStack The current back stack from [NavEaseController.backStack].
 */
@Composable
internal fun DebugBackStackOverlay(backStack: List<NavEaseKey>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(Color(0xCC000000))
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(
            text = "NavEase Back Stack",
            color = Color(0xFF00FF88),
            fontSize = 10.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        )
        HorizontalDivider(color = Color(0xFF00FF88), thickness = 0.5.dp)
        backStack.forEachIndexed { index, key ->
            val isTop = index == backStack.lastIndex
            Text(
                text = "${if (isTop) "▶ " else "  "}[$index] ${key::class.simpleName} — $key",
                color = if (isTop) Color.White else Color(0xAAFFFFFF),
                fontSize = 9.sp,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

