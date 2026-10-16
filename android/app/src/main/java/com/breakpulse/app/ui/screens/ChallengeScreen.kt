package com.breakpulse.app.ui.screens

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.breakpulse.app.core.AppStorage
import com.breakpulse.app.ui.theme.*
import kotlinx.coroutines.delay

data class DailyChallengeInfo(
    val title: String,
    val subtitle: String,
    val duration: Int,
    val watts: Int,
    val emoji: String
)

val challengeCatalog = listOf(
    DailyChallengeInfo("Single-Leg Balance", "Lift one leg and hold • Activate your core & brain", 45, 50, "🦩"),
    DailyChallengeInfo("Desk Push-Ups", "Lean on desk and perform steady push-ups", 30, 40, "💪"),
    DailyChallengeInfo("Chair Squat Hold", "Hover 2 inches above your chair and hold", 40, 50, "🪑")
)

@Composable
fun ChallengeScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val appStorage = remember { AppStorage.getInstance(context) }

    var selectedChallengeIndex by remember { mutableIntStateOf(0) }
    val currentChallenge = challengeCatalog[selectedChallengeIndex]

    var isRunning by remember { mutableStateOf(false) }
    var secondsElapsed by remember { mutableIntStateOf(0) }
    var showCompletionDialog by remember { mutableStateOf(false) }

    // Reset seconds if challenge changes
    LaunchedEffect(selectedChallengeIndex) {
        isRunning = false
        secondsElapsed = 0
    }

    LaunchedEffect(isRunning, selectedChallengeIndex) {
        if (isRunning) {
            while (secondsElapsed < currentChallenge.duration && isRunning) {
                delay(1000L)
                secondsElapsed++
            }
            if (secondsElapsed >= currentChallenge.duration) {
                isRunning = false
                appStorage.completeChallenge(currentChallenge.watts, currentChallenge.title)
                com.breakpulse.app.core.SoundManager.playVictory()
                com.breakpulse.app.core.HapticManager.vibrate(context, 400)
                showCompletionDialog = true
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WakeoutOrange)
    ) {
        // 1. Top Challenge Header Banner
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp, bottom = 12.dp, start = 20.dp, end = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.18f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "DAILY CHALLENGE • ${currentChallenge.duration} SEC",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = currentChallenge.title,
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = currentChallenge.subtitle,
                color = Color.White.copy(alpha = 0.9f),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Selector for 3 challenges
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(challengeCatalog.indices.toList()) { idx ->
                    val c = challengeCatalog[idx]
                    val isSel = selectedChallengeIndex == idx
                    Surface(
                        color = if (isSel) Color.White else Color.White.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.clickable { selectedChallengeIndex = idx }
                    ) {
                        Text(
                            text = "${c.emoji} ${c.title}",
                            color = if (isSel) WakeoutOrange else Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }

        // 2. Camera Viewport with Rounded Top Corners
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(
                    Color.Black,
                    RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                )
        ) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }

                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // Balance Detection Hint Overlay
            Surface(
                color = Color.Black.copy(alpha = 0.65f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
            ) {
                Text(
                    text = if (isRunning) "✅ Good form! Keep holding!" else "${currentChallenge.emoji} Get ready & tap Start",
                    color = if (isRunning) WakeoutGreen else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }

            // Bottom Challenge HUD Card
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(22.dp),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        val remaining = (currentChallenge.duration - secondsElapsed).coerceAtLeast(0)
                        Text(
                            text = "${remaining}s",
                            color = WakeoutOrange,
                            fontWeight = FontWeight.Black,
                            fontSize = 30.sp,
                            lineHeight = 30.sp
                        )
                        Text(
                            text = "⚡ ${currentChallenge.watts} watts on the line",
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (secondsElapsed > 0 && !isRunning) {
                            OutlinedButton(
                                onClick = { secondsElapsed = 0 },
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text("Reset", color = TextSecondary, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = {
                                if (secondsElapsed >= currentChallenge.duration) {
                                    secondsElapsed = 0
                                }
                                isRunning = !isRunning
                                if (isRunning) {
                                    com.breakpulse.app.core.SoundManager.playBell()
                                }
                                com.breakpulse.app.core.HapticManager.vibrate(context, 40)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRunning) Color(0xFFEF4444) else WakeoutGreen
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.height(46.dp)
                        ) {
                            Text(
                                text = if (isRunning) "Pause" else if (secondsElapsed > 0) "Resume" else "Start",
                                color = if (isRunning) Color.White else Color.Black,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // Completion Celebration Dialog
        if (showCompletionDialog) {
            AlertDialog(
                onDismissRequest = { showCompletionDialog = false },
                title = {
                    Text("🏆 Challenge Conquered!", fontWeight = FontWeight.Black, fontSize = 20.sp)
                },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Incredible focus! You held the entire duration.", fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "⚡ +${currentChallenge.watts} Watts Awarded!",
                            color = WakeoutGreenDark,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showCompletionDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = WakeoutGreen),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Claim Watts", color = Color.Black, fontWeight = FontWeight.Black)
                    }
                },
                containerColor = BgCard
            )
        }
    }
}
