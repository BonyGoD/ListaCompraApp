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
        let bannerView = BannerView(adSize: AdSizeBanner)
        bannerView.adUnitID = adUnitId
        bannerView.rootViewController = UIApplication.shared.windows.first?.rootViewController
        bannerView.delegate = self

        self.bannerView = bannerView

        // Establecer el frame del banner antes de agregarlo
        bannerView.frame = CGRect(x: 0, y: 0, width: 320, height: 50)
        addSubview(bannerView)

        // Load ad
        let request = Request()
        bannerView.load(request)
    }

    public override func layoutSubviews() {
        super.layoutSubviews()
        // Actualizar el frame del banner para que coincida con el bounds del contenedor
        bannerView?.frame = bounds
    }

    // Tamaño intrínseco para que Compose/UIKit sepa el tamaño del banner
    public override var intrinsicContentSize: CGSize {
        return CGSize(width: 320, height: 50)
    }
}

// MARK: - BannerViewDelegate
extension AdMobBannerView: BannerViewDelegate {

    public func bannerViewDidReceive(_ bannerView: BannerView) {
        onAdLoaded()
    }

    public func bannerView(_ bannerView: BannerView, didFailToReceiveAdWith error: Error) {
        onAdFailed(error.localizedDescription)
    }
}
