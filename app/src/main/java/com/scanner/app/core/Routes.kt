package com.scanner.app.core

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.scanner.app.presentation.camera.CameraPreviewScreen
import com.scanner.app.presentation.home.HomeScreen

object AppDestinations {
    const val HOME_ROUTE = "home"
    const val CAMERA_PREVIEW_ROUTE = "camera_preview"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppDestinations.HOME_ROUTE
    ) {
        composable(route = AppDestinations.HOME_ROUTE) {
            HomeScreen(navController = navController)
        }
        composable(route = AppDestinations.CAMERA_PREVIEW_ROUTE) {
            CameraPreviewScreen(
                setAppBarVisibility = { true },
                navController = navController
            )
        }
    }
}