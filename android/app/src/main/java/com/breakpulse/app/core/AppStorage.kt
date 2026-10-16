package com.breakpulse.app.core

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class ActivityLogEntry(
    val title: String,
    val watts: Int,
    val timeAgo: String,
    val typeEmoji: String
)

class AppStorage(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("wakeout_breakpulse_prefs", Context.MODE_PRIVATE)

    var todayWatts by mutableIntStateOf(prefs.getInt("today_watts", 318))
        private set

    var lifetimeWatts by mutableIntStateOf(prefs.getInt("lifetime_watts", 971388))
        private set

    var dayStreak by mutableIntStateOf(prefs.getInt("day_streak", 3))
        private set

    var workoutsCount by mutableIntStateOf(prefs.getInt("workouts_count", 1203))
        private set

    var challengesCount by mutableIntStateOf(prefs.getInt("challenges_count", 29))
        private set

    var wigglesDoneToday by mutableIntStateOf(prefs.getInt("wiggles_done", 2))
        private set

    val recentActivities = mutableStateListOf(
        ActivityLogEntry("Watt Invaders (1 Min)", 56, "15m ago", "🎮"),
        ActivityLogEntry("Single-Leg Balance", 50, "1h ago", "🏆"),
        ActivityLogEntry("Neck Lateral Stretch", 15, "2h ago", "🧘"),
        ActivityLogEntry("Wiggle Break", 10, "3h ago", "🔔"),
        ActivityLogEntry("Morning Desk Stretch", 15, "5h ago", "🧘")
    )

    fun addWatts(amount: Int, activityName: String, typeEmoji: String) {
        todayWatts += amount
        lifetimeWatts += amount
        workoutsCount += 1
        prefs.edit()
            .putInt("today_watts", todayWatts)
            .putInt("lifetime_watts", lifetimeWatts)
            .putInt("workouts_count", workoutsCount)
            .apply()

        recentActivities.add(0, ActivityLogEntry(activityName, amount, "Just now", typeEmoji))
        if (recentActivities.size > 20) {
            recentActivities.removeLast()
        }
    }

    fun completeChallenge(amount: Int, challengeName: String) {
        todayWatts += amount
        lifetimeWatts += amount
        challengesCount += 1
        workoutsCount += 1
        prefs.edit()
            .putInt("today_watts", todayWatts)
            .putInt("lifetime_watts", lifetimeWatts)
            .putInt("challenges_count", challengesCount)
            .putInt("workouts_count", workoutsCount)
            .apply()

        recentActivities.add(0, ActivityLogEntry(challengeName, amount, "Just now", "🏆"))
        if (recentActivities.size > 20) {
            recentActivities.removeLast()
        }
    }

    fun recordWiggle() {
        wigglesDoneToday += 1
        todayWatts += 10
        lifetimeWatts += 10
        prefs.edit()
            .putInt("wiggles_done", wigglesDoneToday)
            .putInt("today_watts", todayWatts)
            .putInt("lifetime_watts", lifetimeWatts)
            .apply()

        recentActivities.add(0, ActivityLogEntry("Wiggle Break", 10, "Just now", "🔔"))
    }

    companion object {
        @Volatile
        private var INSTANCE: AppStorage? = null

        fun getInstance(context: Context): AppStorage {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppStorage(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
