//
//  CrashlyticsCallbackHelper.swift
//  CrashlyticsKMPSwift
//

import Foundation

/// Nombres de notificación del protocolo Kotlin → Swift. Única fuente de
/// verdad, para no repetir literales de cadena por el fichero.
enum CrashlyticsNotification {
    static let log = Notification.Name("CrashlyticsKMPLog")
    static let setUserId = Notification.Name("CrashlyticsKMPSetUserId")
    static let setCustomKey = Notification.Name("CrashlyticsKMPSetCustomKey")
    static let setCustomKeys = Notification.Name("CrashlyticsKMPSetCustomKeys")
    static let recordError = Notification.Name("CrashlyticsKMPRecordError")
    static let setCollectionEnabled = Notification.Name("CrashlyticsKMPSetCollectionEnabled")
    static let sendUnsentReports = Notification.Name("CrashlyticsKMPSendUnsentReports")
    static let deleteUnsentReports = Notification.Name("CrashlyticsKMPDeleteUnsentReports")
    static let forceCrash = Notification.Name("CrashlyticsKMPForceCrash")
}

/// Helper singleton para escuchar notificaciones desde Kotlin.
///
/// Traduce cada notificación en una llamada a `CrashlyticsBridge`. Es
/// fire-and-forget: no hay notificaciones de vuelta Swift → Kotlin en esta
/// versión (la API de CrashlyticsKMP no espera resultado).
///
/// No llama a `FirebaseApp.configure()`: eso lo hace la app en su AppDelegate,
/// antes de acceder a `shared`.
@objc public class CrashlyticsCallbackHelper: NSObject {

    @objc public static let shared = CrashlyticsCallbackHelper()

    private override init() {
        super.init()
        setupObservers()
    }

    private func setupObservers() {
        let center = NotificationCenter.default

        center.addObserver(self, selector: #selector(handleLog),
                            name: CrashlyticsNotification.log, object: nil)
        center.addObserver(self, selector: #selector(handleSetUserId),
                            name: CrashlyticsNotification.setUserId, object: nil)
        center.addObserver(self, selector: #selector(handleSetCustomKey),
                            name: CrashlyticsNotification.setCustomKey, object: nil)
        center.addObserver(self, selector: #selector(handleSetCustomKeys),
                            name: CrashlyticsNotification.setCustomKeys, object: nil)
        center.addObserver(self, selector: #selector(handleRecordError),
                            name: CrashlyticsNotification.recordError, object: nil)
        center.addObserver(self, selector: #selector(handleSetCollectionEnabled),
                            name: CrashlyticsNotification.setCollectionEnabled, object: nil)
        center.addObserver(self, selector: #selector(handleSendUnsentReports),
                            name: CrashlyticsNotification.sendUnsentReports, object: nil)
        center.addObserver(self, selector: #selector(handleDeleteUnsentReports),
                            name: CrashlyticsNotification.deleteUnsentReports, object: nil)
        center.addObserver(self, selector: #selector(handleForceCrash),
                            name: CrashlyticsNotification.forceCrash, object: nil)
    }

    @objc private func handleLog(_ notification: Notification) {
        guard let userInfo = notification.userInfo,
              let message = userInfo["message"] as? String else {
            print("❌ [CrashlyticsKMP-Swift] CrashlyticsKMPLog: falta \"message\" en userInfo.")
            return
        }
        CrashlyticsBridge.log(message)
    }

    @objc private func handleSetUserId(_ notification: Notification) {
        guard let userInfo = notification.userInfo,
              let userId = userInfo["userId"] as? String else {
            print("❌ [CrashlyticsKMP-Swift] CrashlyticsKMPSetUserId: falta \"userId\" en userInfo.")
            return
        }
        CrashlyticsBridge.setUserId(userId)
    }

    @objc private func handleSetCustomKey(_ notification: Notification) {
        guard let userInfo = notification.userInfo,
              let key = userInfo["key"] as? String,
              let value = userInfo["value"] as? String,
              let type = userInfo["type"] as? String else {
            print("❌ [CrashlyticsKMP-Swift] CrashlyticsKMPSetCustomKey: faltan campos en userInfo.")
            return
        }
        CrashlyticsBridge.setCustomKey(key: key, value: value, type: type)
    }

    @objc private func handleSetCustomKeys(_ notification: Notification) {
        guard let userInfo = notification.userInfo,
              let keys = userInfo["keys"] as? [String: String] else {
            print("❌ [CrashlyticsKMP-Swift] CrashlyticsKMPSetCustomKeys: falta \"keys\" en userInfo.")
            return
        }
        CrashlyticsBridge.setCustomKeys(keys)
    }

    @objc private func handleRecordError(_ notification: Notification) {
        // "name" y "reason" son obligatorios; "stackTrace"/"keys" ausentes o mal
        // casteados no deben descartar el reporte entero — caen a vacío.
        guard let userInfo = notification.userInfo,
              let name = userInfo["name"] as? String,
              let reason = userInfo["reason"] as? String else {
            print("❌ [CrashlyticsKMP-Swift] CrashlyticsKMPRecordError: faltan \"name\"/\"reason\" en userInfo.")
            return
        }
        let stackTrace = userInfo["stackTrace"] as? [String] ?? []
        let keys = userInfo["keys"] as? [String: String] ?? [:]
        CrashlyticsBridge.recordError(name: name, reason: reason, stackTrace: stackTrace, keys: keys)
    }

    @objc private func handleSetCollectionEnabled(_ notification: Notification) {
        guard let userInfo = notification.userInfo,
              let enabledString = userInfo["enabled"] as? String else {
            print("❌ [CrashlyticsKMP-Swift] CrashlyticsKMPSetCollectionEnabled: falta \"enabled\" en userInfo.")
            return
        }
        switch enabledString {
        case "true":
            CrashlyticsBridge.setCollectionEnabled(true)
        case "false":
            CrashlyticsBridge.setCollectionEnabled(false)
        default:
            print("❌ [CrashlyticsKMP-Swift] CrashlyticsKMPSetCollectionEnabled: valor \"\(enabledString)\" inesperado, se ignora.")
        }
    }

    @objc private func handleSendUnsentReports(_ notification: Notification) {
        CrashlyticsBridge.sendUnsentReports()
    }

    @objc private func handleDeleteUnsentReports(_ notification: Notification) {
        CrashlyticsBridge.deleteUnsentReports()
    }

    @objc private func handleForceCrash(_ notification: Notification) {
        CrashlyticsBridge.forceCrash()
    }

    deinit {
        NotificationCenter.default.removeObserver(self)
    }
}
