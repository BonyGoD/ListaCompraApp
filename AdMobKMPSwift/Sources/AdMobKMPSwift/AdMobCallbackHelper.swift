//
//  AdMobCallbackHelper.swift
//  AdMobKMPSwift
//
//  Created by BonyGoD on 3/2/26.
//

import Foundation
import UIKit
import GoogleMobileAds

/// Helper singleton para escuchar notificaciones desde Kotlin
@objc public class AdMobCallbackHelper: NSObject {

    @objc public static let shared = AdMobCallbackHelper()

    private override init() {
        super.init()
        setupObservers()
    }

    private func setupObservers() {
        // Observer para cargar banner
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handleLoadBannerRequest),
            name: NSNotification.Name("AdMobLoadBannerRequested"),
            object: nil
        )

        // Observer para cargar interstitial
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handleLoadInterstitialRequest),
            name: NSNotification.Name("AdMobLoadInterstitialRequested"),
            object: nil
        )

        // Observer para mostrar interstitial
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handleShowInterstitialRequest),
            name: NSNotification.Name("AdMobShowInterstitialRequested"),
            object: nil
        )
    }

    @objc private func handleLoadBannerRequest(_ notification: Notification) {
        print("🔵 [AdMob-Swift] handleLoadBannerRequest called")
        print("🔵 [AdMob-Swift] userInfo keys: \(notification.userInfo?.keys ?? [])")

        guard let userInfo = notification.userInfo,
              let adUnitId = userInfo["adUnitId"] as? String,
              let bannerId = userInfo["bannerId"] as? String,
              let containerView = userInfo["containerView"] as? UIView else {
            print("❌ [AdMob-Swift] Missing required parameters in notification")
            return
        }

        print("🔵 [AdMob-Swift] adUnitId: \(adUnitId)")
        print("🔵 [AdMob-Swift] bannerId: \(bannerId)")
        print("✅ [AdMob-Swift] containerView found: \(containerView)")
        print("🔵 [AdMob-Swift] containerView bounds: \(containerView.bounds)")

        // Cargar el banner usando AdMobBannerView
        DispatchQueue.main.async {
            print("🔵 [AdMob-Swift] Creating AdMobBannerView on main thread...")
            let bannerView = AdMobBannerView(
                adUnitId: adUnitId,
                onAdLoaded: {
                    print("✅ [AdMob-Swift] Banner loaded successfully!")
                    NotificationCenter.default.post(
                        name: NSNotification.Name("AdMobBannerLoaded"),
                        object: nil,
                        userInfo: ["bannerId": bannerId]
                    )
                },
                onAdFailed: { error in
                    print("❌ [AdMob-Swift] Banner load failed: \(error)")
                    NotificationCenter.default.post(
                        name: NSNotification.Name("AdMobBannerLoadFailed"),
                        object: nil,
                        userInfo: ["bannerId": bannerId, "error": error]
                    )
                }
            )

            print("🔵 [AdMob-Swift] AdMobBannerView created: \(bannerView)")

            // Configurar el frame del banner para que coincida con el containerView
            // IMPORTANTE: El frame debe establecerse DESPUÉS de crear el banner
            let width = containerView.bounds.width
            let height: CGFloat = 50 // Altura estándar del banner de AdMob
            bannerView.frame = CGRect(x: 0, y: 0, width: width, height: height)
            bannerView.autoresizingMask = [.flexibleWidth, .flexibleHeight]

            print("🔵 [AdMob-Swift] Banner frame set to: \(bannerView.frame)")
            print("🔵 [AdMob-Swift] Adding banner to containerView...")
            containerView.addSubview(bannerView)
            print("✅ [AdMob-Swift] Banner added to containerView. Subviews count: \(containerView.subviews.count)")
            print("🔵 [AdMob-Swift] Final banner frame: \(bannerView.frame)")
        }
    }

    @objc private func handleLoadInterstitialRequest(_ notification: Notification) {
        guard let userInfo = notification.userInfo,
              let adUnitId = userInfo["adUnitId"] as? String else {
            return
        }

        AdMobInterstitialBridge.load(adUnitId: adUnitId) { success, error in
            if success {
                NotificationCenter.default.post(
                    name: NSNotification.Name("AdMobInterstitialLoaded"),
                    object: nil
                )
            } else {
                NotificationCenter.default.post(
                    name: NSNotification.Name("AdMobInterstitialLoadFailed"),
                    object: nil,
                    userInfo: ["error": error ?? "Unknown error"]
                )
            }
        }
    }

    @objc private func handleShowInterstitialRequest(_ notification: Notification) {
        AdMobInterstitialBridge.show { event, error in
            switch event {
            case "shown":
                NotificationCenter.default.post(
                    name: NSNotification.Name("AdMobInterstitialShown"),
                    object: nil
                )
            case "dismissed":
                NotificationCenter.default.post(
                    name: NSNotification.Name("AdMobInterstitialDismissed"),
                    object: nil
                )
            case "failed":
                NotificationCenter.default.post(
                    name: NSNotification.Name("AdMobInterstitialShowFailed"),
                    object: nil,
                    userInfo: ["error": error ?? "Unknown error"]
                )
            default:
                break
            }
        }
    }

    deinit {
        NotificationCenter.default.removeObserver(self)
    }
}
