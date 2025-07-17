package com.scanner.app.presentation.camera

import CameraPreviewViewModel
import android.gesture.Gesture
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.viewfinder.compose.MutableCoordinateTransformer
import androidx.camera.viewfinder.core.ImplementationMode
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.scanner.app.R

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPreviewScreen(
    modifier: Modifier = Modifier,
) {
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
    if (cameraPermissionState.status.isGranted) {
        val viewModel: CameraPreviewViewModel = viewModel() // Use a função viewModel()
        CameraPreviewContent(viewModel = viewModel, modifier = modifier)
    } else {
        Column(
            modifier = modifier.fillMaxSize().wrapContentSize().widthIn(max = 480.dp),
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
    viewModel: CameraPreviewViewModel,
    modifier: Modifier = Modifier,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    ) {
        val surfaceRequest by viewModel.surfaceRequest.collectAsStateWithLifecycle()
        val context = LocalContext.current

        LaunchedEffect(lifecycleOwner) {
            viewModel.bindToCamera(context.applicationContext, lifecycleOwner)
        }

    Box(modifier = modifier) { // This Box now takes the Modifier.fillMaxSize()
        surfaceRequest?.let { request ->
            CameraXViewfinder(
                surfaceRequest = request,
                modifier = Modifier.fillMaxSize(), // Camera preview fills the entire Box
                implementationMode = ImplementationMode.EXTERNAL,
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter) // Alinha ao centro inferior da Box
                .padding(bottom = 32.dp) // Adiciona um padding da parte inferior da tela
                .size(72.dp) // Tamanho total do círculo externo
                .border(BorderStroke(4.dp, Color.White), CircleShape) // Borda branca
                .background(Color.Black.copy(alpha = 0.5f), CircleShape) // Fundo preto semi-transparente
                .clickable {
                    // TODO: Implementar a lógica para tirar a foto aqui!
                    // Por exemplo: viewModel.takePicture()
                    println("Botão de foto clicado!")
                }
        ) {
            // Se quiser um círculo interno branco, você pode adicionar outro Box aqui
            Box(
                modifier = Modifier
                    .fillMaxSize(0.8f) // Ocupa 80% do tamanho do pai (círculo externo)
                    .align(Alignment.Center) // Centraliza dentro do pai
                    .background(Color.White, CircleShape) // Círculo interno branco
            )
        }

        IconButton (
            onClick = { viewModel.switchCamera() },
            modifier = Modifier
                .align(Alignment.BottomEnd) // Align to top-right corner of the Box
                .padding(end = 16.dp, bottom = 40.dp) // Adjust padding to clear status bar and edge
        ) {
            val imageVector = ImageVector.vectorResource(id = R.drawable.flip_camera_ios_24dp)
            Icon(imageVector, contentDescription = "Alternar Câmera")
        }
    }

}