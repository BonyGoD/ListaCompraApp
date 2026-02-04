//
//  AdMobBannerView.swift
//  AdMobKMPSwift
//
//  Created by BonyGoD on 3/2/26.
//

import Foundation
import GoogleMobileAds
import UIKit

/// UIView wrapper para BannerView
@objc public class AdMobBannerView: UIView {

    private var bannerView: BannerView?
    private let adUnitId: String
    private let onAdLoaded: () -> Void
    private let onAdFailed: (String) -> Void

    @objc public init(
        adUnitId: String,
        onAdLoaded: @escaping () -> Void,
        onAdFailed: @escaping (String) -> Void
    ) {
        self.adUnitId = adUnitId
        self.onAdLoaded = onAdLoaded
        self.onAdFailed = onAdFailed
        super.init(frame: .zero)
        setupBanner()
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    private func setupBanner() {
        print("🟣 [AdMobBannerView] Setting up banner with adUnitId: \(adUnitId)")

        let bannerView = BannerView(adSize: AdSizeBanner)
        bannerView.adUnitID = adUnitId

        // Obtener el rootViewController correctamente
        let rootVC = UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .flatMap { $0.windows }
            .first { $0.isKeyWindow }?.rootViewController

        print("🟣 [AdMobBannerView] RootViewController: \(String(describing: rootVC))")

        bannerView.rootViewController = rootVC
        bannerView.delegate = self

        self.bannerView = bannerView

        // Establecer el frame del banner antes de agregarlo
        bannerView.frame = CGRect(x: 0, y: 0, width: 320, height: 50)
        print("🟣 [AdMobBannerView] GADBannerView frame set to: \(bannerView.frame)")

        addSubview(bannerView)
        print("🟣 [AdMobBannerView] GADBannerView added as subview")

        // Load ad
        let request = Request()
        print("🟣 [AdMobBannerView] Loading ad request...")
        bannerView.load(request)
    }

    public override func layoutSubviews() {
        super.layoutSubviews()
        print("🟣 [AdMobBannerView] layoutSubviews called. Bounds: \(bounds)")

        // Actualizar el frame del banner para que coincida con el bounds del contenedor
        if let banner = bannerView {
            banner.frame = bounds
            print("🟣 [AdMobBannerView] GADBannerView frame updated to: \(banner.frame)")
        }
    }

    // Tamaño intrínseco para que Compose/UIKit sepa el tamaño del banner
    public override var intrinsicContentSize: CGSize {
        return CGSize(width: 320, height: 50)
    }
}

// MARK: - BannerViewDelegate
extension AdMobBannerView: BannerViewDelegate {

    public func bannerViewDidReceive(_ bannerView: BannerView) {
        print("✅ [AdMobBannerView] Banner ad loaded successfully!")
        print("✅ [AdMobBannerView] GADBannerView frame after load: \(bannerView.frame)")
        print("✅ [AdMobBannerView] AdMobBannerView frame: \(self.frame)")
        onAdLoaded()
    }

    public func bannerView(_ bannerView: BannerView, didFailToReceiveAdWith error: Error) {
        print("❌ [AdMobBannerView] Banner ad failed to load: \(error.localizedDescription)")
        onAdFailed(error.localizedDescription)
    }
}
