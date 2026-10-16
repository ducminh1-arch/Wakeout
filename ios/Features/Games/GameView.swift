//
//  GameView.swift
//  BreakPulse (iOS)
//
//  Seated Micro-Game: Watt Invaders (Wave 6)
//  Sway or tap to steer, punch or tap to fire lasers.
//

import SwiftUI
import AVFoundation

struct TargetModel: Identifiable {
    let id = UUID()
    var x: Double
    var y: Double
    let type: Int // 0 = UFO, 1 = Alien, 2 = Watt
    let speed: Double
}

struct LaserModel: Identifiable {
    let id = UUID()
    var x: Double
    var y: Double
    let speed: Double = 0.045
}

public struct GameView: View {
    @EnvironmentObject private var appState: AppState

    @State private var isRunning = false
    @State private var timeLeft = 60
    @State private var score = 56
    @State private var combo = 28
    @State private var currentWave = 6
    @State private var shipX: Double = 0.5
    @State private var showVictoryDialog = false

    @State private var targets: [TargetModel] = []
    @State private var lasers: [LaserModel] = []
    @State private var gameTimer: Timer?

    @State private var motionDetector: MotionDetector?
    @State private var isCameraReady = false

    public init() {}

    public var body: some View {
        ZStack {
            Color.black.ignoresSafeArea()

            // Subtle live camera feed in background
            if let detector = motionDetector, isCameraReady {
                CameraPreviewView(session: detector.session)
                    .opacity(0.35)
                    .ignoresSafeArea()
            }

            // 1. Interactive Game Stage
            GeometryReader { geo in
                let w = geo.size.width
                let h = geo.size.height

                ZStack {
                    // Starfield subtle dots
                    ForEach(0..<15, id: \.self) { i in
                        Circle()
                            .fill(Color.white.opacity(0.3))
                            .frame(width: 3, height: 3)
                            .position(x: CGFloat((i * 47) % Int(w)), y: CGFloat((i * 83) % Int(h)))
                    }

                    // Lasers
                    ForEach(lasers) { laser in
                        Circle()
                            .fill(Color.wakeoutGreen)
                            .frame(width: 12, height: 12)
                            .position(x: laser.x * w, y: laser.y * h)
                    }

                    // Targets (UFO, Alien, Watt)
                    ForEach(targets) { t in
                        let tx = t.x * w
                        let ty = t.y * h
                        Group {
                            if t.type == 0 {
                                // Purple Flying Saucer UFO
                                ZStack {
                                    Ellipse()
                                        .fill(Color.wakeoutPurple)
                                        .frame(width: 54, height: 22)
                                    Circle()
                                        .fill(Color.cyan)
                                        .frame(width: 18, height: 18)
                                        .offset(y: -5)
                                }
                                .position(x: tx, y: ty)
                            } else if t.type == 1 {
                                // Orange Alien
                                ZStack {
                                    Circle()
                                        .fill(Color.wakeoutOrange)
                                        .frame(width: 36, height: 36)
                                    HStack(spacing: 6) {
                                        Circle().fill(Color.white).frame(width: 6, height: 6)
                                        Circle().fill(Color.white).frame(width: 6, height: 6)
                                    }
                                }
                                .position(x: tx, y: ty)
                            } else {
                                // Glowing Watt
                                ZStack {
                                    Circle()
                                        .fill(Color.wakeoutGreen)
                                        .frame(width: 32, height: 32)
                                    Text("⚡").font(.system(size: 14))
                                }
                                .position(x: tx, y: ty)
                            }
                        }
                    }

                    // Player Spaceship
                    let px = shipX * w
                    let py = h * 0.82
                    ZStack {
                        Circle()
                            .fill(Color.wakeoutOrange)
                            .frame(width: 22, height: 22)
                            .offset(y: 18)
                        Circle()
                            .fill(Color(red: 0.1, green: 0.15, blue: 0.22))
                            .frame(width: 48, height: 48)
                        Circle()
                            .fill(Color.wakeoutGreen)
                            .frame(width: 28, height: 28)
                        Circle()
                            .fill(Color.wakeoutYellow)
                            .frame(width: 10, height: 10)
                    }
                    .position(x: px, y: py)
                }
            }

            // 2. Top HUD Overlay (Wakeout Style: 56 x28 and 60s)
            VStack {
                HStack {
                    HStack(alignment: .bottom, spacing: 6) {
                        Text("\(score)")
                            .font(.system(size: 26, weight: .black))
                            .foregroundColor(.wakeoutYellow)
                        Text("x\(combo)")
                            .font(.system(size: 15, weight: .bold))
                            .foregroundColor(.white)
                    }
                    .padding(.horizontal, 14)
                    .padding(.vertical, 8)
                    .background(Color.black.opacity(0.65))
                    .clipShape(Capsule())

                    Spacer()

                    Text("⏱️ \(timeLeft)s")
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(.white)
                        .padding(.horizontal, 14)
                        .padding(.vertical, 8)
                        .background(Color.black.opacity(0.65))
                        .clipShape(Capsule())
                }
                .padding(.horizontal, 20)
                .padding(.top, 16)

                Spacer()

                // Wave Indicator Banner
                Text("WAVE \(currentWave)")
                    .font(.system(size: 15, weight: .black))
                    .tracking(1)
                    .foregroundColor(.white)
                    .padding(.horizontal, 22)
                    .padding(.vertical, 6)
                    .background(Color.wakeoutPurple.opacity(0.9))
                    .clipShape(Capsule())
                    .padding(.bottom, 130)
            }

            // 3. Touch Steering & Laser Punch Controls
            if isRunning {
                VStack {
                    Spacer()
                    HStack {
                        // Left & Right Steering
                        HStack(spacing: 12) {
                            Button(action: {
                                shipX = max(0.1, shipX - 0.08)
                                triggerHaptic(style: .light)
                            }) {
                                Image(systemName: "arrow.left")
                                    .font(.title2.bold())
                                    .foregroundColor(.white)
                                    .frame(width: 50, height: 50)
                                    .background(Color.white.opacity(0.25))
                                    .clipShape(Circle())
                            }

                            Button(action: {
                                shipX = min(0.9, shipX + 0.08)
                                triggerHaptic(style: .light)
                            }) {
                                Image(systemName: "arrow.right")
                                    .font(.title2.bold())
                                    .foregroundColor(.white)
                                    .frame(width: 50, height: 50)
                                    .background(Color.white.opacity(0.25))
                                    .clipShape(Circle())
                            }
                        }

                        Spacer()

                        // Punch / Shoot Button
                        Button(action: {
                            fireLaser()
                        }) {
                            HStack(spacing: 6) {
                                Text("💥")
                                Text("PUNCH!")
                                    .font(.system(size: 15, weight: .black))
                            }
                            .foregroundColor(.white)
                            .padding(.horizontal, 18)
                            .frame(height: 50)
                            .background(Color.wakeoutOrange)
                            .clipShape(Capsule())
                        }
                    }
                    .padding(.horizontal, 20)
                    .padding(.bottom, 74)
                }
            }

            // 4. Bottom Start / Stop Action Button
            VStack {
                Spacer()
                Button(action: toggleGame) {
                    Text(isRunning ? "⏹️ STOP" : "▶️ START (1 MIN)")
                        .font(.system(size: 16, weight: .black))
                        .foregroundColor(isRunning ? .white : .black)
                        .frame(maxWidth: .infinity)
                        .frame(height: 54)
                        .background(isRunning ? Color.red : Color.wakeoutGreen)
                        .clipShape(RoundedRectangle(cornerRadius: 27))
                }
                .padding(.horizontal, 20)
                .padding(.bottom, 12)
        }
        .onAppear {
            setupCamera()
        }
        .onDisappear {
            motionDetector?.stop()
        }
        .alert("🏆 1-Min Workout Finished!", isPresented: $showVictoryDialog) {
            Button("Play Again", role: .none) {
                startGame()
            }
            Button("Done", role: .cancel) {}
        } message: {
            Text("Awesome active break! You energized your body and earned +\(score) Watts (Max combo: x\(combo)).")
        }
    }

    private func toggleGame() {
        if isRunning {
            stopGame()
        } else {
            startGame()
        }
    }

    private func startGame() {
        isRunning = true
        timeLeft = 60
        score = 56
        combo = 28
        targets.removeAll()
        lasers.removeAll()
        triggerHaptic(style: .medium)

        gameTimer?.invalidate()
        var frame = 0
        gameTimer = Timer.scheduledTimer(withTimeInterval: 0.033, repeats: true) { _ in
            frame += 1
            if frame % 30 == 0 {
                timeLeft -= 1
                currentWave = ((60 - timeLeft) / 10) + 1
                if timeLeft <= 0 {
                    stopGame(completed: true)
                }
            }

            // Spawn targets
            if frame % 25 == 0 && targets.count < 7 {
                targets.append(
                    TargetModel(
                        x: Double.random(in: 0.15...0.85),
                        y: -0.05,
                        type: Int.random(in: 0...2),
                        speed: Double.random(in: 0.007...0.012)
                    )
                )
            }

            // Move lasers
            for i in lasers.indices {
                lasers[i].y -= lasers[i].speed
            }
            lasers.removeAll { $0.y < -0.1 }

            // Move targets & check collisions
            for i in targets.indices {
                targets[i].y += targets[i].speed
            }

            // Collision logic
            for laser in lasers {
                if let hitIndex = targets.firstIndex(where: { t in
                    let dx = t.x - laser.x
                    let dy = t.y - laser.y
                    return (dx * dx + dy * dy) < 0.008
                }) {
                    targets.remove(at: hitIndex)
                    score += 10
                    combo += 1
                    triggerHaptic(style: .light)
                    break
                }
            }

            targets.removeAll { $0.y > 1.1 }
        }
    }

    private func fireLaser() {
        lasers.append(LaserModel(x: shipX - 0.04, y: 0.80))
        lasers.append(LaserModel(x: shipX + 0.04, y: 0.80))
        triggerHaptic(style: .light)
    }

    private func stopGame(completed: Bool = false) {
        isRunning = false
        gameTimer?.invalidate()
        gameTimer = nil
        if completed {
            appState.addWatts(score, activityTitle: "Watt Invaders (1 Min)", emoji: "🎮")
            triggerHaptic(style: .heavy)
            showVictoryDialog = true
        }
    }

    private func triggerHaptic(style: UIImpactFeedbackGenerator.FeedbackStyle) {
        let generator = UIImpactFeedbackGenerator(style: style)
        generator.impactOccurred()
    }

    private func setupCamera() {
        let detector = MotionDetector()
        detector.onPose = { pose in
            if let nose = pose[.nose], isRunning {
                // Tracking head/nose to steer the ship
                let targetX = nose.x
                shipX = max(0.1, min(0.9, shipX + (targetX - shipX) * 0.35))
            }
        }
        do {
            try detector.configure()
            detector.start()
            self.motionDetector = detector
            self.isCameraReady = true
        } catch {
            print("Camera session not configured (e.g. running on Simulator): \(error.localizedDescription)")
            self.isCameraReady = false
        }
    }
}
