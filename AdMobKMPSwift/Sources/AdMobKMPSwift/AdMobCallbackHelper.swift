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
        guard let userInfo = notification.userInfo,
              let adUnitId = userInfo["adUnitId"] as? String,
              let bannerId = userInfo["bannerId"] as? String,
              let containerView = userInfo["containerView"] as? UIView else {
            return
        }

        // Cargar el banner usando AdMobBannerView
        DispatchQueue.main.async {
            let bannerView = AdMobBannerView(
                adUnitId: adUnitId,
                onAdLoaded: {
                    NotificationCenter.default.post(
                        name: NSNotification.Name("AdMobBannerLoaded"),
                        object: nil,
                        userInfo: ["bannerId": bannerId]
                    )
                },
                onAdFailed: { error in
                    NotificationCenter.default.post(
                        name: NSNotification.Name("AdMobBannerLoadFailed"),
                        object: nil,
                        userInfo: ["bannerId": bannerId, "error": error]
                    )
                }
            )

            // Configurar el frame y agregar directamente al containerView
            bannerView.frame = containerView.bounds
            bannerView.autoresizingMask = [.flexibleWidth, .flexibleHeight]
            containerView.addSubview(bannerView)
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
