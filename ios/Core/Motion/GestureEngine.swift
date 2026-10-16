//
//  GestureEngine.swift
//  MoveDesk (iOS)
//
//  Bộ phân loại cử chỉ chuyển động chuẩn hóa theo độ rộng vai (shoulder width).
//  Hỗ trợ: Sway (Nghiêng trái/phải có Hysteresis), Punch (Đấm/Vươn tay có cooldown).
//

import Foundation

public enum Gesture: String, Equatable, Sendable, Codable {
    case swayLeft = "SWAY_LEFT"
    case swayRight = "SWAY_RIGHT"
    case punchLeft = "PUNCH_LEFT"
    case punchRight = "PUNCH_RIGHT"
    case none = "NONE"
}

public struct Baseline: Sendable, Codable, Equatable {
    public var midX: Double
    public var shoulderWidth: Double

    public init(midX: Double, shoulderWidth: Double) {
        self.midX = midX
        self.shoulderWidth = shoulderWidth
    }
}

public final class GestureEngine: @unchecked Sendable {
    public var swayThreshold: Double = 0.35
    public var swayReleaseThreshold: Double = 0.20
    public var punchVelocityThreshold: Double = 3.0
    public var punchCooldownSeconds: Double = 0.30

    public private(set) var baseline: Baseline?

    // Trạng thái nội bộ cho Hysteresis & Cooldown
    private var currentSwayState: Gesture = .none
    private var prevReach: [Joint: (distance: Double, timestamp: Double)] = [:]
    private var lastPunchTime: [Joint: Double] = [:]

    public init(baseline: Baseline? = nil) {
        self.baseline = baseline
    }

    public func setBaseline(_ baseline: Baseline) {
        self.baseline = baseline
    }

    /// Hiệu chuẩn vị trí từ các mẫu (khoảng 2-3 giây đầu tiên)
    public func calibrate(samples: [Pose]) {
        let mids = samples.compactMap { Self.shoulderMid($0) }.sorted()
        let widths = samples.compactMap { Self.shoulderWidth($0) }.sorted()
        guard !mids.isEmpty, !widths.isEmpty else { return }

        let medianMid = mids[mids.count / 2]
        let medianWidth = widths[widths.count / 2]
        self.baseline = Baseline(midX: medianMid, shoulderWidth: medianWidth)
    }

    /// Đánh giá cử chỉ từ một khung hình Pose tại thời điểm timestamp (giây)
    public func evaluate(_ pose: Pose, timestamp t: Double) -> Gesture {
        guard let b = baseline, b.shoulderWidth > 0.02 else {
            return .none
        }

        // 1. Kiểm tra cử chỉ Punch (Ưu tiên cao hơn vì có tính thời điểm / burst)
        let punchPairs: [(Joint, Joint, Gesture)] = [
            (.rightWrist, .rightShoulder, .punchRight),
            (.leftWrist, .leftShoulder, .punchLeft)
        ]

        for (wristJoint, shoulderJoint, gesture) in punchPairs {
            if let detectedPunch = checkPunch(
                pose: pose,
                wrist: wristJoint,
                shoulder: shoulderJoint,
                gesture: gesture,
                timestamp: t,
                shoulderWidth: b.shoulderWidth
            ) {
                return detectedPunch
            }
        }

        // 2. Kiểm tra cử chỉ Sway (Nghiêng trái/phải với Hysteresis)
        if let mid = Self.shoulderMid(pose) {
            let normalizedDelta = (mid - b.midX) / b.shoulderWidth

            switch currentSwayState {
            case .swayRight:
                if normalizedDelta > swayReleaseThreshold {
                    return .swayRight
                } else {
                    currentSwayState = .none
                }
            case .swayLeft:
                if normalizedDelta < -swayReleaseThreshold {
                    return .swayLeft
                } else {
                    currentSwayState = .none
                }
            default:
                if normalizedDelta > swayThreshold {
                    currentSwayState = .swayRight
                    return .swayRight
                } else if normalizedDelta < -swayThreshold {
                    currentSwayState = .swayLeft
                    return .swayLeft
                }
            }
        }

        return .none
    }

    private func checkPunch(
        pose: Pose,
        wrist: Joint,
        shoulder: Joint,
        gesture: Gesture,
        timestamp t: Double,
        shoulderWidth: Double
    ) -> Gesture? {
        guard let w = pose[wrist], let s = pose[shoulder],
              w.confidence > 0.4, s.confidence > 0.4 else {
            return nil
        }

        let currentDistance = hypot(w.x - s.x, w.y - s.y) / shoulderWidth
        let prev = prevReach[wrist]
        prevReach[wrist] = (distance: currentDistance, timestamp: t)

        // Kiểm tra cooldown
        if let lastPunch = lastPunchTime[wrist], (t - lastPunch) < punchCooldownSeconds {
            return nil
        }

        // Điều kiện kích hoạt: cổ tay cao hơn vai (y nhỏ hơn) và vận tốc vươn > ngưỡng
        if let prevRecord = prev, t > prevRecord.timestamp {
            let dt = t - prevRecord.timestamp
            let velocity = (currentDistance - prevRecord.distance) / dt

            if velocity > punchVelocityThreshold && w.y < s.y {
                lastPunchTime[wrist] = t
                return gesture
            }
        }

        return nil
    }

    public static func shoulderMid(_ pose: Pose) -> Double? {
        guard let left = pose[.leftShoulder], let right = pose[.rightShoulder],
              left.confidence > 0.4, right.confidence > 0.4 else {
            return nil
        }
        return (left.x + right.x) / 2.0
    }

    public static func shoulderWidth(_ pose: Pose) -> Double? {
        guard let left = pose[.leftShoulder], let right = pose[.rightShoulder],
              left.confidence > 0.4, right.confidence > 0.4 else {
            return nil
        }
        return hypot(right.x - left.x, right.y - left.y)
    }

    public func reset() {
        currentSwayState = .none
        prevReach.removeAll()
        lastPunchTime.removeAll()
    }
}
