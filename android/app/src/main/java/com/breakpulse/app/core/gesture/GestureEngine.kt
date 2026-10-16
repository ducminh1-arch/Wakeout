package com.breakpulse.app.core.gesture

import kotlin.math.hypot

enum class Gesture {
    SWAY_LEFT,
    SWAY_RIGHT,
    PUNCH_LEFT,
    PUNCH_RIGHT,
    NONE
}

data class Baseline(val midX: Float, val shoulderWidth: Float)

/**
 * Bộ phân loại cử chỉ chuyển động (Kotlin).
 * Chuẩn hóa khoảng cách và vận tốc theo độ rộng vai (shoulder width).
 */
class GestureEngine(
    var swayThreshold: Float = 0.35f,
    var swayReleaseThreshold: Float = 0.20f,
    var punchVelocityThreshold: Float = 3.0f,
    var punchCooldownSeconds: Double = 0.30
) {
    var baseline: Baseline? = null
        private set

    private var currentSwayState: Gesture = Gesture.NONE
    private val prevReach = HashMap<Joint, Pair<Float, Double>>()
    private val lastPunchTime = HashMap<Joint, Double>()

    constructor(initialBaseline: Baseline) : this() {
        this.baseline = initialBaseline
    }

    fun setBaseline(newBaseline: Baseline) {
        this.baseline = newBaseline
    }

    /**
     * Hiệu chuẩn baseline từ danh sách mẫu trong 2-3 giây đầu tiên.
     */
    fun calibrate(samples: List<Pose>) {
        val mids = samples.mapNotNull { shoulderMid(it) }.sorted()
        val widths = samples.mapNotNull { shoulderWidth(it) }.sorted()
        if (mids.isEmpty() || widths.isEmpty()) return
        baseline = Baseline(mids[mids.size / 2], widths[widths.size / 2])
    }

    /**
     * Đánh giá cử chỉ từ dữ liệu Pose tại thời điểm tSeconds (giây).
     */
    fun evaluate(pose: Pose, tSeconds: Double): Gesture {
        val b = baseline ?: return Gesture.NONE
        if (b.shoulderWidth < 0.02f) return Gesture.NONE

        // 1. Kiểm tra cử chỉ Punch (Đấm / vươn tay)
        val punchPairs = listOf(
            Triple(Joint.RIGHT_WRIST, Joint.RIGHT_SHOULDER, Gesture.PUNCH_RIGHT),
            Triple(Joint.LEFT_WRIST, Joint.LEFT_SHOULDER, Gesture.PUNCH_LEFT)
        )

        for ((wristJoint, shoulderJoint, gesture) in punchPairs) {
            checkPunch(pose, wristJoint, shoulderJoint, gesture, tSeconds, b.shoulderWidth)?.let {
                return it
            }
        }

        // 2. Kiểm tra cử chỉ Sway (Nghiêng người với Hysteresis)
        shoulderMid(pose)?.let { mid ->
            val normalizedDelta = (mid - b.midX) / b.shoulderWidth

            when (currentSwayState) {
                Gesture.SWAY_RIGHT -> {
                    if (normalizedDelta > swayReleaseThreshold) {
                        return Gesture.SWAY_RIGHT
                    } else {
                        currentSwayState = Gesture.NONE
                    }
                }
                Gesture.SWAY_LEFT -> {
                    if (normalizedDelta < -swayReleaseThreshold) {
                        return Gesture.SWAY_LEFT
                    } else {
                        currentSwayState = Gesture.NONE
                    }
                }
                else -> {
                    if (normalizedDelta > swayThreshold) {
                        currentSwayState = Gesture.SWAY_RIGHT
                        return Gesture.SWAY_RIGHT
                    } else if (normalizedDelta < -swayThreshold) {
                        currentSwayState = Gesture.SWAY_LEFT
                        return Gesture.SWAY_LEFT
                    }
                }
            }
        }

        return Gesture.NONE
    }

    private fun checkPunch(
        pose: Pose,
        wrist: Joint,
        shoulder: Joint,
        gesture: Gesture,
        tSeconds: Double,
        shoulderWidth: Float
    ): Gesture? {
        val w = pose[wrist] ?: return null
        val s = pose[shoulder] ?: return null
        if (w.confidence < 0.4f || s.confidence < 0.4f) return null

        val currentDistance = hypot(w.x - s.x, w.y - s.y) / shoulderWidth
        val prev = prevReach[wrist]
        prevReach[wrist] = Pair(currentDistance, tSeconds)

        // Cooldown check
        val lastPunch = lastPunchTime[wrist]
        if (lastPunch != null && (tSeconds - lastPunch) < punchCooldownSeconds) {
            return null
        }

        // Velocity & elevation check
        if (prev != null && tSeconds > prev.second) {
            val dt = tSeconds - prev.second
            val velocity = (currentDistance - prev.first) / dt

            if (velocity > punchVelocityThreshold && w.y < s.y) {
                lastPunchTime[wrist] = tSeconds
                return gesture
            }
        }

        return null
    }

    fun reset() {
        currentSwayState = Gesture.NONE
        prevReach.clear()
        lastPunchTime.clear()
    }

    companion object {
        fun shoulderMid(p: Pose): Float? {
            val l = p[Joint.LEFT_SHOULDER] ?: return null
            val r = p[Joint.RIGHT_SHOULDER] ?: return null
            if (l.confidence < 0.4f || r.confidence < 0.4f) return null
            return (l.x + r.x) / 2f
        }

        fun shoulderWidth(p: Pose): Float? {
            val l = p[Joint.LEFT_SHOULDER] ?: return null
            val r = p[Joint.RIGHT_SHOULDER] ?: return null
            if (l.confidence < 0.4f || r.confidence < 0.4f) return null
            return hypot(r.x - l.x, r.y - l.y)
        }
    }
}
