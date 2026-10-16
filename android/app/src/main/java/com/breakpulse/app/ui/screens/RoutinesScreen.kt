package com.breakpulse.app.ui.screens

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

data class DeskRoutineItem(
    val id: String,
    val title: String,
    val category: String,
    val durationSeconds: Int,
    val instructions: String,
    val emoji: String = "🧘"
)

val englishRoutinesList = listOf(
    DeskRoutineItem(
        "1", "Neck Lateral Stretch", "NECK & SHOULDERS", 30,
        "Sit upright in your chair, gently tilt your head toward your right shoulder for 15s, then switch sides.",
        "🙆"
    ),
    DeskRoutineItem(
        "2", "Shoulder Blade Rolls", "NECK & SHOULDERS", 30,
        "Roll both shoulders backward in big circles 10 times, then reverse direction.",
        "🔄"
    ),
    DeskRoutineItem(
        "3", "Chin Tucks Posture Fix", "NECK & SHOULDERS", 30,
        "Gently tuck your chin inward like making a double chin. Hold for 3s, repeat 6 times.",
        "💆"
    ),
    DeskRoutineItem(
        "4", "Prayer Wrist Stretch", "WRISTS & HANDS", 45,
        "Place palms together in front of your chest and slowly lower them down to stretch forearms.",
        "🙏"
    ),
    DeskRoutineItem(
        "5", "Finger Claw & Shake", "WRISTS & HANDS", 30,
        "Make a tight fist for 2s, then spread your fingers wide. Shake your hands to release tension.",
        "✋"
    ),
    DeskRoutineItem(
        "6", "Seated Chair Twist", "SPINE & BACK", 45,
        "Place your right hand on the back of your chair, twist torso gently right. Switch sides after 20s.",
        "🪑"
    ),
    DeskRoutineItem(
        "7", "Seated Cat-Cow Stretch", "SPINE & BACK", 45,
        "Inhale: arch your back and open chest. Exhale: round your spine and tuck chin to chest.",
        "🐈"
    ),
    DeskRoutineItem(
        "8", "Overhead Sky Reach", "SPINE & BACK", 30,
        "Interlace your fingers, push palms toward ceiling, and lengthen your entire spine.",
        "🙌"
    ),
    DeskRoutineItem(
        "9", "20-20-20 Eye Rest", "EYES & BREATHING", 30,
        "Look at an object at least 20 feet away for 20 seconds. Blink slowly 5 times.",
        "👀"
    ),
    DeskRoutineItem(
        "10", "Box Breathing Reset", "EYES & BREATHING", 60,
        "Inhale 4s -> Hold 4s -> Exhale 4s -> Hold 4s. Instantly calms your nervous system.",
        "🫁"
    ),
    DeskRoutineItem(
        "11", "Seated Knee Tucks", "LEGS & CIRCULATION", 45,
        "Hold chair edges and lift alternating knees toward your chest. Perform 15 reps per leg.",
        "🦵"
    ),
    DeskRoutineItem(
        "12", "Seated Calf Pump", "LEGS & CIRCULATION", 45,
        "Keep feet on the floor and rhythmically lift your heels as high as possible to pump venous blood.",
        "🦶"
    )
)

val routineCategories = listOf(
    "ALL",
    "NECK & SHOULDERS",
    "WRISTS & HANDS",
    "SPINE & BACK",
    "EYES & BREATHING",
    "LEGS & CIRCULATION"
)

