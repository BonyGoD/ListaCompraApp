//
//  AdMobBannerView.swift
//  AdMobKMPSwift
//
//  Created by BonyGoD on 3/2/26.
//

import Foundation
import GoogleMobileAds
import UIKit

/// UIView wrapper para GADBannerView
@objc public class AdMobBannerView: UIView {

    private var bannerView: GADBannerView?
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
        let bannerView = GADBannerView(adSize: GADAdSizeBanner)
        bannerView.adUnitID = adUnitId
        bannerView.rootViewController = UIApplication.shared.windows.first?.rootViewController
        bannerView.delegate = self

        self.bannerView = bannerView
        addSubview(bannerView)

        // Load ad
        let request = GADRequest()
        bannerView.load(request)
    }

    public override func layoutSubviews() {
        super.layoutSubviews()
        bannerView?.frame = bounds
    }
}

// MARK: - GADBannerViewDelegate
extension AdMobBannerView: GADBannerViewDelegate {

    public func bannerViewDidReceiveAd(_ bannerView: GADBannerView) {
        onAdLoaded()
    }

    public func bannerView(_ bannerView: GADBannerView, didFailToReceiveAdWithError error: Error) {
        onAdFailed(error.localizedDescription)
    }
}
