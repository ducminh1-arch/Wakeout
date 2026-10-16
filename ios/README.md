# 🍎 Wakeout Native iOS Application (SwiftUI + Apple Vision)

This folder contains the native iOS implementation of **Wakeout**, engineered for iOS 16+ using **SwiftUI**, **AVFoundation**, **Vision Human Body Pose Estimation (Apple Neural Engine)**, and **Core Haptics**.

---

## 🚀 Quick Start with Xcode

### 1. Open in Xcode
Double-click the Xcode project file or run:
```bash
open ios/Wakeout.xcodeproj
```

### 2. Select Device & Run
1. In Xcode's top toolbar, select any **iPhone Simulator** (e.g. *iPhone 16 Pro*, *iPhone 15*, etc.) or your **Physical iPhone / iPad**.
2. Press **Cmd + R** (or click the ▶️ **Play / Run** button).
3. The app will launch with the official Wakeout app icon, LaunchScreen, and full interactive features!

---

## 📱 Features & 5 Authentic Wakeout Tabs

1. **Tab 1: 1-Min Game (`GameView.swift`)**
   - Retro arcade desk exercise micro-game (*Watt Invaders*).
   - Real-time front-camera head sway tracking via Apple Vision (`VNDetectHumanBodyPoseRequest`).
   - On-screen touch steering controls (`◀️` and `▶️`) + `💥 PUNCH!` laser button.
   - Live HUD: Combo badge (`56 x28`), `WAVE 6`, `60s` timer countdown.
   - Victory Dialog that awards Watts into persistent `AppState`.

2. **Tab 2: Daily Challenge (`ChallengeView.swift`)**
   - Exact match to Wakeout: `DAILY CHALLENGE • 45 SEC`, `Single-Leg Balance`, `Desk Push-Ups`, `Chair Squats`.
   - **Live Front Camera Preview** ([CameraPreviewView.swift](file:///d:/Wakeout/ios/Core/Camera/CameraPreviewView.swift)) with smooth posture overlay.
   - Interactive `Start` / `Pause` / `Reset` controls with haptic feedback, awarding +50 Watts.

3. **Tab 3: Desk Stretches (`RoutinesView.swift`)**
   - 12 curated desk stretches across 5 categories (`NECK & SHOULDERS`, `WRISTS & HANDS`, `SPINE & BACK`, `EYES & BREATHING`, `LEGS & CIRCULATION`).
   - Category filtering chips, Hero featured stretch, and carousel list.
   - Interactive stretch player modal with circular countdown ring, play/pause, and haptic completion.

4. **Tab 4: Wiggle Reminder (`ReminderView.swift`)**
   - Toast notification card: `Time to wiggle! Let's play a game!`.
   - Giant circular countdown ring: `29:53 until your next wiggle`.
   - Single-line interval selector pills: `20m`, `30m`, `45m`, `60m`.
   - `⚡ WIGGLE NOW!` button with Core Haptics, awarding +10 Watts.

5. **Tab 5: Watts Activity Log (`WattsView.swift`)**
   - Circular Watts progress display (`⚡ 318 of 1,100 today`).
   - Stats summary: `3 Day Streak`, `1,203 Workouts`, `29 Challenges`.
   - `LIFETIME WATTS • ⚡ 971,388 🏆`.
   - Interactive Calendar Heatmap with tap-to-inspect daily Watts.
   - Modal Activity Log sheet showing recent micro-break timestamps.

---

## 🎨 App Icon & Assets
- **Xcode Asset Catalog**: Located in [Assets.xcassets](file:///d:/Wakeout/ios/Assets.xcassets).
- **Official App Icon**: 1024x1024 App Store icon + all iPhone & iPad sizes with the glowing golden energy lightning bolt and heart pulse.
- **Launch Screen**: Configured in [LaunchScreen.storyboard](file:///d:/Wakeout/ios/LaunchScreen.storyboard) with authentic Wakeout orange branding.
