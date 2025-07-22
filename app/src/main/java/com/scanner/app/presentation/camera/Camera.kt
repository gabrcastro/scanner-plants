package com.scanner.app.presentation.camera

import CameraPreviewViewModel
import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.viewfinder.core.ImplementationMode
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.scanner.app.R
import com.scanner.app.ui.theme.White

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPreviewScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    setAppBarVisibility: (Boolean) -> Unit,
) {
    val view = LocalView.current
    val window = (view.context as? Activity)?.window

    DisposableEffect(key1 = window) { // Re-run if window instance changes
        val windowInsetsController = window?.let { WindowCompat.getInsetsController(it, view) }

        if (windowInsetsController != null) {
            // Hide both status and navigation bars
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars()) // Key change

            // Configure behavior for immersive mode (user can swipe to temporarily show them)
            windowInsetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

            // Signal that the TopAppBar (custom app bar) should also be hidden
            setAppBarVisibility(false)
        }

        onDispose {
            // This block is the cleanup for DisposableEffect
            if (windowInsetsController != null) {
                // Show both status and navigation bars again when leaving the screen
                windowInsetsController.show(WindowInsetsCompat.Type.systemBars()) // Key change
            }
            // Signal that the TopAppBar can be shown again
            setAppBarVisibility(true)
        }
    }

    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
    if (cameraPermissionState.status.isGranted) {
        val viewModel: CameraPreviewViewModel = viewModel() // Use a função viewModel()
        CameraPreviewContent(viewModel = viewModel, navController = navController)
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .wrapContentSize()
                .widthIn(max = 480.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val textToShow = if (cameraPermissionState.status.shouldShowRationale) {
                "Whoops! Looks like we need your camera to work our magic!" +
                        "Don't worry, we just wanna see your pretty face (and maybe some cats).  " +
                        "Grant us permission and let's get this party started!"
            } else {
                "Hi there! We need your camera to work our magic! ✨\n" +
                        "Grant us permission and let's get this party started! \uD83C\uDF89"
            }
            Text(textToShow, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                Text("Unleash the Camera!")
            }
        }
    }
}

@Composable
fun CameraPreviewContent(
    navController: NavController,
    viewModel: CameraPreviewViewModel,
    modifier: Modifier = Modifier,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    ) {
        val surfaceRequest by viewModel.surfaceRequest.collectAsStateWithLifecycle()
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        var isCapturing by remember { mutableStateOf(false) }

        LaunchedEffect(lifecycleOwner) {
            viewModel.bindToCamera(context.applicationContext, lifecycleOwner)
        }

    Box(modifier = modifier) {
        surfaceRequest?.let { request ->
            CameraXViewfinder(
                surfaceRequest = request,
                modifier = Modifier.fillMaxSize(),
                implementationMode = ImplementationMode.EXTERNAL,
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(vertical = 40.dp, horizontal = 16.dp)
        ) {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = White
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            if (isCapturing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(72.dp),
                    color = White
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .border(BorderStroke(4.dp, Color.White), CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .clickable(enabled = !isCapturing) { // Disable click while capturing
                            isCapturing = true
                            viewModel.takePhoto { success, uriString, error ->
                                isCapturing = false // Reset loading state
                                if (success) {
                                    Log.d("CameraScreen", "Photo saved: $uriString")
                                    Toast.makeText(context, "Photo saved!", Toast.LENGTH_SHORT).show()
                                    // Optionally navigate or do something with the URI
                                } else {
                                    Log.e("CameraScreen", "Error saving photo: $error")
                                    Toast.makeText(context, "Error: $error", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                ) {
                    Box( // Inner white circle (visual only)
                        modifier = Modifier
                            .fillMaxSize(0.8f)
                            .align(Alignment.Center)
                            .background(Color.White, CircleShape)
                    )
                }
            }
        }

        IconButton (
            onClick = { viewModel.switchCamera() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 40.dp)
        ) {
            val imageVector = ImageVector
                .vectorResource(id = R.drawable.flip_camera_ios_24dp)
            Icon(imageVector, tint = White, contentDescription = "Alternar Câmera")
        }
    }

}