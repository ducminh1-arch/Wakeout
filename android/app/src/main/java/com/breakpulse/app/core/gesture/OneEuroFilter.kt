package com.breakpulse.app.core.gesture

import kotlin.math.PI
import kotlin.math.abs

/**
 * Bộ lọc One-Euro Filter (Kotlin) theo spec chung.
 * Loại bỏ hiện tượng rung giật jitter khi giữ yên, phản hồi nhanh khi di chuyển.
 */
class OneEuroFilter(
    private val minCutoff: Double = 1.2,
    private val beta: Double = 0.02,
    private val dCutoff: Double = 1.0
) {
    private var xPrev: Double? = null
    private var dxPrev = 0.0
    private var tPrev = 0.0

    private fun alpha(cutoff: Double, dt: Double): Double {
        val tau = 1.0 / (2.0 * PI * cutoff)
        return 1.0 / (1.0 + tau / dt)
    }

    fun filter(x: Double, tSeconds: Double): Double {
        val xp = xPrev
        if (xp == null || tSeconds <= tPrev) {
            xPrev = x
            tPrev = tSeconds
            dxPrev = 0.0
            return x
        }

        val dt = tSeconds - tPrev
        val dx = (x - xp) / dt
        val dxHat = dxPrev + alpha(dCutoff, dt) * (dx - dxPrev)
        val cutoff = minCutoff + beta * abs(dxHat)
        val xHat = xp + alpha(cutoff, dt) * (x - xp)

        xPrev = xHat
        dxPrev = dxHat
        tPrev = tSeconds
        return xHat
    }

    fun reset() {
        xPrev = null
        dxPrev = 0.0
        tPrev = 0.0
    }
}
