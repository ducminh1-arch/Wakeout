//
//  AppState.swift
//  BreakPulse (iOS)
//
//  Persistent Observable State for Watts, Streaks, and Activities
//

import SwiftUI
import Combine

public struct ActivityItem: Identifiable, Sendable {
    public let id = UUID()
    public let title: String
    public let watts: Int
    public let timeAgo: String
    public let emoji: String

    public init(title: String, watts: Int, timeAgo: String, emoji: String) {
        self.title = title
        self.watts = watts
        self.timeAgo = timeAgo
        self.emoji = emoji
    }
}

@MainActor
public final class AppState: ObservableObject {
    @Published public var todayWatts: Int {
        didSet { UserDefaults.standard.set(todayWatts, forKey: "today_watts") }
    }
    @Published public var lifetimeWatts: Int {
        didSet { UserDefaults.standard.set(lifetimeWatts, forKey: "lifetime_watts") }
    }
    @Published public var dayStreak: Int {
        didSet { UserDefaults.standard.set(dayStreak, forKey: "day_streak") }
    }
    @Published public var workoutsCount: Int {
        didSet { UserDefaults.standard.set(workoutsCount, forKey: "workouts_count") }
    }
    @Published public var challengesCount: Int {
        didSet { UserDefaults.standard.set(challengesCount, forKey: "challenges_count") }
    }
    @Published public var wigglesDoneToday: Int {
        didSet { UserDefaults.standard.set(wigglesDoneToday, forKey: "wiggles_done") }
    }
    @Published public var selectedTab: Int = 0

    @Published public var recentActivities: [ActivityItem] = [
        ActivityItem(title: "Watt Invaders (1 Min)", watts: 56, timeAgo: "15m ago", emoji: "🎮"),
        ActivityItem(title: "Single-Leg Balance", watts: 50, timeAgo: "1h ago", emoji: "🏆"),
        ActivityItem(title: "Neck Lateral Stretch", watts: 15, timeAgo: "2h ago", emoji: "🧘"),
        ActivityItem(title: "Wiggle Break", watts: 10, timeAgo: "3h ago", emoji: "🔔"),
        ActivityItem(title: "Morning Desk Stretch", watts: 15, timeAgo: "5h ago", emoji: "🧘")
    ]

    public init() {
        let def = UserDefaults.standard
        let storedWatts = def.integer(forKey: "today_watts")
        self.todayWatts = storedWatts > 0 ? storedWatts : 318

        let storedLifetime = def.integer(forKey: "lifetime_watts")
        self.lifetimeWatts = storedLifetime > 0 ? storedLifetime : 971388

        let storedStreak = def.integer(forKey: "day_streak")
        self.dayStreak = storedStreak > 0 ? storedStreak : 3

        let storedWorkouts = def.integer(forKey: "workouts_count")
        self.workoutsCount = storedWorkouts > 0 ? storedWorkouts : 1203

        let storedChallenges = def.integer(forKey: "challenges_count")
        self.challengesCount = storedChallenges > 0 ? storedChallenges : 29

        let storedWiggles = def.integer(forKey: "wiggles_done")
        self.wigglesDoneToday = storedWiggles > 0 ? storedWiggles : 2
    }

    public func addWatts(_ amount: Int, activityTitle: String, emoji: String) {
        todayWatts += amount
        lifetimeWatts += amount
        workoutsCount += 1
        recentActivities.insert(ActivityItem(title: activityTitle, watts: amount, timeAgo: "Just now", emoji: emoji), at: 0)
        if recentActivities.count > 25 {
            recentActivities.removeLast()
        }
    }

    public func completeChallenge(_ amount: Int, challengeTitle: String) {
        todayWatts += amount
        lifetimeWatts += amount
        challengesCount += 1
        workoutsCount += 1
        recentActivities.insert(ActivityItem(title: challengeTitle, watts: amount, timeAgo: "Just now", emoji: "🏆"), at: 0)
    }

    public func recordWiggle() {
        wigglesDoneToday += 1
        todayWatts += 10
        lifetimeWatts += 10
        recentActivities.insert(ActivityItem(title: "Wiggle Break", watts: 10, timeAgo: "Just now", emoji: "🔔"), at: 0)
    }
}
