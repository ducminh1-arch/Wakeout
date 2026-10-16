//
//  ChallengeView.swift
//  Wakeout (iOS)
//
//  Daily Physical Balance & Micro-Challenge with Camera Viewport
//  Authentic match to Wakeout Daily Challenge UI with Camera Preview
//

import SwiftUI
import AVFoundation

struct DailyChallengeItem: Identifiable {
    let id = UUID()
    let title: String
    let subtitle: String
    let duration: Int
    let watts: Int
    let emoji: String
}

public struct ChallengeView: View {
    @EnvironmentObject private var appState: AppState

    let challenges: [DailyChallengeItem] = [
        DailyChallengeItem(title: "Single-Leg Balance", subtitle: "Lift one leg and hold • Activate your core & brain", duration: 45, watts: 50, emoji: "🦩"),
        DailyChallengeItem(title: "Desk Push-Ups", subtitle: "Lean on desk and perform steady push-ups", duration: 30, watts: 40, emoji: "💪"),
        DailyChallengeItem(title: "Chair Squats", subtitle: "Hover 2 inches above chair and hold", duration: 40, watts: 50, emoji: "🪑")
    ]

    @State private var selectedIndex = 0
    @State private var isRunning = false
    @State private var secondsElapsed = 0
    @State private var timer: Timer?
    @State private var showCelebration = false

    @State private var motionDetector: MotionDetector?
    @State private var isCameraReady = false

    public init() {}

    private var currentChallenge: DailyChallengeItem {
        challenges[selectedIndex]
    }

    public var body: some View {
        VStack(spacing: 0) {
            // 1. Top Challenge Header Banner (Sunny Orange - Exact match to Wakeout)
            VStack(spacing: 6) {
                Text("DAILY CHALLENGE • \(currentChallenge.duration) SEC")
                    .font(.system(size: 11, weight: .black))
                    .tracking(1)
                    .foregroundColor(.white)
                    .padding(.horizontal, 12)
                    .padding(.vertical, 4)
                    .background(Color.black.opacity(0.18))
                    .clipShape(Capsule())

                Text(currentChallenge.title)
                    .font(.system(size: 26, weight: .black))
                    .foregroundColor(.white)

                Text(currentChallenge.subtitle)
                    .font(.system(size: 12, weight: .bold))
                    .foregroundColor(.white.opacity(0.92))

                // Selector pills
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        ForEach(challenges.indices, id: \.self) { idx in
                            let c = challenges[idx]
                            let isSel = selectedIndex == idx
                            Text("\(c.emoji) \(c.title)")
                                .font(.system(size: 12, weight: .black))
                                .foregroundColor(isSel ? .wakeoutOrange : .white)
                                .padding(.horizontal, 14)
                                .padding(.vertical, 7)
                                .background(isSel ? Color.white : Color.white.opacity(0.28))
                                .clipShape(Capsule())
                                .onTapGesture {
                                    selectedIndex = idx
                                    resetChallenge()
                                }
                        }
                    }
                    .padding(.horizontal, 20)
                }
                .padding(.top, 4)
            }
            .padding(.top, 16)
            .padding(.bottom, 14)
            .frame(maxWidth: .infinity)
            .background(Color.wakeoutOrange)