@Composable
fun RoutinesScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val appStorage = remember { AppStorage.getInstance(context) }

    var selectedCategory by remember { mutableStateOf("ALL") }
    var featuredRoutine by remember { mutableStateOf(englishRoutinesList.first()) }
    var activeWorkoutRoutine by remember { mutableStateOf<DeskRoutineItem?>(null) }
    var workoutSecondsRemaining by remember { mutableIntStateOf(30) }
    var isWorkoutRunning by remember { mutableStateOf(true) }
    var showWorkoutCompletedDialog by remember { mutableStateOf(false) }

    // Filter routines by selected category
    val filteredRoutines = remember(selectedCategory) {
        if (selectedCategory == "ALL") englishRoutinesList
        else englishRoutinesList.filter { it.category == selectedCategory }
    }

    // Workout countdown timer
    LaunchedEffect(activeWorkoutRoutine, isWorkoutRunning) {
        val routine = activeWorkoutRoutine
        if (routine != null && isWorkoutRunning) {
            while (workoutSecondsRemaining > 0 && isWorkoutRunning) {
                delay(1000L)
                workoutSecondsRemaining--
            }
            if (workoutSecondsRemaining <= 0) {
                // Workout completed!
                appStorage.addWatts(15, routine.title, "🧘")
                com.breakpulse.app.core.SoundManager.playVictory()
                com.breakpulse.app.core.HapticManager.vibrate(context, 300)
                activeWorkoutRoutine = null
                showWorkoutCompletedDialog = true
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BgPage)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Header Bar like sample app: 3k+ Desk Stretches / 2 of 28
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 28.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "3k+ Desk Stretches",
                            color = TextMain,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Desk Stretches, Shakes & Jumps",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(16.dp),
                        shadowElevation = 2.dp
                    ) {
                        Text(
                            text = "${filteredRoutines.size} Stretches",
                            color = WakeoutGreenDark,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Category Chips Carousel
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(routineCategories) { cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            color = if (isSelected) WakeoutGreen else Color.White,
                            shape = RoundedCornerShape(18.dp),
                            shadowElevation = if (isSelected) 3.dp else 1.dp,
                            modifier = Modifier.clickable {
                                selectedCategory = cat
                                val firstInCat = if (cat == "ALL") englishRoutinesList.first()
                                else englishRoutinesList.find { it.category == cat } ?: englishRoutinesList.first()
                                featuredRoutine = firstInCat
                            }
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) Color.Black else TextSecondary,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // Big Featured Card (Authentic Wakeout Style)
            item {
                Surface(
                    color = BgCard,
                    shape = RoundedCornerShape(28.dp),
                    shadowElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = WakeoutGreenLight,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = featuredRoutine.category,
                                    color = WakeoutGreenDark,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Text(
                                text = "⏱️ ${featuredRoutine.durationSeconds}s",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = featuredRoutine.title,
                            color = TextMain,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = featuredRoutine.instructions,
                            color = TextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Visual Stage with Exercise Graphic
                        Surface(
                            color = Color(0xFFF0FDF4),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${featuredRoutine.emoji} 🪑",
                                    fontSize = 50.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // "START ROUTINE" Interactive Button
                        Button(
                            onClick = {
                                workoutSecondsRemaining = featuredRoutine.durationSeconds
                                isWorkoutRunning = true
                                activeWorkoutRoutine = featuredRoutine
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WakeoutGreen),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            Text(
                                text = "▶️ START ROUTINE (${featuredRoutine.durationSeconds}s)",
                                color = Color.Black,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }

            // Sub-routines Section Header
            item {
                Text(
                    text = "Quick Desk Stretches",
                    color = TextMain,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
            }

            // Horizontal Carousel of mini routines
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredRoutines) { routine ->
                        Surface(
                            color = BgCard,
                            shape = RoundedCornerShape(20.dp),
                            shadowElevation = 3.dp,
                            modifier = Modifier
                                .width(210.dp)
                                .clickable {
                                    featuredRoutine = routine
                                }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = routine.emoji,
                                        fontSize = 20.sp
                                    )
                                    Text(
                                        text = "⏱️ ${routine.durationSeconds}s",
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = routine.title,
                                    color = TextMain,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    maxLines = 1
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        workoutSecondsRemaining = routine.durationSeconds
                                        isWorkoutRunning = true
                                        activeWorkoutRoutine = routine
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = WakeoutGreen),
                                    shape = RoundedCornerShape(14.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.fillMaxWidth().height(36.dp)
                                ) {
                                    Text(
                                        text = "Play Routine",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Live Workout Player Modal Dialog
        activeWorkoutRoutine?.let { routine ->
            val totalSec = routine.durationSeconds.toFloat()
            val prog = (1f - (workoutSecondsRemaining / totalSec)).coerceIn(0f, 1f)

            AlertDialog(
                onDismissRequest = { activeWorkoutRoutine = null },
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            color = WakeoutGreenLight,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = routine.category,
                                color = WakeoutGreenDark,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = routine.title,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = TextMain
                        )
                    }
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Countdown Ring
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(150.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                                val strokeWidth = 12.dp.toPx()
                                drawCircle(
                                    color = Color(0xFFF1F5F9),
                                    style = Stroke(width = strokeWidth)
                                )
                                drawArc(
                                    color = WakeoutGreen,
                                    startAngle = -90f,
                                    sweepAngle = prog * 360f,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$workoutSecondsRemaining",
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.Black,
                                    color = WakeoutGreen
                                )
                                Text(
                                    text = "seconds left",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = routine.instructions,
                            color = TextMain,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Controls: Pause / Resume / Finish
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { isWorkoutRunning = !isWorkoutRunning },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isWorkoutRunning) Color(0xFFEF4444) else WakeoutGreen
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = if (isWorkoutRunning) "Pause" else "Resume",
                                    color = if (isWorkoutRunning) Color.White else Color.Black,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            Button(
                                onClick = {
                                    appStorage.addWatts(15, routine.title, "🧘")
                                    activeWorkoutRoutine = null
                                    showWorkoutCompletedDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = WakeoutOrange),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Complete",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { activeWorkoutRoutine = null }) {
                        Text("Cancel", color = TextSecondary, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = BgCard
            )
        }

        // Completion Celebration Dialog
        if (showWorkoutCompletedDialog) {
            AlertDialog(
                onDismissRequest = { showWorkoutCompletedDialog = false },
                title = {
                    Text("🎉 Stretch Completed!", fontWeight = FontWeight.Black, fontSize = 20.sp)
                },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("You earned +15 Watts and refreshed your body!", fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("⚡ +15 Watts", color = WakeoutGreenDark, fontWeight = FontWeight.Black, fontSize = 24.sp)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showWorkoutCompletedDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = WakeoutGreen),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Awesome!", color = Color.Black, fontWeight = FontWeight.Black)
                    }
                },
                containerColor = BgCard
            )
        }
    }
}
