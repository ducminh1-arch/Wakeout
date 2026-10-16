package com.breakpulse.app.ui.screens

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.breakpulse.app.core.AppStorage
import com.breakpulse.app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun ReminderScreen(
    onTriggerNow: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appStorage = remember { AppStorage.getInstance(context) }

    var selectedMinutes by remember { mutableIntStateOf(30) }
    var secondsRemaining by remember { mutableIntStateOf(29 * 60 + 53) }
    var isTimerRunning by remember { mutableStateOf(true) }
    var showCelebration by remember { mutableStateOf(false) }

    // Countdown loop
    LaunchedEffect(isTimerRunning, selectedMinutes) {
        while (isTimerRunning) {
            delay(1000L)
            if (secondsRemaining > 0) {
                secondsRemaining--
            } else {
                secondsRemaining = selectedMinutes * 60
                com.breakpulse.app.core.SoundManager.playBell()
                com.breakpulse.app.core.HapticManager.vibrate(context, 400)
            }
        }
    }

    // Auto dismiss celebration banner after 3 seconds
    LaunchedEffect(showCelebration) {
        if (showCelebration) {
            delay(3000L)
            showCelebration = false
        }
    }

    val minutes = secondsRemaining / 60
    val seconds = secondsRemaining % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)
    val totalSeconds = (selectedMinutes * 60).toFloat()
    val progress = (1f - (secondsRemaining / totalSeconds)).coerceIn(0f, 1f)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgPage)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Toast Notification Card (Exact match to sample app screenshot)
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(22.dp),
            shadowElevation = 3.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .clickable {
                    onTriggerNow()
                }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    color = WakeoutOrangeLight,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "🔔", fontSize = 24.sp)
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Time to wiggle!",
                        color = TextMain,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Tap to launch 1-min game break",
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Surface(
                    color = WakeoutOrange.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "PLAY",
                        color = WakeoutOrange,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        // Celebration Toast Banner
        AnimatedVisibility(
            visible = showCelebration,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Surface(
                color = WakeoutGreen,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "🎉 +10 Watts! Great wiggle session!",
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // 2. Main Wiggle Timer Card (Giant Orange Ring)
        Surface(
            color = BgCard,
            shape = RoundedCornerShape(32.dp),
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Wiggle Reminder",
                    color = TextMain,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Every $selectedMinutes min • 1st round",
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Giant Circular Countdown
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                        .clickable {
                            isTimerRunning = !isTimerRunning
                            Toast.makeText(
                                context,
                                if (isTimerRunning) "Timer Resumed" else "Timer Paused",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                        val strokeWidth = 14.dp.toPx()

                        // Background track
                        drawCircle(
                            color = Color(0xFFE2E8F0),
                            style = Stroke(width = strokeWidth)
                        )

                        // Orange progress arc
                        drawArc(
                            color = WakeoutOrange,
                            startAngle = -90f,
                            sweepAngle = (progress * 360f).coerceAtLeast(4f),
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = timeFormatted,
                            color = TextMain,
                            fontWeight = FontWeight.Black,
                            fontSize = 42.sp,
                            letterSpacing = (-1).sp,
                            lineHeight = 42.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isTimerRunning) "until your next wiggle" else "PAUSED (tap to resume)",
                            color = if (isTimerRunning) TextSecondary else WakeoutOrange,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Interval Selection Pills - Fixed single-line pills without text wrapping!
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(20, 30, 45, 60).forEach { mins ->
                        val isSelected = selectedMinutes == mins
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) WakeoutOrange else BgCardSubtle)
                                .clickable {
                                    selectedMinutes = mins
                                    secondsRemaining = mins * 60
                                    isTimerRunning = true
                                    com.breakpulse.app.core.HapticManager.vibrate(context, 30)
                                }
                        ) {
                            Text(
                                text = "${mins}m",
                                color = if (isSelected) Color.White else TextSecondary,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Completed today badge (Live synced with AppStorage)
                Surface(
                    color = WakeoutGreenLight,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "⚡ ${appStorage.wigglesDoneToday} wiggles done today",
                            color = WakeoutGreenDark,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // 3. Bottom Action Button ("WIGGLE NOW!") - Placed cleanly above navigation bar
        Button(
            onClick = {
                // Audio feedback
                com.breakpulse.app.core.SoundManager.playBell()

                // Safe Haptic feedback
                com.breakpulse.app.core.HapticManager.vibrate(context, 80)

                // Award watts and record wiggle
                appStorage.recordWiggle()
                showCelebration = true
                secondsRemaining = selectedMinutes * 60

                // Trigger navigation to 1-Min game after a brief celebration
                onTriggerNow()
            },
            colors = ButtonDefaults.buttonColors(containerColor = WakeoutOrange),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "⚡ WIGGLE NOW!",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
