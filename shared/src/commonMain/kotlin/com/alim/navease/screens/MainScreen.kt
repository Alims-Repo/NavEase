package com.alim.navease.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.alimsrepo.navease.generated.AppScreens
import io.github.alimsrepo.navease.runtime.NavEaseArgs
import io.github.alimsrepo.navease.runtime.NavEaseResult
import io.github.alimsrepo.navease.runtime.NavEaseScreen
import io.github.alimsrepo.navease.generated.backWithMainResult
import io.github.alimsrepo.navease.runtime.data.NavController
import io.github.alimsrepo.navease.runtime.domain.NavScreen

data class SampleData(val name: String, val count: Int)

@NavEaseScreen(route = "Main")
class MainScreen  : NavScreen<AppScreens.Main>() {

    @NavEaseArgs
    data class Args(val userId: String, val age: Int)

    @NavEaseResult
    data class Result(val value: Int, val sampleData: SampleData)

    @Composable
    override fun Content(
        navKey: AppScreens.Main,
        navController: NavController
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Main Screen ${navKey.userId}, ${navKey.age}",
                    style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
                )

                FlowRow {
                    Button(
                        onClick = {
                            navController.backWithMainResult(value = 1, sampleData = SampleData("Sample", 42))
                        }
                    ) {
                        Text("Return -> 1")
                    }

                    Button(
                        onClick = {
                            navController.backWithMainResult(value = 2, sampleData = SampleData("Sample", 42))
                        }
                    ) {
                        Text("Return -> 2")
                    }

                    Button(
                        onClick = {
                            navController.backWithMainResult(value = 3, sampleData = SampleData("Sample", 42))
                        }
                    ) {
                        Text("Return -> 3")
                    }
                }
            }
        }
    }

}