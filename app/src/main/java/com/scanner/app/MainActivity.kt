package com.scanner.app

import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import com.scanner.app.presentation.home.HomeScreen
import com.scanner.app.ui.theme.ScannerAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Dizer ao sistema para desenhar o conteúdo atrás das barras do sistema
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 2. Ocultar as barras de status e navegação (Método moderno para API 30+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let {
                it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            // 3. Ocultar as barras de status e navegação (Método para APIs mais antigas < 30)
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY // Garante que as barras não apareçam facilmente
                            or android.view.View.SYSTEM_UI_FLAG_FULLSCREEN    // Oculta a barra de status
                            or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // Oculta a barra de navegação
                    )
        }

        setContent {
            ScannerAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HomeScreen(padding = innerPadding)
                }
            }
        }
    }
}