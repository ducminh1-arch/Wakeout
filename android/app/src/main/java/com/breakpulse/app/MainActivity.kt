package com.breakpulse.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.breakpulse.app.ui.screens.*
import com.breakpulse.app.ui.theme.BreakPulseTheme
import com.breakpulse.app.ui.theme.TextSecondary
import com.breakpulse.app.ui.theme.WakeoutGreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BreakPulseTheme {
                MainAppScaffold()
            }
        }
    }
}

@Composable
fun MainAppScaffold() {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasCameraPermission = isGranted
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = NavigationBarDefaults.Elevation
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Text("🎮", fontSize = 20.sp) },
                    label = { Text("1-Min", fontSize = 11.sp, fontWeight = FontWeight.Black) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = WakeoutGreen,
                        selectedTextColor = WakeoutGreen,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = Color(0xFFE8F8D8)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Text("🏆", fontSize = 20.sp) },
                    label = { Text("Challenge", fontSize = 11.sp, fontWeight = FontWeight.Black) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = WakeoutGreen,
                        selectedTextColor = WakeoutGreen,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = Color(0xFFE8F8D8)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Text("🧘", fontSize = 20.sp) },
                    label = { Text("Stretches", fontSize = 11.sp, fontWeight = FontWeight.Black) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = WakeoutGreen,
                        selectedTextColor = WakeoutGreen,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = Color(0xFFE8F8D8)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Text("⏰", fontSize = 20.sp) },
                    label = { Text("Reminder", fontSize = 11.sp, fontWeight = FontWeight.Black) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = WakeoutGreen,
                        selectedTextColor = WakeoutGreen,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = Color(0xFFE8F8D8)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Text("⚡", fontSize = 20.sp) },
                    label = { Text("Watts", fontSize = 11.sp, fontWeight = FontWeight.Black) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = WakeoutGreen,
                        selectedTextColor = WakeoutGreen,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = Color(0xFFE8F8D8)
                    )
                )
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            0 -> GameScreen(modifier = Modifier.padding(innerPadding))
            1 -> ChallengeScreen(modifier = Modifier.padding(innerPadding))
            2 -> RoutinesScreen(modifier = Modifier.padding(innerPadding))
            3 -> ReminderScreen(onTriggerNow = { selectedTab = 0 }, modifier = Modifier.padding(innerPadding))
            4 -> WattsScreen(modifier = Modifier.padding(innerPadding))
        }
    }
}
