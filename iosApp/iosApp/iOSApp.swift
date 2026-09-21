import SwiftUI
import Firebase
import FirebaseMessaging
import UserNotifications
import GoogleSignIn
import SignInKMPSwift
import AdMobKMPSwift
import CrashlyticsKMPSwift
import ComposeApp

class AppDelegate: NSObject, UIApplicationDelegate {
    private var isFCMTokenRequestPending = false

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

        UNUserNotificationCenter.current().delegate = self
        Messaging.messaging().delegate = self
        setupPushNotificationsBridge()

        UNUserNotificationCenter.current().getNotificationSettings { settings in
            switch settings.authorizationStatus {
            case .authorized, .provisional, .ephemeral:
                DispatchQueue.main.async {
                    application.registerForRemoteNotifications()
                }
            default:
                break
            }
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

    func application(
        _ application: UIApplication,
        didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data
    ) {
        Messaging.messaging().apnsToken = deviceToken

        if isFCMTokenRequestPending {
            isFCMTokenRequestPending = false
            fetchAndPublishFCMToken()
        }
    }

    func application(
        _ application: UIApplication,
        didFailToRegisterForRemoteNotificationsWithError error: Error
    ) {
        if isFCMTokenRequestPending {
            isFCMTokenRequestPending = false
            #if DEBUG
            print("🔔 [Push-Swift] Fallo registrando en APNs: \(error)")
            #endif
            NotificationCenter.default.post(
                name: NSNotification.Name("FCMTokenResponse"),
                object: nil,
                userInfo: [:]
            )
        }
    }

    private func setupPushNotificationsBridge() {
        NotificationCenter.default.addObserver(
            forName: NSNotification.Name("FCMTokenRequested"),
            object: nil,
            queue: .main
        ) { [weak self] _ in
            if Messaging.messaging().apnsToken == nil {
                self?.isFCMTokenRequestPending = true
                return
            }
            self?.fetchAndPublishFCMToken()
        }
    }

    private func fetchAndPublishFCMToken() {
        Messaging.messaging().token { token, error in
            if let error = error {
                #if DEBUG
                print("🔔 [Push-Swift] Error obteniendo el token FCM: \(error)")
                #endif
            }
            NotificationCenter.default.post(
                name: NSNotification.Name("FCMTokenResponse"),
                object: nil,
                userInfo: token != nil ? ["token": token!] : [:]
            )
        }
    }
}

extension AppDelegate: MessagingDelegate {
    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        #if DEBUG
        print("🔔 [Push-Swift] Token FCM (registro): \(fcmToken ?? "nil")")
        #endif
        guard let fcmToken = fcmToken else { return }
        NotificationCenter.default.post(
            name: NSNotification.Name("FCMTokenRefreshed"),
            object: nil,
            userInfo: ["token": fcmToken]
        )
    }
}

extension AppDelegate: UNUserNotificationCenterDelegate {
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        completionHandler([.banner, .list, .sound])
    }

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        completionHandler()
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
