//
//  AdPreloader.swift
//  iosApp
//
//  Created by BonyGoD on 5/2/26.
//

import Foundation
import GoogleMobileAds
import UIKit

/// Singleton para precargar y gestionar anuncios intersticiales en iOS
@objc public class AdPreloader: NSObject {

    @objc public static let shared = AdPreloader()

    private var interstitialAd: InterstitialAd?
    private var isLoading = false

    private override init() {
        super.init()
        setupNotificationObservers()
    }

    // MARK: - Notification Observers

    private func setupNotificationObservers() {
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handlePreloadRequest),
            name: NSNotification.Name("AdPreloaderPreloadRequested"),
            object: nil
        )

        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handleShowRequest),
            name: NSNotification.Name("AdPreloaderShowRequested"),
            object: nil
        )

        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handleIsReadyRequest),
            name: NSNotification.Name("AdPreloaderIsReadyRequested"),
            object: nil
        )
    }

    @objc private func handlePreloadRequest(_ notification: Notification) {
        guard let adUnitId = notification.userInfo?["adUnitId"] as? String else {
            print("❌ [AdPreloader] Missing adUnitId in preload request")
            return
        }
        preloadAd(adUnitId: adUnitId)
    }

    @objc private func handleShowRequest(_ notification: Notification) {
        print("🔵 [AdPreloader] Show request received")

        // Obtener el root view controller
        guard let rootVC = UIApplication.shared.connectedScenes
            .compactMap({ $0 as? UIWindowScene })
            .flatMap({ $0.windows })
            .first(where: { $0.isKeyWindow })?.rootViewController else {
            print("❌ [AdPreloader] No root view controller found")
            NotificationCenter.default.post(
                name: NSNotification.Name("AdPreloaderShowFailed"),
                object: nil,
                userInfo: ["error": "No root view controller"]
            )
            return
        }

        showAd(
            from: rootVC,
            onAdShown: {
                NotificationCenter.default.post(
                    name: NSNotification.Name("AdPreloaderAdShown"),
                    object: nil
                )
            },
            onAdDismissed: {
                NotificationCenter.default.post(
                    name: NSNotification.Name("AdPreloaderAdDismissed"),
                    object: nil
                )
            },
            onAdFailedToShow: { error in
                NotificationCenter.default.post(
                    name: NSNotification.Name("AdPreloaderShowFailed"),
                    object: nil,
                    userInfo: ["error": error]
                )
            }
        )
    }

    @objc private func handleIsReadyRequest(_ notification: Notification) {
        let ready = isAdReady()
        NotificationCenter.default.post(
            name: NSNotification.Name("AdPreloaderIsReadyResponse"),
            object: nil,
            userInfo: ["isReady": ready]
        )
    }

    // MARK: - Public Methods

    @objc public func preloadAd(adUnitId: String) {
        if isLoading || interstitialAd != nil {
            print("🟡 [AdPreloader] Ad already loading or loaded")
            return
        }

        isLoading = true
        print("🟡 [AdPreloader] Preloading interstitial ad: \(adUnitId)")

        let request = Request()

        InterstitialAd.load(
            withAdUnitID: adUnitId,
            request: request
        ) { [weak self] ad, error in
            guard let self = self else { return }

            self.isLoading = false

            if let error = error {
                print("❌ [AdPreloader] Failed to preload ad: \(error.localizedDescription)")
                return
            }

            print("✅ [AdPreloader] Interstitial ad preloaded successfully")
            self.interstitialAd = ad
        }
    }

    @objc public func showAd(
        from viewController: UIViewController,
        onAdShown: @escaping () -> Void,
        onAdDismissed: @escaping () -> Void,
        onAdFailedToShow: @escaping (String) -> Void
    ) {
        guard let interstitial = interstitialAd else {
            print("❌ [AdPreloader] No ad loaded to show")
            onAdFailedToShow("No ad loaded")
            return
        }

        print("🟢 [AdPreloader] Showing preloaded interstitial ad")

        interstitial.fullScreenContentDelegate = self

        // Guardar los callbacks para usarlos en el delegate
        self.onAdShownCallback = onAdShown
        self.onAdDismissedCallback = onAdDismissed
        self.onAdFailedToShowCallback = onAdFailedToShow

        interstitial.present(fromRootViewController: viewController)
    }

    @objc public func isAdReady() -> Bool {
        return interstitialAd != nil
    }

    // Callbacks privados
    private var onAdShownCallback: (() -> Void)?
    private var onAdDismissedCallback: (() -> Void)?
    private var onAdFailedToShowCallback: ((String) -> Void)?
}

// MARK: - FullScreenContentDelegate
extension AdPreloader: FullScreenContentDelegate {

    public func adDidPresentFullScreenContent(_ ad: FullScreenPresentingAd) {
        print("✅ [AdPreloader] Ad presented full screen content")
        onAdShownCallback?()
    }

    public func adDidDismissFullScreenContent(_ ad: FullScreenPresentingAd) {
        print("👋 [AdPreloader] Ad dismissed")
        interstitialAd = nil
        onAdDismissedCallback?()

        // Limpiar callbacks
        onAdShownCallback = nil
        onAdDismissedCallback = nil
        onAdFailedToShowCallback = nil
    }

    public func ad(_ ad: FullScreenPresentingAd, didFailToPresentFullScreenContentWithError error: Error) {
        print("❌ [AdPreloader] Failed to show ad: \(error.localizedDescription)")
        interstitialAd = nil
        onAdFailedToShowCallback?(error.localizedDescription)

        // Limpiar callbacks
        onAdShownCallback = nil
        onAdDismissedCallback = nil
        onAdFailedToShowCallback = nil
    }
}
