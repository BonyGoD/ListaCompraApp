import SwiftUI
import Firebase
import GoogleSignIn
import SignInKMPSwift
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

        // Arranca el SDK de Google Mobile Ads y los puentes de banner e
        // intersticial con Kotlin. La precarga del intersticial no se dispara
        // aquí: la pide Kotlin con AdMobKMP.initializeAds() (MainViewController),
        // que es quien conoce el AdMobConfig y el interruptor interstitialEnabled.
        AdMobKMPBridge.start()

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
