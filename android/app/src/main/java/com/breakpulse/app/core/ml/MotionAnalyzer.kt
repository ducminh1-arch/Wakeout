package com.breakpulse.app.core.ml

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import com.breakpulse.app.core.gesture.Joint
import com.breakpulse.app.core.gesture.Pose
import com.breakpulse.app.core.gesture.TrackedPoint

/**
 * CameraX ImageAnalysis Analyzer kết hợp Google ML Kit Pose Detection.
 * Chuẩn hóa kích thước khung hình sau khi xoay (rotation) và lật gương (mirror) camera trước.
 */
class MotionAnalyzer(
    private val isFrontCamera: Boolean = true,
    private val onPose: (Pose, timestampNs: Long) -> Unit
) : ImageAnalysis.Analyzer {

    private val detector = PoseDetection.getClient(
        PoseDetectorOptions.Builder()
            .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
            .build()
    )

    private val landmarkMap = mapOf(
        PoseLandmark.NOSE to Joint.NOSE,
        PoseLandmark.LEFT_SHOULDER to Joint.LEFT_SHOULDER,
        PoseLandmark.RIGHT_SHOULDER to Joint.RIGHT_SHOULDER,
        PoseLandmark.LEFT_ELBOW to Joint.LEFT_ELBOW,
        PoseLandmark.RIGHT_ELBOW to Joint.RIGHT_ELBOW,
        PoseLandmark.LEFT_WRIST to Joint.LEFT_WRIST,
        PoseLandmark.RIGHT_WRIST to Joint.RIGHT_WRIST,
        PoseLandmark.LEFT_HIP to Joint.LEFT_HIP,
        PoseLandmark.RIGHT_HIP to Joint.RIGHT_HIP,
        PoseLandmark.LEFT_KNEE to Joint.LEFT_KNEE,
        PoseLandmark.RIGHT_KNEE to Joint.RIGHT_KNEE,
        PoseLandmark.LEFT_ANKLE to Joint.LEFT_ANKLE,
        PoseLandmark.RIGHT_ANKLE to Joint.RIGHT_ANKLE
    )

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val rotation = imageProxy.imageInfo.rotationDegrees
        // Kích thước sau khi xoay tương ứng với hệ quy chiếu ML Kit
        val swap = rotation == 90 || rotation == 270
        val width = (if (swap) imageProxy.height else imageProxy.width).toFloat()
        val height = (if (swap) imageProxy.width else imageProxy.height).toFloat()
        val timestampNs = imageProxy.imageInfo.timestamp

        val inputImage = InputImage.fromMediaImage(mediaImage, rotation)

        detector.process(inputImage)
            .addOnSuccessListener { poseResult ->
                val resultMap = HashMap<Joint, TrackedPoint>()

                for ((landmarkType, joint) in landmarkMap) {
                    val landmark = poseResult.getPoseLandmark(landmarkType) ?: continue
                    if (landmark.inFrameLikelihood < 0.3f) continue

                    var x = landmark.position.x / width
                    val y = landmark.position.y / height

                    // Mirror cho camera trước để tạo cảm giác tự nhiên như soi gương
                    if (isFrontCamera) {
                        x = 1.0f - x
                    }

                    resultMap[joint] = TrackedPoint(
                        x = x.coerceIn(0.0f, 1.0f),
                        y = y.coerceIn(0.0f, 1.0f),
                        confidence = landmark.inFrameLikelihood
                    )
                }

                onPose(resultMap, timestampNs)
            }
            .addOnFailureListener {
                // Bỏ qua khung hình lỗi để không nghẽn pipeline
            }
            .addOnCompleteListener {
                // Luôn giải phóng ImageProxy để CameraX nhận khung hình kế tiếp
                imageProxy.close()
            }
    }

    fun close() {
        detector.close()
    }
}
