//
//  RoutinesView.swift
//  BreakPulse (iOS)
//
//  3k+ Curated Desk Stretches & Micro-Workouts Carousel
//

import SwiftUI

public struct RoutineItem: Identifiable, Sendable {
    public let id: String
    public let title: String
    public let category: String
    public let durationSeconds: Int
    public let instructions: String
    public let emoji: String

    public init(id: String, title: String, category: String, durationSeconds: Int, instructions: String, emoji: String) {
        self.id = id
        self.title = title
        self.category = category
        self.durationSeconds = durationSeconds
        self.instructions = instructions
        self.emoji = emoji
    }
}

public struct RoutinesView: View {
    @EnvironmentObject private var appState: AppState

    public let routines: [RoutineItem] = [
        RoutineItem(id: "1", title: "Neck Lateral Stretch", category: "NECK & SHOULDERS", durationSeconds: 30, instructions: "Sit upright in chair, gently tilt head toward right shoulder for 15s, then switch sides.", emoji: "🙆"),
        RoutineItem(id: "2", title: "Shoulder Blade Rolls", category: "NECK & SHOULDERS", durationSeconds: 30, instructions: "Roll both shoulders backward in big circles 10 times, then reverse direction.", emoji: "🔄"),
        RoutineItem(id: "3", title: "Chin Tucks Posture Fix", category: "NECK & SHOULDERS", durationSeconds: 30, instructions: "Gently tuck chin inward like making a double chin. Hold for 3s, repeat 6 times.", emoji: "💆"),
        RoutineItem(id: "4", title: "Prayer Wrist Stretch", category: "WRISTS & HANDS", durationSeconds: 45, instructions: "Place palms together in front of chest and slowly lower them down to stretch forearms.", emoji: "🙏"),
        RoutineItem(id: "5", title: "Finger Claw & Shake", category: "WRISTS & HANDS", durationSeconds: 30, instructions: "Make a tight fist for 2s, then spread fingers wide. Shake hands to release tension.", emoji: "✋"),
        RoutineItem(id: "6", title: "Seated Chair Twist", category: "SPINE & BACK", durationSeconds: 45, instructions: "Place right hand on chair back, twist torso gently right. Switch sides after 20s.", emoji: "🪑"),
        RoutineItem(id: "7", title: "Seated Cat-Cow Stretch", category: "SPINE & BACK", durationSeconds: 45, instructions: "Inhale: arch back and open chest. Exhale: round spine and tuck chin to chest.", emoji: "🐈"),
        RoutineItem(id: "8", title: "Overhead Sky Reach", category: "SPINE & BACK", durationSeconds: 30, instructions: "Interlace fingers, push palms toward ceiling, and lengthen entire spine.", emoji: "🙌"),
        RoutineItem(id: "9", title: "20-20-20 Eye Rest", category: "EYES & BREATHING", durationSeconds: 30, instructions: "Look at an object at least 20 feet away for 20 seconds. Blink slowly 5 times.", emoji: "👀"),
        RoutineItem(id: "10", title: "Box Breathing Reset", category: "EYES & BREATHING", durationSeconds: 60, instructions: "Inhale 4s -> Hold 4s -> Exhale 4s -> Hold 4s. Instantly calms nervous system.", emoji: "🫁"),
        RoutineItem(id: "11", title: "Seated Knee Tucks", category: "LEGS & CIRCULATION", durationSeconds: 45, instructions: "Hold chair edges and lift alternating knees toward chest. 15 reps per leg.", emoji: "🦵"),
        RoutineItem(id: "12", title: "Seated Calf Pump", category: "LEGS & CIRCULATION", durationSeconds: 45, instructions: "Keep feet on floor and rhythmically lift heels as high as possible.", emoji: "🦶")
    ]

    let categories = [
        "ALL", "NECK & SHOULDERS", "WRISTS & HANDS", "SPINE & BACK", "EYES & BREATHING", "LEGS & CIRCULATION"
    ]

    @State private var selectedCategory = "ALL"
    @State private var featuredRoutine: RoutineItem = RoutineItem(id: "1", title: "Neck Lateral Stretch", category: "NECK & SHOULDERS", durationSeconds: 30, instructions: "Sit upright in chair, gently tilt head toward right shoulder for 15s, then switch sides.", emoji: "🙆")
    @State private var activeWorkoutRoutine: RoutineItem?
    @State private var workoutRemaining = 30
    @State private var isWorkoutPlaying = true
    @State private var workoutTimer: Timer?
    @State private var showCompleteAlert = false

    public init() {}

