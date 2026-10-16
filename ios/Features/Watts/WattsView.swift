//
//  WattsView.swift
//  BreakPulse (iOS)
//
//  Watts Activity Log, Lifetime Milestones & Heatmap Calendar
//

import SwiftUI

public struct WattsView: View {
    @EnvironmentObject private var appState: AppState

    @State private var showActivityLog = false
    @State private var selectedDayIndex = 6 // Sunday (Oct 12)

    let dayNames = ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"]
    let dayNumbers = [6, 7, 8, 9, 10, 11, 12]
    let dayActive = [true, true, true, true, true, false, true]

    public init() {}

    private var progress: Double {
        min(1.0, Double(appState.todayWatts) / 1100.0)
    }

    public var body: some View {
        ScrollView(showsIndicators: false) {
            VStack(spacing: 16) {
                // Header
                HStack {
                    Text("Watts")
                        .font(.system(size: 28, weight: .black))
                        .foregroundColor(.textMain)

                    Spacer()

                    Button(action: {
                        showActivityLog = true
                    }) {
                        Text("Activity Log 📋")
                            .font(.system(size: 13, weight: .black))
                            .foregroundColor(.wakeoutGreenDark)
                            .padding(.horizontal, 12)
                            .padding(.vertical, 6)
                            .background(Color.wakeoutGreenLight)
                            .clipShape(Capsule())
                    }
                }
                .padding(.top, 16)

                // Watts Circular Display Card
                VStack(spacing: 8) {
                    ZStack {
                        Circle()
                            .stroke(Color(red: 0.94, green: 0.96, blue: 0.98), lineWidth: 14)
                            .frame(width: 170, height: 170)

                        Circle()
                            .trim(from: 0, to: CGFloat(progress))
                            .stroke(Color.wakeoutGreen, style: StrokeStyle(lineWidth: 14, lineCap: .round))
                            .frame(width: 170, height: 170)
                            .rotationEffect(.degrees(-90))
                            .animation(.easeOut(duration: 0.8), value: progress)

                        VStack(spacing: 2) {
                            Text("⚡ \(appState.todayWatts)")
                                .font(.system(size: 34, weight: .black))
                                .foregroundColor(.wakeoutGreen)
                            Text("of 1,100 today")
                                .font(.system(size: 12, weight: .bold))
                                .foregroundColor(.textSecondary)
                        }
                    }
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 24)
                .background(Color.white)
                .clipShape(RoundedRectangle(cornerRadius: 28))
                .shadow(color: Color.black.opacity(0.05), radius: 6, x: 0, y: 3)

                // 3 Stats Boxes Grid
                HStack(spacing: 10) {
                    VStack(spacing: 4) {
                        Text("\(appState.dayStreak)")
                            .font(.system(size: 24, weight: .black))
                            .foregroundColor(.wakeoutOrange)
                        Text("Day Streak")
                            .font(.system(size: 11, weight: .bold))
                            .foregroundColor(.textSecondary)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 14)
                    .background(Color.white)
                    .clipShape(RoundedRectangle(cornerRadius: 20))
                    .shadow(color: Color.black.opacity(0.04), radius: 4, x: 0, y: 2)

                    VStack(spacing: 4) {
                        Text(String(format: "%d", appState.workoutsCount))
                            .font(.system(size: 24, weight: .black))
                            .foregroundColor(.wakeoutGreen)
                        Text("Workouts")
                            .font(.system(size: 11, weight: .bold))
                            .foregroundColor(.textSecondary)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 14)
                    .background(Color.white)
                    .clipShape(RoundedRectangle(cornerRadius: 20))
                    .shadow(color: Color.black.opacity(0.04), radius: 4, x: 0, y: 2)

                    VStack(spacing: 4) {
                        Text("\(appState.challengesCount)")
                            .font(.system(size: 24, weight: .black))
                            .foregroundColor(.wakeoutBlue)
                        Text("Challenges")
                            .font(.system(size: 11, weight: .bold))
                            .foregroundColor(.textSecondary)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 14)
                    .background(Color.white)
                    .clipShape(RoundedRectangle(cornerRadius: 20))
                    .shadow(color: Color.black.opacity(0.04), radius: 4, x: 0, y: 2)
                }

                // Lifetime Watts Banner
                HStack {
                    VStack(alignment: .leading, spacing: 2) {
                        Text("LIFETIME WATTS")
                            .font(.system(size: 11, weight: .black))
                            .tracking(1)
                            .foregroundColor(.textSecondary)
                        Text("⚡ " + String(format: "%d", appState.lifetimeWatts))
                            .font(.system(size: 24, weight: .black))
                            .foregroundColor(.wakeoutGreenDark)
                    }
                    Spacer()
                    Text("🏆").font(.system(size: 34))
                }
                .padding(18)
                .background(Color(red: 0.94, green: 0.99, blue: 0.95))
                .clipShape(RoundedRectangle(cornerRadius: 20))
                .overlay(
                    RoundedRectangle(cornerRadius: 20)
                        .stroke(Color.wakeoutGreen.opacity(0.3), lineWidth: 1)
                )

                // Calendar Heatmap Card
                VStack(alignment: .leading, spacing: 14) {
                    HStack {
                        Text("Active Days")
                            .font(.system(size: 15, weight: .black))
                            .foregroundColor(.textMain)
                        Spacer()
                        Text("October 2026")
                            .font(.system(size: 12, weight: .bold))
                            .foregroundColor(.textSecondary)
                    }

                    // Days of week
                    HStack {
                        ForEach(dayNames, id: \.self) { d in
                            Text(d)
                                .font(.system(size: 11, weight: .bold))
                                .foregroundColor(.textSecondary)
                                .frame(maxWidth: .infinity)
                        }
                    }

                    // Date badges
                    HStack {
                        ForEach(dayNumbers.indices, id: \.self) { idx in
                            let num = dayNumbers[idx]
                            let active = dayActive[idx]
                            let isSel = selectedDayIndex == idx

                            Text("\(num)")
                                .font(.system(size: 12, weight: .black))
                                .foregroundColor(active || isSel ? .white : .textSecondary)
                                .frame(maxWidth: .infinity)
                                .frame(height: 38)
                                .background(
                                    isSel ? Color.wakeoutGreenDark : (active ? Color.wakeoutGreen : Color.bgCardSubtle)
                                )
                                .clipShape(RoundedRectangle(cornerRadius: 10))
                                .onTapGesture {
                                    selectedDayIndex = idx
                                    let gen = UIImpactFeedbackGenerator(style: .light)
                                    gen.impactOccurred()
                                }
                        }
                    }

                    // Day summary banner
                    HStack {
                        Text("\(dayNames[selectedDayIndex]), Oct \(dayNumbers[selectedDayIndex]):")
                            .font(.system(size: 12, weight: .bold))
                            .foregroundColor(.textSecondary)
                        Spacer()
                        Text(dayNumbers[selectedDayIndex] == 11 ? "Rest day (0 Watts)" : "⚡ 318 Watts earned • Great job!")
                            .font(.system(size: 12, weight: .black))
                            .foregroundColor(dayNumbers[selectedDayIndex] == 11 ? .textSecondary : .wakeoutGreenDark)
                    }
                    .padding(10)
                    .background(Color.bgPage)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                }
                .padding(18)
                .background(Color.white)
                .clipShape(RoundedRectangle(cornerRadius: 24))
                .shadow(color: Color.black.opacity(0.04), radius: 6, x: 0, y: 2)
                .padding(.bottom, 24)
            }
            .padding(.horizontal, 20)
        }
        .background(Color.bgPage.ignoresSafeArea())
        .sheet(isPresented: $showActivityLog) {
            NavigationStack {
                List(appState.recentActivities) { act in
                    HStack(spacing: 12) {
                        Text(act.emoji).font(.title2)
                        VStack(alignment: .leading, spacing: 2) {
                            Text(act.title)
                                .font(.headline.bold())
                                .foregroundColor(.textMain)
                            Text(act.timeAgo)
                                .font(.caption)
                                .foregroundColor(.textSecondary)
                        }
                        Spacer()
                        Text("+\(act.watts)⚡")
                            .font(.subheadline.bold())
                            .foregroundColor(.wakeoutGreenDark)
                            .padding(.horizontal, 8)
                            .padding(.vertical, 4)
                            .background(Color.wakeoutGreenLight)
                            .clipShape(Capsule())
                    }
                    .padding(.vertical, 4)
                }
                .navigationTitle("Activity Log")
                .navigationBarTitleDisplayMode(.inline)
                .toolbar {
                    ToolbarItem(placement: .cancellationAction) {
                        Button("Close") { showActivityLog = false }
                    }
                }
            }
            .presentationDetents([.medium, .large])
        }
    }
}
