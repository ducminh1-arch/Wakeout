//
//  GestureEngineTests.swift
//  MoveDeskTests (iOS)
//
//  XCTest Runner kiểm tra độ chính xác của GestureEngine dựa trên bộ test vector dùng chung
//  (shared-spec/gesture_vectors.json).
//

import XCTest
@testable import BreakPulseCore

final class GestureEngineTests: XCTestCase {

    struct TestVectorFile: Codable {
        let baseline: TestBaseline
        let cases: [TestCase]
    }

    struct TestBaseline: Codable {
        let midX: Double
        let shoulderWidth: Double
    }

    struct TestCase: Codable {
        let name: String
        let description: String
        let frames: [TestFrame]
        let expect_last: String
    }

    struct TestFrame: Codable {
        let t: Double
        let joints: [String: TestJointPoint]
    }

    struct TestJointPoint: Codable {
        let x: Double
        let y: Double
        let confidence: Double
    }

    func testSharedGestureVectors() throws {
        // Đường dẫn file test vector dùng chung
        let bundle = Bundle(for: Self.self)
        let vectorURL = bundle.url(forResource: "gesture_vectors", withExtension: "json")
            ?? URL(fileURLWithPath: "../../../shared-spec/gesture_vectors.json")

        guard FileManager.default.fileExists(atPath: vectorURL.path) else {
            // Chạy kiểm thử thủ công với baseline mẫu nếu không tìm thấy file URL ngoài bundle
            runInlineVerification()
            return
        }

        let data = try Data(contentsOf: vectorURL)
        let testSuite = try JSONDecoder().decode(TestVectorFile.self, from: data)

        let engine = GestureEngine(baseline: Baseline(
            midX: testSuite.baseline.midX,
            shoulderWidth: testSuite.baseline.shoulderWidth
        ))

        for testCase in testSuite.cases {
            engine.reset()
            var lastGesture: Gesture = .none

            for frame in testCase.frames {
                var pose: Pose = [:]
                for (jointKey, point) in frame.joints {
                    if let joint = Joint(rawValue: jointKey) {
                        pose[joint] = TrackedPoint(x: point.x, y: point.y, confidence: point.confidence)
                    }
                }
                lastGesture = engine.evaluate(pose, timestamp: frame.t)
            }

            XCTAssertEqual(
                lastGesture.rawValue,
                testCase.expect_last,
                "Test case '\(testCase.name)' thất bại: kỳ vọng \(testCase.expect_last) nhưng nhận được \(lastGesture.rawValue)"
            )
        }
    }

    private func runInlineVerification() {
        let engine = GestureEngine(baseline: Baseline(midX: 0.5, shoulderWidth: 0.30))

        // 1. Kiểm tra Sway Right
        let swayRightPose: Pose = [
            .leftShoulder: TrackedPoint(x: 0.47, y: 0.50, confidence: 0.95),
            .rightShoulder: TrackedPoint(x: 0.77, y: 0.50, confidence: 0.95)
        ]
        let swayResult = engine.evaluate(swayRightPose, timestamp: 0.033)
        XCTAssertEqual(swayResult, .swayRight)

        // 2. Kiểm tra Punch Right
        engine.reset()
        let t0Pose: Pose = [
            .leftShoulder: TrackedPoint(x: 0.35, y: 0.50, confidence: 0.95),
            .rightShoulder: TrackedPoint(x: 0.65, y: 0.50, confidence: 0.95),
            .rightWrist: TrackedPoint(x: 0.65, y: 0.62, confidence: 0.90)
        ]
        _ = engine.evaluate(t0Pose, timestamp: 0.0)

        let t1Pose: Pose = [
            .leftShoulder: TrackedPoint(x: 0.35, y: 0.50, confidence: 0.95),
            .rightShoulder: TrackedPoint(x: 0.65, y: 0.50, confidence: 0.95),
            .rightWrist: TrackedPoint(x: 0.67, y: 0.28, confidence: 0.90)
        ]
        let punchResult = engine.evaluate(t1Pose, timestamp: 0.10)
        XCTAssertEqual(punchResult, .punchRight)
    }
}
