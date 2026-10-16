package com.breakpulse.app.ui.screens

import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.breakpulse.app.core.AppStorage
import com.breakpulse.app.core.HapticManager
import com.breakpulse.app.core.SoundManager
import com.breakpulse.app.core.gesture.Gesture
import com.breakpulse.app.core.gesture.GestureEngine
import com.breakpulse.app.core.ml.MotionAnalyzer
import com.breakpulse.app.ui.theme.*
import kotlinx.coroutines.delay
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import kotlin.random.Random

data class TargetItem(
    var x: Float,
    var y: Float,
    val type: Int, // 0 = UFO, 1 = Alien, 2 = Watt
    val speed: Float = 0.007f + Random.nextFloat() * 0.006f
)

data class LaserItem(
    var x: Float,
    var y: Float,
    val speed: Float = 0.04f
)

@Composable
fun GameScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val appStorage = remember { AppStorage.getInstance(context) }

    var isGameRunning by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableIntStateOf(60) }
    var score by remember { mutableIntStateOf(56) }
    var combo by remember { mutableIntStateOf(28) }
    var shipX by remember { mutableFloatStateOf(0.5f) }
    var currentWave by remember { mutableIntStateOf(6) }
    var showGameOverDialog by remember { mutableStateOf(false) }

    val targets = remember { mutableStateListOf<TargetItem>() }
    val lasers = remember { mutableStateListOf<LaserItem>() }
    val pendingLasers = remember { ConcurrentLinkedQueue<LaserItem>() }

    val gestureEngine = remember { GestureEngine() }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    fun fireLaser() {
        pendingLasers.add(LaserItem(shipX - 0.04f, 0.82f))
        pendingLasers.add(LaserItem(shipX + 0.04f, 0.82f))
        SoundManager.playLaser()
        HapticManager.vibrate(context, 25)
    }

    // Game loop
    LaunchedEffect(isGameRunning) {
        if (isGameRunning) {
            timeLeft = 60
            score = 56
            combo = 28
            targets.clear()
            lasers.clear()
            pendingLasers.clear()

            var frameCount = 0
            while (timeLeft > 0 && isGameRunning) {
                delay(33L) // ~30 FPS loop
                frameCount++

                if (frameCount % 30 == 0) {
                    timeLeft--
                    currentWave = ((60 - timeLeft) / 10) + 1
                }

                // Drain pending lasers safely from background or UI threads
                while (true) {
                    val p = pendingLasers.poll() ?: break
                    lasers.add(p)
                }

                // Spawn targets periodically
                if (frameCount % 24 == 0 && targets.size < 7) {
                    targets.add(
                        TargetItem(
                            x = 0.15f + Random.nextFloat() * 0.7f,
                            y = -0.05f,
                            type = Random.nextInt(3)
                        )
                    )
                }

                // Move lasers safely
                lasers.removeAll { laser ->
                    laser.y -= laser.speed
                    laser.y < -0.1f
                }

                // Move targets & check collisions safely without ConcurrentModificationException
                val currentLasers = lasers.toList()
                val hitLasers = mutableSetOf<LaserItem>()

                targets.removeAll { target ->
                    target.y += target.speed
                    var hit = false
                    for (laser in currentLasers) {
                        if (hitLasers.contains(laser)) continue
                        val dx = target.x - laser.x
                        val dy = target.y - laser.y
                        if (dx * dx + dy * dy < 0.009f) {
                            hit = true
                            hitLasers.add(laser)
                            break
                        }
                    }

                    if (hit) {
                        score += 10
                        combo++
                        SoundManager.playHit()
                        HapticManager.vibrate(context, 35)
                        true
                    } else {
                        target.y > 1.1f
                    }
                }

                if (hitLasers.isNotEmpty()) {
                    lasers.removeAll(hitLasers)
                }
            }

            if (isGameRunning) {
                isGameRunning = false
                appStorage.addWatts(score, "Watt Invaders (1 Min)", "🎮")
                SoundManager.playVictory()
                HapticManager.vibrate(context, 300)
                showGameOverDialog = true
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(isGameRunning) {
                if (isGameRunning) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        shipX = (shipX + dragAmount.x / size.width).coerceIn(0.1f, 0.9f)
                    }
                }
            }
    ) {
        // 1. Fullscreen Camera Preview
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

                        val imageAnalyzer = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also {
                                it.setAnalyzer(
                                    cameraExecutor,
                                    MotionAnalyzer(isFrontCamera = true) { pose, ts ->
                                        val nowSec = ts / 1_000_000_000.0
                                        val gesture = gestureEngine.evaluate(pose, nowSec)
                                        val mid = GestureEngine.shoulderMid(pose)
                                        if (mid != null) {
                                            shipX = mid.coerceIn(0.1f, 0.9f)
                                        }

                                        if (gesture == Gesture.PUNCH_RIGHT || gesture == Gesture.PUNCH_LEFT) {
                                            if (isGameRunning) {
                                                pendingLasers.add(LaserItem(shipX - 0.04f, 0.82f))
                                                pendingLasers.add(LaserItem(shipX + 0.04f, 0.82f))
                                                SoundManager.playLaser()
                                                HapticManager.vibrate(context, 25)
                                            }
                                        }
                                    }
                                )
                            }

                        val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalyzer
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // 2. Interactive Game Canvas (UFOs, Aliens, Lasers, Spaceship)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Lasers
            for (laser in lasers) {
                drawCircle(
                    color = WakeoutGreen,
                    radius = 10.dp.toPx(),
                    center = Offset(laser.x * w, laser.y * h)
                )
            }

            // Targets (UFO, Alien, Watt)
            for (t in targets) {
                val tx = t.x * w
                val ty = t.y * h
                when (t.type) {
                    0 -> { // Flying Saucer UFO (Purple & Cyan)
                        drawOval(
                            color = WakeoutPurple,
                            topLeft = Offset(tx - 32.dp.toPx(), ty - 12.dp.toPx()),
                            size = androidx.compose.ui.geometry.Size(64.dp.toPx(), 24.dp.toPx())
                        )
                        drawCircle(
                            color = Color(0xFF67E8F9),
                            radius = 14.dp.toPx(),
                            center = Offset(tx, ty - 8.dp.toPx())
                        )
                    }
                    1 -> { // Alien (Orange / Red)
                        drawCircle(
                            color = WakeoutOrange,
                            radius = 22.dp.toPx(),
                            center = Offset(tx, ty)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 6.dp.toPx(),
                            center = Offset(tx - 8.dp.toPx(), ty - 4.dp.toPx())
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 6.dp.toPx(),
                            center = Offset(tx + 8.dp.toPx(), ty - 4.dp.toPx())
                        )
                    }
                    else -> { // Glowing Watt
                        drawCircle(
                            color = WakeoutGreen,
                            radius = 20.dp.toPx(),
                            center = Offset(tx, ty)
                        )
                    }
                }
            }

            // Player Spaceship / Avatar at bottom
            val px = shipX * w
            val py = h * 0.82f

            // Jet flame
            drawCircle(
                color = WakeoutOrange,
                radius = 16.dp.toPx(),
                center = Offset(px, py + 22.dp.toPx())
            )
            // Ship Body
            drawCircle(
                color = Color(0xFF1E293B),
                radius = 28.dp.toPx(),
                center = Offset(px, py)
            )
            drawCircle(
                color = WakeoutGreen,
                radius = 16.dp.toPx(),
                center = Offset(px, py)
            )
            drawCircle(
                color = WakeoutYellow,
                radius = 6.dp.toPx(),
                center = Offset(px, py)
            )
        }

        // 3. Top HUD Overlay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 36.dp, start = 18.dp, end = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left score badge: 56 x28
            Surface(
                color = Color.Black.copy(alpha = 0.65f),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "$score",
                        color = WakeoutYellow,
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp
                    )
                    Text(
                        text = "x$combo",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            // Right Timer badge
            Surface(
                color = Color.Black.copy(alpha = 0.65f),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(
                    text = "⏱️ ${timeLeft}s",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }

        // 4. Center-Bottom "WAVE 6" Indicator
        Surface(
            color = WakeoutPurple.copy(alpha = 0.85f),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 146.dp)
        ) {
            Text(
                text = "WAVE $currentWave",
                color = Color.White,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                fontSize = 15.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )
        }

        // 5. On-Screen Quick Steering & Punch Controls (Left/Right + Punch)
        if (isGameRunning) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 82.dp, start = 20.dp, end = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Steer Left / Right buttons
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f))
                            .clickable {
                                shipX = (shipX - 0.08f).coerceAtLeast(0.1f)
                                HapticManager.vibrate(context, 15)
                            }
                    ) {
                        Text(text = "◀️", fontSize = 20.sp)
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f))
                            .clickable {
                                shipX = (shipX + 0.08f).coerceAtMost(0.9f)
                                HapticManager.vibrate(context, 15)
                            }
                    ) {
                        Text(text = "▶️", fontSize = 20.sp)
                    }
                }

                // Fire / Punch Button
                Button(
                    onClick = { fireLaser() },
                    colors = ButtonDefaults.buttonColors(containerColor = WakeoutOrange),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.height(52.dp)
                ) {
                    Text(
                        text = "💥 PUNCH!",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // 6. Bottom Start / Stop Action Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp, start = 20.dp, end = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = {
                    isGameRunning = !isGameRunning
                    if (isGameRunning) {
                        SoundManager.playBell()
                    }
                    HapticManager.vibrate(context, 40)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isGameRunning) Color(0xFFEF4444) else WakeoutGreen
                ),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text(
                    text = if (isGameRunning) "⏹️ STOP" else "▶️ START (1 MIN)",
                    color = if (isGameRunning) Color.White else Color.Black,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
            }
        }

        // 7. Victory Game Over Dialog
        if (showGameOverDialog) {
            AlertDialog(
                onDismissRequest = { showGameOverDialog = false },
                title = {
                    Text("🏆 1-Min Workout Finished!", fontWeight = FontWeight.Black, fontSize = 20.sp)
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Awesome active break! You energized your body.", fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = WakeoutGreenLight,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "⚡ +$score Watts",
                                    color = WakeoutGreenDark,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 28.sp
                                )
                                Text(
                                    text = "Max Combo: x$combo",
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showGameOverDialog = false
                            isGameRunning = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WakeoutGreen),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Play Again", color = Color.Black, fontWeight = FontWeight.Black)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showGameOverDialog = false }) {
                        Text("Done", color = TextSecondary, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = BgCard
            )
        }
    }
}
