// swift-tools-version: 5.9
// The swift-tools-version declares the minimum version of Swift required to build this package.

import PackageDescription

let package = Package(
    name: "AdMobKMPSwift",
    platforms: [
        .iOS(.v15)
    ],
    products: [
        .library(
            name: "AdMobKMPSwift",
            targets: ["AdMobKMPSwift"]
        ),
    ],
    dependencies: [
        .package(url: "https://github.com/googleads/swift-package-manager-google-mobile-ads.git", from: "12.0.0")
    ],
    targets: [
        .target(
            name: "AdMobKMPSwift",
            dependencies: [
                .product(name: "GoogleMobileAds", package: "swift-package-manager-google-mobile-ads")
            ],
            path: "Sources/AdMobKMPSwift"
        )
    ]
)
