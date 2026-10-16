package com.breakpulse.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

@Composable
fun WattsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val appStorage = remember { AppStorage.getInstance(context) }

    var showActivityLogDialog by remember { mutableStateOf(false) }
    var selectedDayIndex by remember { mutableIntStateOf(6) } // Default to Sunday (Today, 12th)

    val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val dayNumbers = listOf(6, 7, 8, 9, 10, 11, 12)
    val dayWatts = listOf(280, 420, 310, 520, 390, 0, appStorage.todayWatts)
    val dayActive = listOf(true, true, true, true, true, false, true)

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
            // Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 28.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Watts",
                        color = TextMain,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black
                    )
                    Surface(
                        color = WakeoutGreen.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.clickable { showActivityLogDialog = true }
                    ) {
                        Text(
                            text = "Activity Log 📋",
                            color = WakeoutGreenDark,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Big Circular Progress Ring Card
            item {
                Surface(
                    color = BgCard,
                    shape = RoundedCornerShape(28.dp),
                    shadowElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(170.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokeWidth = 14.dp.toPx()
                                drawCircle(
                                    color = Color(0xFFF1F5F9),
                                    style = Stroke(width = strokeWidth)
                                )
                                val progress = (appStorage.todayWatts.toFloat() / 1100f).coerceIn(0f, 1f)
                                drawArc(
                                    color = WakeoutGreen,
                                    startAngle = -90f,
                                    sweepAngle = progress * 360f,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "⚡ ${appStorage.todayWatts}",
                                    color = WakeoutGreen,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 36.sp,
                                    lineHeight = 36.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "of 1,100 today",
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // 3 Stats Boxes Grid (Streak, Workouts, Challenges)
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        color = BgCard,
                        shape = RoundedCornerShape(20.dp),
                        shadowElevation = 3.dp,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${appStorage.dayStreak}",
                                color = WakeoutOrange,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Day Streak",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        color = BgCard,
                        shape = RoundedCornerShape(20.dp),
                        shadowElevation = 3.dp,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = String.format("%,d", appStorage.workoutsCount),
                                color = WakeoutGreen,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Workouts",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        color = BgCard,
                        shape = RoundedCornerShape(20.dp),
                        shadowElevation = 3.dp,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${appStorage.challengesCount}",
                                color = WakeoutBlue,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Challenges",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Lifetime Watts Banner
            item {
                Surface(
                    color = Color(0xFFF0FDF4),
                    shape = RoundedCornerShape(20.dp),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "LIFETIME WATTS",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "⚡ " + String.format("%,d", appStorage.lifetimeWatts),
                                color = WakeoutGreenDark,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Text(text = "🏆", fontSize = 36.sp)
                    }
                }
            }

            // Calendar Heatmap Card - Interactive Day Selector
            item {
                Surface(
                    color = BgCard,
                    shape = RoundedCornerShape(24.dp),
                    shadowElevation = 3.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Active Days",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = TextMain
                            )
                            Text(
                                text = "October 2026",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Days of week row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            dayNames.forEach { d ->
                                Box(
                                    modifier = Modifier.width(38.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = d,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Date bubbles row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            dayNumbers.forEachIndexed { i, num ->
                                val active = dayActive[i]
                                val isSelected = selectedDayIndex == i

                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            when {
                                                isSelected -> WakeoutGreenDark
                                                active -> WakeoutGreen
                                                else -> Color(0xFFF1F5F9)
                                            }
                                        )
                                        .clickable {
                                            selectedDayIndex = i
                                        }
                                ) {
                                    Text(
                                        text = "$num",
                                        color = if (active || isSelected) Color.White else TextSecondary,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Selected Day Info Banner
                        Surface(
                            color = BgPage,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${dayNames[selectedDayIndex]}, Oct ${dayNumbers[selectedDayIndex]}:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary
                                )
                                Text(
                                    text = if (dayWatts[selectedDayIndex] > 0)
                                        "⚡ ${dayWatts[selectedDayIndex]} Watts earned"
                                    else
                                        "Rest day (0 Watts)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (dayWatts[selectedDayIndex] > 0) WakeoutGreenDark else TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Activity Log Dialog
        if (showActivityLogDialog) {
            AlertDialog(
                onDismissRequest = { showActivityLogDialog = false },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Activity Log", fontWeight = FontWeight.Black, fontSize = 20.sp)
                        Text("📋", fontSize = 22.sp)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 340.dp)
                    ) {
                        Text(
                            text = "Recent micro-workouts & challenges:",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(appStorage.recentActivities) { item ->
                                Surface(
                                    color = BgPage,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(text = item.typeEmoji, fontSize = 18.sp)
                                            Column {
                                                Text(
                                                    text = item.title,
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 13.sp,
                                                    color = TextMain
                                                )
                                                Text(
                                                    text = item.timeAgo,
                                                    fontWeight = FontWeight.Normal,
                                                    fontSize = 11.sp,
                                                    color = TextSecondary
                                                )
                                            }
                                        }

                                        Surface(
                                            color = WakeoutGreenLight,
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text(
                                                text = "+${item.watts}⚡",
                                                color = WakeoutGreenDark,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 12.sp,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showActivityLogDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = WakeoutGreen),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Close", color = Color.Black, fontWeight = FontWeight.Black)
                    }
                },
                containerColor = BgCard,
                titleContentColor = TextMain
            )
        }
    }
}
