import SwiftUI
import Firebase
import GoogleSignIn
import SignInKMPSwift
import GoogleMobileAds
import AdMobKMPSwift
import CrashlyticsKMPSwift
import ComposeApp

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil) -> Bool {
        FirebaseApp.configure()
        _ = CrashlyticsCallbackHelper.shared   // debe ir DESPUÉS de configure()

        // Inicializar el helper para escuchar las notificaciones de Kotlin (GoogleSignIn)
        _ = SignInCallbackHelper.shared

        // Inicializar Google Mobile Ads SDK
        MobileAds.shared.start()

        // Inicializar el helper para escuchar las notificaciones de Kotlin (AdMob)
        _ = AdMobCallbackHelper.shared

        // Precargar el intersticial usando el Ad Unit ID correcto desde Kotlin
        let preloader = InterstitialAdPreloader()
        if preloader.isInterstitialEnabled() {
            AdPreloader.shared.preloadAd(adUnitId: preloader.getAdUnitId())
        }

        return true
    }

    func application(
        _ app: UIApplication,
        open url: URL,
        options: [UIApplication.OpenURLOptionsKey : Any] = [:]
    ) -> Bool {
        return GIDSignIn.sharedInstance.handle(url)
    }
}

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