            // 2. Camera Viewport Area
            ZStack {
                Color.black

                // Live Camera Preview if available on device
                if let detector = motionDetector, isCameraReady {
                    CameraPreviewView(session: detector.session)
                        .clipped()
                } else {
                    // Fallback for Simulator or permission pending
                    VStack(spacing: 12) {
                        Text(currentChallenge.emoji)
                            .font(.system(size: 72))

                        Text("Camera Active • On-Device AI")
                            .font(.system(size: 13, weight: .bold))
                            .foregroundColor(.white.opacity(0.7))
                    }
                }

                // Top Floating Status Pill
                VStack {
                    HStack {
                        Spacer()
                        HStack(spacing: 6) {
                            Text(currentChallenge.emoji)
                            Text(isRunning ? "Holding strong! \(currentChallenge.duration - secondsElapsed)s left" : "Get ready & tap Start")
                                .font(.system(size: 13, weight: .heavy))
                        }
                        .foregroundColor(.white)
                        .padding(.horizontal, 16)
                        .padding(.vertical, 9)
                        .background(Color.black.opacity(0.72))
                        .clipShape(Capsule())
                        Spacer()
                    }
                    .padding(.top, 16)

                    Spacer()
                }

                // 3. Bottom Challenge HUD Card (White Card - Exact match to user photo)
                VStack {
                    Spacer()
                    HStack {
                        VStack(alignment: .leading, spacing: 2) {
                            let rem = max(0, currentChallenge.duration - secondsElapsed)
                            Text("\(rem)s")
                                .font(.system(size: 34, weight: .black))
                                .foregroundColor(.wakeoutOrange)

                            HStack(spacing: 4) {
                                Text("⚡")
                                    .font(.system(size: 12))
                                Text("\(currentChallenge.watts) watts on the line")
                                    .font(.system(size: 12, weight: .bold))
                                    .foregroundColor(.textSecondary)
                            }
                        }

                        Spacer()

                        HStack(spacing: 10) {
                            if secondsElapsed > 0 && !isRunning {
                                Button("Reset", action: resetChallenge)
                                    .font(.system(size: 13, weight: .bold))
                                    .foregroundColor(.textSecondary)
                                    .padding(.horizontal, 14)
                                    .padding(.vertical, 10)
                                    .background(Color.bgCardSubtle)
                                    .clipShape(Capsule())
                            }

                            Button(action: toggleTimer) {
                                Text(isRunning ? "Pause" : (secondsElapsed > 0 ? "Resume" : "Start"))
                                    .font(.system(size: 16, weight: .black))
                                    .foregroundColor(isRunning ? .white : .black)
                                    .padding(.horizontal, 24)
                                    .frame(height: 48)
                                    .background(isRunning ? Color.red : Color.wakeoutGreen)
                                    .clipShape(RoundedRectangle(cornerRadius: 16))
                            }
                        }
                    }
                    .padding(18)
                    .background(Color.white)
                    .clipShape(RoundedRectangle(cornerRadius: 26))
                    .shadow(color: Color.black.opacity(0.15), radius: 10, x: 0, y: 4)
                    .padding(.horizontal, 16)
                    .padding(.bottom, 16)
                }
            }
            .clipShape(RoundedRectangle(cornerRadius: 24, style: .continuous))
        }
        .background(Color.wakeoutOrange.ignoresSafeArea())
        .onAppear {
            setupCamera()
        }
        .onDisappear {
            motionDetector?.stop()
        }
        .alert("🏆 Challenge Conquered!", isPresented: $showCelebration) {
            Button("Claim Watts", role: .cancel) {
                resetChallenge()
            }
        } message: {
            Text("Incredible balance and focus! You held the entire duration and earned +\(currentChallenge.watts) Watts!")
        }
    }

    private func setupCamera() {
        let detector = MotionDetector()
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

    private func toggleTimer() {
        if isRunning {
            isRunning = false
            timer?.invalidate()
            timer = nil
        } else {
            isRunning = true
            triggerHaptic(style: .medium)
            timer = Timer.scheduledTimer(withTimeInterval: 1.0, repeats: true) { _ in
                secondsElapsed += 1
                if secondsElapsed >= currentChallenge.duration {
                    isRunning = false
                    timer?.invalidate()
                    timer = nil
                    appState.completeChallenge(currentChallenge.watts, challengeTitle: currentChallenge.title)
                    triggerHaptic(style: .heavy)
                    showCelebration = true
                }
            }
        }
    }

    private func resetChallenge() {
        isRunning = false
        secondsElapsed = 0
        timer?.invalidate()
        timer = nil
    }

    private func triggerHaptic(style: UIImpactFeedbackGenerator.FeedbackStyle) {
        let generator = UIImpactFeedbackGenerator(style: style)
        generator.impactOccurred()
    }
}
