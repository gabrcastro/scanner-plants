package com.scanner.app.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Scan
import com.composables.icons.lucide.Settings2
import com.scanner.app.R
import com.scanner.app.core.AppDestinations
import com.scanner.app.ui.theme.FuturisticEndCyan
import com.scanner.app.ui.theme.FuturisticMidGreen
import com.scanner.app.ui.theme.FuturisticStartBlue
import com.scanner.app.ui.theme.Green
import com.scanner.app.ui.theme.White

@Composable
fun HomeScreen(navController: NavController) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(FuturisticStartBlue, FuturisticMidGreen, FuturisticEndCyan),
                        start = Offset(0f, 0f), // Top-left
                        end = Offset(
                            Float.POSITIVE_INFINITY,
                            Float.POSITIVE_INFINITY
                        ) // Bottom-right
                    )
                )
                .padding(innerPadding),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Hello, [username]",
                color = White,
                modifier = Modifier.padding(top = 30.dp, start = 24.dp),
                fontSize = 28.sp,
                fontWeight = FontWeight.W500,
            )

            Box (
                modifier = Modifier
                    .padding(24.dp)
                    .align(Alignment.Start),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {

                        IconButton(
                            onClick = { /* TODO */ },
                            modifier = Modifier
                                .clip(CircleShape)
                                .padding(3.dp)
                        ) {
                            Image(Lucide.Settings2,
                                contentDescription = "Settings",
                                colorFilter = ColorFilter.tint(Color.White),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.fillMaxHeight(0.01f))

                        IconButton(
                            onClick = { /* TODO */ },
                            modifier = Modifier
                                .clip(CircleShape)
                                .padding(3.dp)
                        ) {
                            Image(
                                Lucide.Info,
                                contentDescription = "Information",
                                colorFilter = ColorFilter.tint(Color.White),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column {
                        IconButton(
                            onClick = { navController.navigate(AppDestinations.CAMERA_PREVIEW_ROUTE) },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(FuturisticStartBlue.copy(alpha = 0.15f))
                                .padding(8.dp)
                        ) {
                            Image(
                                Lucide.Scan,
                                contentDescription = "Scan",
                                colorFilter = ColorFilter.tint(Color.White),
                            )
                        }
                    }
                }
            }
        }
    }
}