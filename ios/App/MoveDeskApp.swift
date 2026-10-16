//
//  MoveDeskApp.swift
//  BreakPulse (iOS)
//
//  Entry point for Native iOS SwiftUI application with 5 Authentic Wakeout Tabs.
//

import SwiftUI

@main
struct MoveDeskApp: App {
    @StateObject private var appState = AppState()

    init() {
        // Configure clean tab bar appearance
        let appearance = UITabBarAppearance()
        appearance.configureWithOpaqueBackground()
        appearance.backgroundColor = UIColor.white
        UITabBar.appearance().standardAppearance = appearance
        UITabBar.appearance().scrollEdgeAppearance = appearance
    }

    var body: some Scene {
        WindowGroup {
            TabView(selection: $appState.selectedTab) {
                GameView()
                    .tabItem {
                        Label("1-Min", systemImage: "gamecontroller.fill")
                    }
                    .tag(0)

                ChallengeView()
                    .tabItem {
                        Label("Challenge", systemImage: "trophy.fill")
                    }
                    .tag(1)

                RoutinesView()
                    .tabItem {
                        Label("Stretches", systemImage: "figure.mind.and.body")
                    }
                    .tag(2)

                ReminderView()
                    .tabItem {
                        Label("Reminder", systemImage: "alarm.fill")
                    }
                    .tag(3)

                WattsView()
                    .tabItem {
                        Label("Watts", systemImage: "bolt.fill")
                    }
                    .tag(4)
            }
            .accentColor(.wakeoutGreenDark)
            .environmentObject(appState)
            .preferredColorScheme(.light)
        }
    }
}
