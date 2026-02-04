import SwiftUI
import Firebase
import GoogleSignIn
import GoogleSignInKMPSwift
import GoogleMobileAds
import AdMobKMPSwift

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil) -> Bool {
        print("🔵 [AppDelegate] didFinishLaunchingWithOptions called")

        FirebaseApp.configure()
        print("✅ [AppDelegate] Firebase configured")

        // Inicializar el helper para escuchar las notificaciones de Kotlin (GoogleSignIn)
        _ = GoogleAuthCallbackHelper.shared
        print("✅ [AppDelegate] GoogleAuthCallbackHelper initialized")

        // Inicializar Google Mobile Ads SDK
        print("🔵 [AppDelegate] Starting Google Mobile Ads...")
        MobileAds.shared.start { status in
            print("✅ [AppDelegate] Google Mobile Ads started with status: \(status)")
        }

        // Inicializar el helper para escuchar las notificaciones de Kotlin (AdMob)
        _ = AdMobCallbackHelper.shared
        print("✅ [AppDelegate] AdMobCallbackHelper initialized")

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
