package com.heard.heartdisease

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// ✅ IMPORTANT IMPORTS
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.heard.heartdisease.ui.navigation.Routes
import com.heard.heartdisease.ui.screens.*
import com.heard.heartdisease.ui.theme.HeartAppTheme

// ✅ FIXED IMPORTS
import com.heard.heartdisease.ui.viewmodel.PredictionViewModel
import com.heard.heartdisease.ui.viewmodel.VmFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            HeartAppTheme {

                val app = application as HeartApplication
                val vm: PredictionViewModel = viewModel(factory = VmFactory(app))
                val nav = rememberNavController()

                NavHost(navController = nav, startDestination = Routes.SPLASH) {

                    composable(Routes.SPLASH) {
                        SplashScreen {
                            nav.navigate(Routes.HOME) {
                                popUpTo(Routes.SPLASH) { inclusive = true }
                            }
                        }
                    }

                    composable(Routes.HOME) {
                        HomeScreen(
                            vm = vm,
                            onPredict = { nav.navigate(Routes.INPUT) },
                            onHistory = { nav.navigate(Routes.HISTORY) }
                        )
                    }

                    composable(Routes.INPUT) {
                        InputScreen(
                            vm = vm,
                            onBack = { nav.popBackStack() },
                            onScan = { nav.navigate(Routes.SCAN) },
                            onResult = { nav.navigate(Routes.RESULT) }
                        )
                    }

                    composable(Routes.SCAN) {
                        ScanScreen(
                            onBack = { nav.popBackStack() },
                            onHeartRate = { bpm ->
                                vm.setScannedHeartRate(bpm)
                                nav.popBackStack()
                            }
                        )
                    }

                    composable(Routes.RESULT) {
                        val r = vm.result.collectAsStateWithLifecycle().value

                        if (r != null) {
                            ResultScreen(result = r) {
                                nav.navigate(Routes.HOME) {
                                    popUpTo(0)
                                }
                            }
                        } else {
                            Text(
                                "No result available.",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp)
                            )
                        }
                    }

                    composable(Routes.HISTORY) {
                        HistoryScreen(vm = vm, onBack = { nav.popBackStack() })
                    }
                }
            }
        }
    }
}