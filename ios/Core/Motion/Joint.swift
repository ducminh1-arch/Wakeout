//
//  Joint.swift
//  MoveDesk (iOS)
//
//  Tập hợp 13 khớp giao nhau giữa Apple Vision Framework và Google ML Kit.
//  Hệ tọa độ chuẩn hóa: Gốc trên-trái, x tăng sang phải (đã mirror), y tăng xuống dưới.
//

import Foundation

public enum Joint: String, CaseIterable, Sendable, Codable {
    case nose = "NOSE"
    case leftShoulder = "LEFT_SHOULDER"
    case rightShoulder = "RIGHT_SHOULDER"
    case leftElbow = "LEFT_ELBOW"
    case rightElbow = "RIGHT_ELBOW"
    case leftWrist = "LEFT_WRIST"
    case rightWrist = "RIGHT_WRIST"
    case leftHip = "LEFT_HIP"
    case rightHip = "RIGHT_HIP"
    case leftKnee = "LEFT_KNEE"
    case rightKnee = "RIGHT_KNEE"
    case leftAnkle = "LEFT_ANKLE"
    case rightAnkle = "RIGHT_ANKLE"
}

public struct TrackedPoint: Sendable, Codable, Equatable {
    public var x: Double          // 0.0 ... 1.0 (trái -> phải, đã mirror)
    public var y: Double          // 0.0 ... 1.0 (trên -> dưới)
    public var confidence: Double // 0.0 ... 1.0

    public init(x: Double, y: Double, confidence: Double) {
        self.x = x
        self.y = y
        self.confidence = confidence
    }
}

public typealias Pose = [Joint: TrackedPoint]
