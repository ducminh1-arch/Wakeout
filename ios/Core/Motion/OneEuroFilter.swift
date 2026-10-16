//
//  OneEuroFilter.swift
//  MoveDesk (iOS)
//
//  Bộ lọc khử rung One-Euro Filter theo spec chung.
//  Loại bỏ hiện tượng rung lắc jitter khi người dùng giữ yên tư thế,
//  đồng thời duy trì phản hồi tức thời (low latency) khi có chuyển động nhanh.
//

import Foundation

public final class OneEuroFilter: @unchecked Sendable {
    private let minCutoff: Double
    private let beta: Double
    private let dCutoff: Double

    private var xPrev: Double?
    private var dxPrev: Double = 0.0
    private var tPrev: Double?

    public init(minCutoff: Double = 1.2, beta: Double = 0.02, dCutoff: Double = 1.0) {
        self.minCutoff = minCutoff
        self.beta = beta
        self.dCutoff = dCutoff
    }

    private func alpha(cutoff: Double, dt: Double) -> Double {
        let tau = 1.0 / (2.0 * .pi * cutoff)
        return 1.0 / (1.0 + tau / dt)
    }

    public func filter(_ x: Double, timestamp t: Double) -> Double {
        guard let xp = xPrev, let tp = tPrev, t > tp else {
            xPrev = x
            tPrev = t
            dxPrev = 0.0
            return x
        }

        let dt = t - tp
        let dx = (x - xp) / dt
        let dxHat = dxPrev + alpha(cutoff: dCutoff, dt: dt) * (dx - dxPrev)
        let cutoff = minCutoff + beta * abs(dxHat)
        let xHat = xp + alpha(cutoff: cutoff, dt: dt) * (x - xp)

        xPrev = xHat
        dxPrev = dxHat
        tPrev = t
        return xHat
    }

    public func reset() {
        xPrev = nil
        tPrev = nil
        dxPrev = 0.0
    }
}
