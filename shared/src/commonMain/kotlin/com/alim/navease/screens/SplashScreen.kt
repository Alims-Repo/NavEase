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
import io.github.alimsrepo.navease.runtime.navigation.NavController
import io.github.alimsrepo.navease.runtime.domain.NavScreen
import kotlinx.coroutines.delay

@NavEaseScreen(route = "Splash", startDestination = true)
class SplashScreen : NavScreen() {

    @Composable
    override fun Content(
        navKey: NavKey,
        navController: NavController
    ) {
        val result by navController.mainResult()

        // rememberSaveable survives back-stack changes — prevents infinite re-navigation
        var hasNavigated by rememberSaveable { mutableStateOf(false) }

        LaunchedEffect(hasNavigated) {
            if (!hasNavigated) {
                delay(2200L)
                hasNavigated = true
                navController.navigateToMain(userId = "alim_dev", age = 27)
            }
        }

        // Entrance animation
        var visible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { visible = true }

        val scale by animateFloatAsState(
            targetValue = if (visible) 1f else 0.6f,
            animationSpec = spring(dampingRatio = 0.55f, stiffness = 300f),
            label = "splash_scale"
        )
        val alpha by animateFloatAsState(
            targetValue = if (visible) 1f else 0f,
            animationSpec = tween(600),
            label = "splash_alpha"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            // Center brand block
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                }
            ) {
                // Monogram badge
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "NE",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "NavEase",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "KSP-Powered Navigation for KMP",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
                )

                Spacer(Modifier.height(56.dp))

                BouncingDots(color = MaterialTheme.colorScheme.onPrimary)
            }

            // Result banner — shown when returning from Main with a result
            result?.let {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 24.dp, vertical = 48.dp),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shadowElevation = 4.dp
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
                            text = "Result received from Main  ·  value = ${it.value}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // Library credits — bottom
            Text(
                text = "navease-runtime · navease-ksp",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            )
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
                targetValue = -10f,
                animationSpec = infiniteRepeatable(
                    animation = tween(400, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(index * 140)
                ),
                label = "dot_$index"
            )
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .offset(y = offsetY.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.85f))
            )
        }
    }
}
