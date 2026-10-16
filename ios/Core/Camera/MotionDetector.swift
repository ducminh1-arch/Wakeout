//
//  MotionDetector.swift
//  MoveDesk (iOS)
//
//  AVFoundation Camera Session kết hợp Apple Vision Framework (VNDetectHumanBodyPoseRequest).
//  Tối ưu: Xoay thẳng đứng portrait + mirror ngay tại AVCaptureConnection,
//  chạy trực tiếp trên Apple Neural Engine (ANE) on-device, không lưu khung hình vào bộ nhớ đĩa.
//

import Foundation
import Vision
import AVFoundation

public final class MotionDetector: NSObject, AVCaptureVideoDataOutputSampleBufferDelegate, @unchecked Sendable {
    public let session = AVCaptureSession()
    private let output = AVCaptureVideoDataOutput()
    private let videoQueue = DispatchQueue(label: "movedesk.camera.video", qos: .userInitiated)
    private let poseRequest = VNDetectHumanBodyPoseRequest()

    /// Callback nhận diện Pose mới nhất (trả về trên Main Thread hoặc background queue tùy chọn)
    public var onPose: (@MainActor (Pose) -> Void)?

    private static let jointMap: [VNHumanBodyPoseObservation.JointName: Joint] = [
        .nose: .nose,
        .leftShoulder: .leftShoulder,
        .rightShoulder: .rightShoulder,
        .leftElbow: .leftElbow,
        .rightElbow: .rightElbow,
        .leftWrist: .leftWrist,
        .rightWrist: .rightWrist,
        .leftHip: .leftHip,
        .rightHip: .rightHip,
        .leftKnee: .leftKnee,
        .rightKnee: .rightKnee,
        .leftAnkle: .leftAnkle,
        .rightAnkle: .rightAnkle
    ]

    public func configure() throws {
        session.beginConfiguration()
        session.sessionPreset = .hd1280x720

        guard let camera = AVCaptureDevice.default(.builtInWideAngleCamera, for: .video, position: .front),
              let input = try? AVCaptureDeviceInput(device: camera),
              session.canAddInput(input) else {
            session.commitConfiguration()
            throw NSError(domain: "MotionDetector", code: 1, userInfo: [NSLocalizedDescriptionKey: "Không thể khởi tạo camera trước"])
        }
        session.addInput(input)

        output.alwaysDiscardsLateVideoFrames = true
        output.setSampleBufferDelegate(self, queue: videoQueue)
        guard session.canAddOutput(output) else {
            session.commitConfiguration()
            throw NSError(domain: "MotionDetector", code: 2, userInfo: [NSLocalizedDescriptionKey: "Không thể thêm video output"])
        }
        session.addOutput(output)

        // Xoay portrait + mirror ngay tại nguồn hardware
        if let connection = output.connection(with: .video) {
            if connection.isVideoRotationAngleSupported(90) {
                connection.videoRotationAngle = 90
            }
            if connection.isVideoMirroringSupported {
                connection.automaticallyAdjustsVideoMirroring = false
                connection.isVideoMirrored = true
            }
        }

        session.commitConfiguration()
    }

    public func start() {
        videoQueue.async { [session] in
            if !session.isRunning {
                session.startRunning()
            }
        }
    }

    public func stop() {
        videoQueue.async { [session] in
            if session.isRunning {
                session.stopRunning()
            }
        }
    }

    public func captureOutput(
        _ output: AVCaptureOutput,
        didOutput sampleBuffer: CMSampleBuffer,
        from connection: AVCaptureConnection
    ) {
        guard let pixelBuffer = CMSampleBufferGetImageBuffer(sampleBuffer) else { return }

        // Vì buffer đã được xoay 90 độ và mirror ở connection, orientation truyền vào Vision là .up
        let handler = VNImageRequestHandler(cvPixelBuffer: pixelBuffer, orientation: .up, options: [:])
        do {
            try handler.perform([poseRequest])
            guard let observation = poseRequest.results?.first else { return }

            var pose: Pose = [:]
            let recognizedPoints = try observation.recognizedPoints(.all)

            for (jointName, point) in recognizedPoints {
                guard let joint = Self.jointMap[jointName], point.confidence > 0.3 else { continue }
                // Vision có gốc tọa độ dưới-trái -> chuyển sang chuẩn trên-trái (1 - y)
                pose[joint] = TrackedPoint(
                    x: Double(point.location.x),
                    y: Double(1.0 - point.location.y),
                    confidence: Double(point.confidence)
                )
            }

            let callback = onPose
            Task { @MainActor in
                callback?(pose)
            }
        } catch {
            // Khung hình lỗi bỏ qua để giữ 30 FPS mượt mà
        }
    }
}
