//
//  AdMobInterstitialBridge.swift
//  AdMobKMPSwift
//
//  Created by BonyGoD on 3/2/26.
//

import Foundation
import GoogleMobileAds
import UIKit

/// Bridge para manejar Interstitial Ads
@objc public class AdMobInterstitialBridge: NSObject {

    public static var interstitialAd: InterstitialAd?

    /// Carga un anuncio intersticial
    @objc public static func load(
        adUnitId: String,
        completion: @escaping (Bool, String?) -> Void
    ) {
        DispatchQueue.main.async {
            let request = Request()

            InterstitialAd.load(
                with: adUnitId,
                request: request
            ) { ad, error in
                if let error = error {
                    completion(false, error.localizedDescription)
                    return
                }

                interstitialAd = ad
                completion(true, nil)
            }
        }
    }

    /// Muestra el anuncio intersticial
    @objc public static func show(
        completion: @escaping (String, String?) -> Void
    ) {
        DispatchQueue.main.async {
            guard let interstitialAd = interstitialAd else {
                completion("failed", "Ad not loaded")
                return
            }

            guard let rootViewController = UIApplication.shared.windows.first?.rootViewController else {
                completion("failed", "No root view controller")
                return
            }

            // Configurar delegate
            let delegate = InterstitialAdDelegate(completion: completion)
            interstitialAd.fullScreenContentDelegate = delegate

            // Mantener referencia al delegate
            objc_setAssociatedObject(
                interstitialAd,
                &AssociatedKeys.delegateKey,
                delegate,
                .OBJC_ASSOCIATION_RETAIN_NONATOMIC
            )

            do {
                try interstitialAd.present(from: rootViewController)
            } catch {
                completion("failed", error.localizedDescription)
            }
        }
    }

    /// Verifica si hay un anuncio listo
    @objc public static func isReady() -> Bool {
        return interstitialAd != nil
    }
}

// MARK: - Delegate
private class InterstitialAdDelegate: NSObject, FullScreenContentDelegate {

    private let completion: (String, String?) -> Void

    init(completion: @escaping (String, String?) -> Void) {
        self.completion = completion
        super.init()
    }

    func adDidPresent(fullScreenContentAd ad: FullScreenPresentingAd) {
        completion("shown", nil)
    }

    func adDidDismiss(fullScreenContentAd ad: FullScreenPresentingAd) {
        AdMobInterstitialBridge.interstitialAd = nil
        completion("dismissed", nil)
    }

    func ad(_ ad: FullScreenPresentingAd, didFailToPresentFullScreenContentWithError error: Error) {
        AdMobInterstitialBridge.interstitialAd = nil
        completion("failed", error.localizedDescription)
    }
}

// MARK: - Associated Keys
private struct AssociatedKeys {
    static var delegateKey: UInt8 = 0
}
