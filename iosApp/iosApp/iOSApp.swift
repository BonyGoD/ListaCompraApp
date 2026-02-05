import SwiftUI
import Firebase
import GoogleSignIn
import GoogleSignInKMPSwift
import GoogleMobileAds
import AdMobKMPSwift
import ComposeApp

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil) -> Bool {
        FirebaseApp.configure()

        // Inicializar el helper para escuchar las notificaciones de Kotlin (GoogleSignIn)
        _ = GoogleAuthCallbackHelper.shared

        // Inicializar Google Mobile Ads SDK
        MobileAds.shared.start()

        // Inicializar el helper para escuchar las notificaciones de Kotlin (AdMob)
        _ = AdMobCallbackHelper.shared

        // Precargar el intersticial usando el Ad Unit ID correcto desde Kotlin
        let adUnitId = InterstitialAdPreloader().getAdUnitId()
        AdPreloader.shared.preloadAd(adUnitId: adUnitId)

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