    private var filteredRoutines: [RoutineItem] {
        if selectedCategory == "ALL" { return routines }
        return routines.filter { $0.category == selectedCategory }
    }

    public var body: some View {
        ScrollView(showsIndicators: false) {
            VStack(alignment: .leading, spacing: 18) {
                // Header
                HStack {
                    VStack(alignment: .leading, spacing: 2) {
                        Text("3k+ Desk Stretches")
                            .font(.system(size: 24, weight: .black))
                            .foregroundColor(.textMain)
                        Text("Desk Stretches, Shakes & Jumps")
                            .font(.system(size: 13, weight: .bold))
                            .foregroundColor(.textSecondary)
                    }

                    Spacer()

                    Text("\(filteredRoutines.count) Stretches")
                        .font(.system(size: 12, weight: .black))
                        .foregroundColor(.wakeoutGreenDark)
                        .padding(.horizontal, 10)
                        .padding(.vertical, 6)
                        .background(Color.white)
                        .clipShape(Capsule())
                        .shadow(color: Color.black.opacity(0.06), radius: 4, x: 0, y: 2)
                }
                .padding(.top, 14)

                // Category Chips
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        ForEach(categories, id: \.self) { cat in
                            let isSel = selectedCategory == cat
                            Text(cat)
                                .font(.system(size: 11, weight: .black))
                                .foregroundColor(isSel ? .black : .textSecondary)
                                .padding(.horizontal, 14)
                                .padding(.vertical, 8)
                                .background(isSel ? Color.wakeoutGreen : Color.white)
                                .clipShape(Capsule())
                                .shadow(color: Color.black.opacity(0.04), radius: 3, x: 0, y: 1)
                                .onTapGesture {
                                    selectedCategory = cat
                                    if let first = (cat == "ALL" ? routines.first : routines.first(where: { $0.category == cat })) {
                                        featuredRoutine = first
                                    }
                                }
                        }
                    }
                }

                // Featured Hero Card
                VStack(alignment: .leading, spacing: 14) {
                    HStack {
                        Text(featuredRoutine.category)
                            .font(.system(size: 11, weight: .black))
                            .foregroundColor(.wakeoutGreenDark)
                            .padding(.horizontal, 10)
                            .padding(.vertical, 4)
                            .background(Color.wakeoutGreenLight)
                            .clipShape(RoundedRectangle(cornerRadius: 10))

                        Spacer()

                        Text("⏱️ \(featuredRoutine.durationSeconds)s")
                            .font(.system(size: 13, weight: .bold))
                            .foregroundColor(.textSecondary)
                    }

                    Text(featuredRoutine.title)
                        .font(.system(size: 20, weight: .black))
                        .foregroundColor(.textMain)

                    Text(featuredRoutine.instructions)
                        .font(.system(size: 13))
                        .foregroundColor(.textSecondary)
                        .lineSpacing(3)

                    // Visual Graphic Stage
                    ZStack {
                        RoundedRectangle(cornerRadius: 20)
                            .fill(Color(red: 0.94, green: 0.99, blue: 0.95))
                            .frame(height: 130)

                        Text("\(featuredRoutine.emoji) 🪑")
                            .font(.system(size: 50))
                    }

                    // Start Routine Button
                    Button(action: {
                        startWorkout(featuredRoutine)
                    }) {
                        Text("▶️ START ROUTINE (\(featuredRoutine.durationSeconds)s)")
                            .font(.system(size: 15, weight: .black))
                            .foregroundColor(.black)
                            .frame(maxWidth: .infinity)
                            .frame(height: 52)
                            .background(Color.wakeoutGreen)
                            .clipShape(RoundedRectangle(cornerRadius: 26))
                    }
                }
                .padding(20)
                .background(Color.white)
                .clipShape(RoundedRectangle(cornerRadius: 28))
                .shadow(color: Color.black.opacity(0.06), radius: 6, x: 0, y: 3)

                // Sub-routines Carousel Section
                Text("Quick Desk Stretches")
                    .font(.system(size: 18, weight: .black))
                    .foregroundColor(.textMain)

                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 12) {
                        ForEach(filteredRoutines) { r in
                            VStack(alignment: .leading, spacing: 10) {
                                HStack {
                                    Text(r.emoji).font(.title2)
                                    Spacer()
                                    Text("⏱️ \(r.durationSeconds)s")
                                        .font(.caption.bold())
                                        .foregroundColor(.textSecondary)
                                }

                                Text(r.title)
                                    .font(.system(size: 14, weight: .black))
                                    .foregroundColor(.textMain)
                                    .lineLimit(1)

                                Button(action: {
                                    startWorkout(r)
                                }) {
                                    Text("Play Routine")
                                        .font(.system(size: 12, weight: .black))
                                        .foregroundColor(.black)
                                        .frame(maxWidth: .infinity)
                                        .frame(height: 36)
                                        .background(Color.wakeoutGreen)
                                        .clipShape(RoundedRectangle(cornerRadius: 14))
                                }
                            }
                            .padding(14)
                            .frame(width: 200)
                            .background(Color.white)
                            .clipShape(RoundedRectangle(cornerRadius: 20))
                            .shadow(color: Color.black.opacity(0.04), radius: 4, x: 0, y: 2)
                            .onTapGesture {
                                featuredRoutine = r
                            }
                        }
                    }
                }
                .padding(.bottom, 24)
            }
            .padding(.horizontal, 20)
        }
        .background(Color.bgPage.ignoresSafeArea())
        .sheet(item: $activeWorkoutRoutine) { routine in
            VStack(spacing: 20) {
                Capsule()
                    .fill(Color.gray.opacity(0.3))
                    .frame(width: 40, height: 5)
                    .padding(.top, 12)

                Text(routine.category)
                    .font(.caption.bold())
                    .foregroundColor(.wakeoutGreenDark)
                    .padding(.horizontal, 10)
                    .padding(.vertical, 4)
                    .background(Color.wakeoutGreenLight)
                    .clipShape(Capsule())

                Text(routine.title)
                    .font(.title2.bold())
                    .foregroundColor(.textMain)

                // Timer Ring
                ZStack {
                    Circle()
                        .stroke(Color.gray.opacity(0.15), lineWidth: 14)
                        .frame(width: 160, height: 160)

                    let total = Double(routine.durationSeconds)
                    let prog = 1.0 - (Double(workoutRemaining) / total)
                    Circle()
                        .trim(from: 0, to: CGFloat(prog))
                        .stroke(Color.wakeoutGreen, style: StrokeStyle(lineWidth: 14, lineCap: .round))
                        .frame(width: 160, height: 160)
                        .rotationEffect(.degrees(-90))

                    VStack {
                        Text("\(workoutRemaining)")
                            .font(.system(size: 48, weight: .black))
                            .foregroundColor(.wakeoutGreen)
                        Text("seconds left")
                            .font(.caption.bold())
                            .foregroundColor(.textSecondary)
                    }
                }

                Text(routine.instructions)
                    .font(.subheadline)
                    .foregroundColor(.textSecondary)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 24)

                HStack(spacing: 12) {
                    Button(action: {
                        isWorkoutPlaying.toggle()
                    }) {
                        Text(isWorkoutPlaying ? "Pause" : "Resume")
                            .font(.headline.bold())
                            .foregroundColor(isWorkoutPlaying ? .white : .black)
                            .frame(maxWidth: .infinity)
                            .frame(height: 50)
                            .background(isWorkoutPlaying ? Color.red : Color.wakeoutGreen)
                            .clipShape(RoundedRectangle(cornerRadius: 18))
                    }

                    Button(action: {
                        finishWorkout(routine)
                    }) {
                        Text("Complete")
                            .font(.headline.bold())
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .frame(height: 50)
                            .background(Color.wakeoutOrange)
                            .clipShape(RoundedRectangle(cornerRadius: 18))
                    }
                }
                .padding(.horizontal, 24)

                Spacer()
            }
            .presentationDetents([.fraction(0.65)])
        }
        .alert("🎉 Stretch Completed!", isPresented: $showCompleteAlert) {
            Button("Awesome!", role: .cancel) {}
        } message: {
            Text("You energized your muscles and earned +15 Watts!")
        }
    }

    private func startWorkout(_ routine: RoutineItem) {
        workoutRemaining = routine.durationSeconds
        isWorkoutPlaying = true
        activeWorkoutRoutine = routine

        workoutTimer?.invalidate()
        workoutTimer = Timer.scheduledTimer(withTimeInterval: 1.0, repeats: true) { _ in
            if isWorkoutPlaying {
                if workoutRemaining > 0 {
                    workoutRemaining -= 1
                } else {
                    finishWorkout(routine)
                }
            }
        }
    }

    private func finishWorkout(_ routine: RoutineItem) {
        workoutTimer?.invalidate()
        workoutTimer = nil
        activeWorkoutRoutine = nil
        appState.addWatts(15, activityTitle: routine.title, emoji: "🧘")
        let generator = UINotificationFeedbackGenerator()
        generator.notificationOccurred(.success)
        showCompleteAlert = true
    }
}
