// swift-tools-version: 5.9
// The swift-tools-version declares the minimum version of Swift required to build this package.

import PackageDescription

let package = Package(
    name: "CrashlyticsKMPSwift",
    platforms: [
        .iOS(.v15)
    ],
    products: [
        .library(
            name: "CrashlyticsKMPSwift",
            targets: ["CrashlyticsKMPSwift"]
        ),
    ],
    dependencies: [
        .package(url: "https://github.com/firebase/firebase-ios-sdk", from: "12.7.0")
    ],
    targets: [
        .target(
            name: "CrashlyticsKMPSwift",
            dependencies: [
                .product(name: "FirebaseCore", package: "firebase-ios-sdk"),
                .product(name: "FirebaseCrashlytics", package: "firebase-ios-sdk")
            ],
            path: "CrashlyticsKMPSwift/Sources/CrashlyticsKMPSwift"
        )
    ]
)
