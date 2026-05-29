package com.alim.navease.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import io.github.alimsrepo.navease.generated.mainResult
import io.github.alimsrepo.navease.generated.navigateToMain
import io.github.alimsrepo.navease.runtime.annotations.NavEaseScreen
import io.github.alimsrepo.navease.runtime.domain.NavScreen
import io.github.alimsrepo.navease.runtime.navigation.NavController
import kotlinx.coroutines.delay

@NavEaseScreen(route = "Splash", startDestination = true)
class SplashScreen : NavScreen() {

    @Composable
    override fun Content(navKey: NavKey, navController: NavController) {
        val result by navController.mainResult()

        var hasNavigated by rememberSaveable { mutableStateOf(false) }
        LaunchedEffect(hasNavigated) {
            if (!hasNavigated) {
                delay(2_400L)
                hasNavigated = true
                navController.navigateToMain(userId = "kmp_dev", age = 2021)
            }
        }

        var visible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { visible = true }

        val scale by animateFloatAsState(
            targetValue = if (visible) 1f else 0.55f,
            animationSpec = spring(dampingRatio = 0.52f, stiffness = 280f),
            label = "splash_scale"
        )
        val alpha by animateFloatAsState(
            targetValue = if (visible) 1f else 0f,
            animationSpec = tween(500),
            label = "splash_alpha"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.graphicsLayer {
                    scaleX = scale; scaleY = scale; this.alpha = alpha
                }
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "KH",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Spacer(Modifier.height(28.dp))

                Text(
                    text = "KMP Hub",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "Discover · Explore · Build with Kotlin",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
                )

                Spacer(Modifier.height(14.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("Android", "iOS", "Desktop", "Web").forEach { platform ->
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f)
                        ) {
                            Text(
                                text = platform,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(52.dp))
                BouncingDots(color = MaterialTheme.colorScheme.onPrimary)
            }

            // Result banner when returning from home
            result?.let {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 24.dp, vertical = 52.dp),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shadowElevation = 6.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondary)
                        )
                        Text(
                            text = when (it.value) {
                                1 -> "You rated 1 ⭐ — thanks for the feedback!"
                                2 -> "You rated 2 ⭐⭐ — we're improving fast!"
                                else -> "You rated 3 ⭐⭐⭐ — you made our day! 🎉"
                            },
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Powered by NavEase",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.55f)
                )
                Text(
                    text = "navease-runtime · navease-ksp",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.35f)
                )
            }
        }
    }
}

@Composable
private fun BouncingDots(color: Color) {
    val transition = rememberInfiniteTransition(label = "dots")
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        repeat(3) { index ->
            val offsetY by transition.animateFloat(
                initialValue = 0f,
                targetValue = -12f,
                animationSpec = infiniteRepeatable(
                    animation = tween(380, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(index * 130)
                ),
                label = "dot_$index"
            )
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .offset(y = offsetY.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.80f))
            )
        }
    }
}
