package com.breakpulse.app.core.gesture

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Unit Test runner cho GestureEngine (Kotlin/Android),
 * đối chiếu trực tiếp với các kịch bản trong shared-spec/gesture_vectors.json.
 */
class GestureEngineTest {

    private lateinit var engine: GestureEngine

    @Before
    fun setUp() {
        // Khởi tạo engine với baseline chuẩn từ shared-spec
        engine = GestureEngine(Baseline(midX = 0.50f, shoulderWidth = 0.30f))
    }

    @Test
    fun testSwayRightClear() {
        val frame0 = mapOf(
            Joint.LEFT_SHOULDER to TrackedPoint(0.35f, 0.50f, 0.95f),
            Joint.RIGHT_SHOULDER to TrackedPoint(0.65f, 0.50f, 0.95f)
        )
        engine.evaluate(frame0, 0.0)

        val frame1 = mapOf(
            Joint.LEFT_SHOULDER to TrackedPoint(0.47f, 0.50f, 0.95f),
            Joint.RIGHT_SHOULDER to TrackedPoint(0.77f, 0.50f, 0.95f)
        )
        val result = engine.evaluate(frame1, 0.033)
        assertEquals(Gesture.SWAY_RIGHT, result)
    }

    @Test
    fun testSwayLeftClear() {
        val frame0 = mapOf(
            Joint.LEFT_SHOULDER to TrackedPoint(0.35f, 0.50f, 0.95f),
            Joint.RIGHT_SHOULDER to TrackedPoint(0.65f, 0.50f, 0.95f)
        )
        engine.evaluate(frame0, 0.0)

        val frame1 = mapOf(
            Joint.LEFT_SHOULDER to TrackedPoint(0.23f, 0.50f, 0.95f),
            Joint.RIGHT_SHOULDER to TrackedPoint(0.53f, 0.50f, 0.95f)
        )
        val result = engine.evaluate(frame1, 0.033)
        assertEquals(Gesture.SWAY_LEFT, result)
    }

    @Test
    fun testSwayBelowThresholdReturnsNone() {
        val frame0 = mapOf(
            Joint.LEFT_SHOULDER to TrackedPoint(0.35f, 0.50f, 0.95f),
            Joint.RIGHT_SHOULDER to TrackedPoint(0.65f, 0.50f, 0.95f)
        )
        engine.evaluate(frame0, 0.0)

        val frame1 = mapOf(
            Joint.LEFT_SHOULDER to TrackedPoint(0.37f, 0.50f, 0.95f),
            Joint.RIGHT_SHOULDER to TrackedPoint(0.67f, 0.50f, 0.95f)
        )
        val result = engine.evaluate(frame1, 0.033)
        assertEquals(Gesture.NONE, result)
    }

    @Test
    fun testPunchUpRight() {
        val frame0 = mapOf(
            Joint.LEFT_SHOULDER to TrackedPoint(0.35f, 0.50f, 0.95f),
            Joint.RIGHT_SHOULDER to TrackedPoint(0.65f, 0.50f, 0.95f),
            Joint.RIGHT_WRIST to TrackedPoint(0.65f, 0.62f, 0.90f)
        )
        engine.evaluate(frame0, 0.0)

        val frame1 = mapOf(
            Joint.LEFT_SHOULDER to TrackedPoint(0.35f, 0.50f, 0.95f),
            Joint.RIGHT_SHOULDER to TrackedPoint(0.65f, 0.50f, 0.95f),
            Joint.RIGHT_WRIST to TrackedPoint(0.67f, 0.28f, 0.90f)
        )
        val result = engine.evaluate(frame1, 0.10)
        assertEquals(Gesture.PUNCH_RIGHT, result)
    }

    @Test
    fun testPunchUpLeft() {
        val frame0 = mapOf(
            Joint.LEFT_SHOULDER to TrackedPoint(0.35f, 0.50f, 0.95f),
            Joint.RIGHT_SHOULDER to TrackedPoint(0.65f, 0.50f, 0.95f),
            Joint.LEFT_WRIST to TrackedPoint(0.35f, 0.62f, 0.90f)
        )
        engine.evaluate(frame0, 0.0)

        val frame1 = mapOf(
            Joint.LEFT_SHOULDER to TrackedPoint(0.35f, 0.50f, 0.95f),
            Joint.RIGHT_SHOULDER to TrackedPoint(0.65f, 0.50f, 0.95f),
            Joint.LEFT_WRIST to TrackedPoint(0.33f, 0.28f, 0.90f)
        )
        val result = engine.evaluate(frame1, 0.10)
        assertEquals(Gesture.PUNCH_LEFT, result)
    }

    @Test
    fun testPunchDownwardIgnored() {
        val frame0 = mapOf(
            Joint.LEFT_SHOULDER to TrackedPoint(0.35f, 0.50f, 0.95f),
            Joint.RIGHT_SHOULDER to TrackedPoint(0.65f, 0.50f, 0.95f),
            Joint.RIGHT_WRIST to TrackedPoint(0.65f, 0.60f, 0.90f)
        )
        engine.evaluate(frame0, 0.0)

        // Cổ tay di chuyển nhanh nhưng đi xuống dưới (y = 0.90f > shoulder.y = 0.50f)
        val frame1 = mapOf(
            Joint.LEFT_SHOULDER to TrackedPoint(0.35f, 0.50f, 0.95f),
            Joint.RIGHT_SHOULDER to TrackedPoint(0.65f, 0.50f, 0.95f),
            Joint.RIGHT_WRIST to TrackedPoint(0.65f, 0.90f, 0.90f)
        )
        val result = engine.evaluate(frame1, 0.10)
        assertEquals(Gesture.NONE, result)
    }
}
