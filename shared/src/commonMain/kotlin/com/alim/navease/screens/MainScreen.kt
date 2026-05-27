package com.alim.navease.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import io.github.alimsrepo.navease.runtime.annotations.NavEaseArgs
import io.github.alimsrepo.navease.runtime.annotations.NavEaseResult
import io.github.alimsrepo.navease.runtime.annotations.NavEaseScreen
import io.github.alimsrepo.navease.generated.backWithMainResult
import io.github.alimsrepo.navease.generated.mainArgs
import io.github.alimsrepo.navease.runtime.navigation.NavController
import io.github.alimsrepo.navease.runtime.domain.NavScreen

@NavEaseScreen(route = "Main")
class MainScreen  : NavScreen() {

    @NavEaseArgs
    data class Args(val userId: String, val age: Int)

    @NavEaseResult
    data class Result(val value: Int)

    @Composable
    override fun Content(
        navKey: NavKey,
        navController: NavController
    ) {
        val args = navKey.mainArgs()
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Main Screen ${args.userId}, ${args.age}",
                    style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
                )

                FlowRow {
                    Button(
                        onClick = {
                            navController.backWithMainResult(value = 1)
                        }
                    ) {
                        Text("Return -> 0")
                    }

                    Button(
                        onClick = {
                            navController.backWithMainResult(value = 2)
                        }
                    ) {
                        Text("Return -> 1")
                    }

                    Button(
                        onClick = {
                            navController.backWithMainResult(value = 3)
                        }
                    ) {
                        Text("Return -> 2")
                    }

                    Button(
                        onClick = {
                            navController.back()
                        }
                    ) {
                        Text("Return -Nothing")
                    }
                }
            }
        }
    }
}