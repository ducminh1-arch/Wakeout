package com.breakpulse.app.core.gesture

/**
 * Tập hợp 13 khớp giao nhau giữa Google ML Kit và Apple Vision.
 * Hệ tọa độ chuẩn hóa: Gốc trên-trái, x tăng sang phải (đã mirror), y tăng xuống dưới.
 */
enum class Joint {
    NOSE,
    LEFT_SHOULDER,
    RIGHT_SHOULDER,
    LEFT_ELBOW,
    RIGHT_ELBOW,
    LEFT_WRIST,
    RIGHT_WRIST,
    LEFT_HIP,
    RIGHT_HIP,
    LEFT_KNEE,
    RIGHT_KNEE,
    LEFT_ANKLE,
    RIGHT_ANKLE
}

data class TrackedPoint(
    val x: Float,          // 0.0f .. 1.0f (trái -> phải, đã mirror)
    val y: Float,          // 0.0f .. 1.0f (trên -> dưới)
    val confidence: Float  // 0.0f .. 1.0f
)

typealias Pose = Map<Joint, TrackedPoint>
