package com.example.fittrack

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fittrack.ui.screens.active.ActiveTrackingScreen
import com.example.fittrack.ui.screens.active.ActiveTrackingViewModel
import com.example.fittrack.ui.screens.history.RunHistoryScreen
import com.example.fittrack.ui.screens.history.RunHistoryViewModel
import com.example.fittrack.ui.screens.home.HomeScreen
import com.example.fittrack.ui.screens.home.HomeViewModel
import com.example.fittrack.ui.theme.FitTrackTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as FitTrackApp
        val homeViewModel: HomeViewModel by viewModels { HomeViewModel.Factory(app.repository) }
        val trackingViewModel: ActiveTrackingViewModel by viewModels { ActiveTrackingViewModel.Factory(app.repository) }
        val historyViewModel: RunHistoryViewModel by viewModels { RunHistoryViewModel.Factory(app.repository) }

        setContent {
            FitTrackTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigation(
                        homeViewModel = homeViewModel,
                        trackingViewModel = trackingViewModel,
                        historyViewModel = historyViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun MainNavigation(
    homeViewModel: HomeViewModel,
    trackingViewModel: ActiveTrackingViewModel,
    historyViewModel: RunHistoryViewModel
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    var hasPermissions by remember {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        mutableStateOf(fineLocationGranted)
    }

    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        hasPermissions = fineGranted || coarseGranted
    }

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionsLauncher.launch(permissionsToRequest.toTypedArray())
    }

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                viewModel = homeViewModel,
                onStartRunClick = {
                    if (hasPermissions) {
                        navController.navigate("active_tracking")
                    } else {
                        val permissionsToRequest = mutableListOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        permissionsLauncher.launch(permissionsToRequest.toTypedArray())
                    }
                },
                onViewAllHistoryClick = {
                    navController.navigate("history")
                }
            )
        }

        composable("active_tracking") {
            ActiveTrackingScreen(
                viewModel = trackingViewModel,
                onRunFinished = {
                    navController.popBackStack()
                },
                onCloseClick = {
                    navController.popBackStack()
                }
            )
        }

        composable("history") {
            RunHistoryScreen(
                viewModel = historyViewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
