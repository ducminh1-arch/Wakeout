// swift-tools-version: 5.9
// The swift-tools-version declares the minimum version of Swift required to build this package.

import PackageDescription

let package = Package(
    name: "BreakPulseCore",
    platforms: [
        .iOS(.v16),
        .macOS(.v13)
    ],
    products: [
        .library(
            name: "BreakPulseCore",
            targets: ["BreakPulseCore"]
        ),
    ],
    dependencies: [],
    targets: [
        .target(
            name: "BreakPulseCore",
            path: "Core"
        ),
        .testTarget(
            name: "BreakPulseCoreTests",
            dependencies: ["BreakPulseCore"],
            path: "Tests"
        ),
    ]
)
