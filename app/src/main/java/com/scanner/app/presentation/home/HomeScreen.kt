package com.scanner.app.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.scanner.app.core.AppDestinations
import com.scanner.app.ui.theme.Green
import com.scanner.app.ui.theme.White

@Composable
fun HomeScreen(navController: NavController) {
    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(AppDestinations.CAMERA_PREVIEW_ROUTE) },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Escanear",
                        tint = White
                    )
                },
                text = {
                    Text(
                        text = "Escanear",
                        color = White
                    )
                },
                containerColor = Green,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Bottom
        ) {
//            CameraPreviewScreen(
//                setAppBarVisibility = { true }
//            )
        }
    }
}