//
//  ReminderView.swift
//  BreakPulse (iOS)
//
//  Wiggle Reminder with Giant Circular Countdown & Single-Line Interval Pills
//

import SwiftUI

public struct ReminderView: View {
    @EnvironmentObject private var appState: AppState

    @State private var selectedMinutes = 30
    @State private var secondsRemaining = 29 * 60 + 53
    @State private var isRunning = true
    @State private var showCelebration = false
    @State private var timer: Timer?

    public init() {}

    private var timeFormatted: String {
        let m = secondsRemaining / 60
        let s = secondsRemaining % 60
        return String(format: "%02d:%02d", m, s)
    }

    private var progress: Double {
        let total = Double(selectedMinutes * 60)
        return max(0.01, 1.0 - (Double(secondsRemaining) / total))
    }

    public var body: some View {
        ScrollView(showsIndicators: false) {
            VStack(spacing: 16) {
                // 1. Toast Notification Card (Exact match to sample app screenshot)
                Button(action: {
                    appState.selectedTab = 0
                }) {
                    HStack(spacing: 14) {
                        ZStack {
                            RoundedRectangle(cornerRadius: 16)
                                .fill(Color.wakeoutOrangeLight)
                                .frame(width: 48, height: 48)
                            Text("🔔").font(.title2)
                        }

                        VStack(alignment: .leading, spacing: 2) {
                            Text("Time to wiggle!")
                                .font(.system(size: 16, weight: .black))
                                .foregroundColor(.textMain)
                            Text("Tap to launch 1-min game break")
                                .font(.system(size: 12, weight: .bold))
                                .foregroundColor(.textSecondary)
                        }

                        Spacer()

                        Text("PLAY")
                            .font(.system(size: 11, weight: .black))
                            .foregroundColor(.wakeoutOrange)
                            .padding(.horizontal, 10)
                            .padding(.vertical, 5)
                            .background(Color.wakeoutOrange.opacity(0.12))
                            .clipShape(Capsule())
                    }
                    .padding(16)
                    .background(Color.white)
                    .clipShape(RoundedRectangle(cornerRadius: 22))
                    .shadow(color: Color.black.opacity(0.05), radius: 5, x: 0, y: 2)
                }
                .buttonStyle(.plain)
                .padding(.top, 16)

                // Celebration Banner
                if showCelebration {
                    HStack {
                        Text("🎉 +10 Watts! Great wiggle session!")
                            .font(.system(size: 14, weight: .black))
                            .foregroundColor(.black)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 12)
                    .background(Color.wakeoutGreen)
                    .clipShape(RoundedRectangle(cornerRadius: 16))
                    .transition(.opacity.combined(with: .scale))
                }

                // 2. Main Wiggle Timer Card (Giant Orange Ring)
                VStack(spacing: 20) {
                    VStack(spacing: 4) {
                        Text("Wiggle Reminder")
                            .font(.system(size: 24, weight: .black))
                            .foregroundColor(.textMain)
                        Text("Every \(selectedMinutes) min • 1st round")
                            .font(.system(size: 13, weight: .bold))
                            .foregroundColor(.textSecondary)
                    }

                    // Giant Circular Countdown
                    ZStack {
                        Circle()
                            .stroke(Color(red: 0.90, green: 0.92, blue: 0.94), lineWidth: 14)
                            .frame(width: 190, height: 190)

                        Circle()
                            .trim(from: 0, to: CGFloat(progress))
                            .stroke(Color.wakeoutOrange, style: StrokeStyle(lineWidth: 14, lineCap: .round))
                            .frame(width: 190, height: 190)
                            .rotationEffect(.degrees(-90))
                            .animation(.linear(duration: 1.0), value: progress)

                        VStack(spacing: 2) {
                            Text(timeFormatted)
                                .font(.system(size: 42, weight: .black))
                                .foregroundColor(.textMain)
                                .tracking(-1)
                            Text(isRunning ? "until your next wiggle" : "PAUSED (tap to resume)")
                                .font(.system(size: 12, weight: .bold))
                                .foregroundColor(isRunning ? .textSecondary : .wakeoutOrange)
                        }
                    }
                    .onTapGesture {
                        isRunning.toggle()
                    }

                    // Interval Selection Pills - Fixed horizontal single-line pills
                    HStack(spacing: 8) {
                        ForEach([20, 30, 45, 60], id: \.self) { mins in
                            let isSel = selectedMinutes == mins
                            Button(action: {
                                selectedMinutes = mins
                                secondsRemaining = mins * 60
                                isRunning = true
                                let gen = UIImpactFeedbackGenerator(style: .light)
                                gen.impactOccurred()
                            }) {
                                Text("\(mins)m")
                                    .font(.system(size: 14, weight: .black))
                                    .foregroundColor(isSel ? .white : .textSecondary)
                                    .frame(maxWidth: .infinity)
                                    .frame(height: 44)
                                    .background(isSel ? Color.wakeoutOrange : Color.bgCardSubtle)
                                    .clipShape(RoundedRectangle(cornerRadius: 20))
                            }
                            .buttonStyle(.plain)
                        }
                    }

                    // Completed today badge
                    HStack {
                        Text("⚡ \(appState.wigglesDoneToday) wiggles done today")
                            .font(.system(size: 13, weight: .black))
                            .foregroundColor(.wakeoutGreenDark)
                    }
                    .padding(.horizontal, 14)
                    .padding(.vertical, 8)
                    .background(Color.wakeoutGreenLight)
                    .clipShape(Capsule())
                }
                .padding(24)
                .background(Color.white)
                .clipShape(RoundedRectangle(cornerRadius: 32))
                .shadow(color: Color.black.opacity(0.06), radius: 8, x: 0, y: 3)

                // 3. Bottom Action Button ("WIGGLE NOW!")
                Button(action: triggerWiggleNow) {
                    Text("⚡ WIGGLE NOW!")
                        .font(.system(size: 16, weight: .black))
                        .tracking(1)
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 56)
                        .background(Color.wakeoutOrange)
                        .clipShape(RoundedRectangle(cornerRadius: 28))
                        .shadow(color: Color.wakeoutOrange.opacity(0.35), radius: 8, x: 0, y: 4)
                }
                .buttonStyle(.plain)
                .padding(.bottom, 24)
            }
            .padding(.horizontal, 20)
        }
        .background(Color.bgPage.ignoresSafeArea())
        .onAppear {
            startTimer()
        }
        .onDisappear {
            timer?.invalidate()
        }
    }

    private func startTimer() {
        timer?.invalidate()
        timer = Timer.scheduledTimer(withTimeInterval: 1.0, repeats: true) { _ in
            if isRunning {
                if secondsRemaining > 0 {
                    secondsRemaining -= 1
                } else {
                    secondsRemaining = selectedMinutes * 60
                    let gen = UINotificationFeedbackGenerator()
                    gen.notificationOccurred(.warning)
                }
            }
        }
    }

    private func triggerWiggleNow() {
        let gen = UIImpactFeedbackGenerator(style: .heavy)
        gen.impactOccurred()
        appState.recordWiggle()
        withAnimation {
            showCelebration = true
        }
        secondsRemaining = selectedMinutes * 60

        DispatchQueue.main.asyncAfter(deadline: .now() + 0.8) {
            appState.selectedTab = 0 // Switch to 1-Min Game
        }
    }
}
