//
//  CrashlyticsBridge.swift
//  CrashlyticsKMPSwift
//

import Foundation
import FirebaseCrashlytics

/// Envuelve `Crashlytics.crashlytics()` con métodos estáticos simples, sin
/// estado propio. `CrashlyticsCallbackHelper` es el único punto que llama a
/// este bridge; separado en su propio fichero para mantener el mapeo con el
/// SDK nativo aislado del código de notificaciones.
@objc public class CrashlyticsBridge: NSObject {

    @objc public static func log(_ message: String) {
        Crashlytics.crashlytics().log(message)
    }

    /// `userId` vacío ⇒ limpia el id actual (Crashlytics no distingue "sin id"
    /// de "id vacío", así que forwarding directo es suficiente).
    @objc public static func setUserId(_ userId: String) {
        Crashlytics.crashlytics().setUserID(userId)
    }

    /// Reconstruye el tipo real de `value` a partir de `type`
    /// (`"string"|"bool"|"int"|"long"|"double"`). Si el parseo falla o el tipo
    /// no se reconoce, cae a String.
    @objc public static func setCustomKey(key: String, value: String, type: String) {
        let crashlytics = Crashlytics.crashlytics()

        switch type {
        case "bool":
            if let boolValue = Bool(value) {
                crashlytics.setCustomValue(boolValue, forKey: key)
            } else {
                print("❌ [CrashlyticsKMP-Swift] setCustomKey: \"\(value)\" no es un Bool válido, se guarda como String.")
                crashlytics.setCustomValue(value, forKey: key)
            }
        case "int":
            if let intValue = Int32(value) {
                crashlytics.setCustomValue(intValue, forKey: key)
            } else {
                print("❌ [CrashlyticsKMP-Swift] setCustomKey: \"\(value)\" no es un Int válido, se guarda como String.")
                crashlytics.setCustomValue(value, forKey: key)
            }
        case "long":
            if let longValue = Int64(value) {
                crashlytics.setCustomValue(longValue, forKey: key)
            } else {
                print("❌ [CrashlyticsKMP-Swift] setCustomKey: \"\(value)\" no es un Long válido, se guarda como String.")
                crashlytics.setCustomValue(value, forKey: key)
            }
        case "double":
            if let doubleValue = Double(value) {
                crashlytics.setCustomValue(doubleValue, forKey: key)
            } else {
                print("❌ [CrashlyticsKMP-Swift] setCustomKey: \"\(value)\" no es un Double válido, se guarda como String.")
                crashlytics.setCustomValue(value, forKey: key)
            }
        case "string":
            crashlytics.setCustomValue(value, forKey: key)
        default:
            print("❌ [CrashlyticsKMP-Swift] setCustomKey: tipo \"\(type)\" desconocido, se guarda como String.")
            crashlytics.setCustomValue(value, forKey: key)
        }
    }

    /// Todas las keys llegan ya como String (protocolo Kotlin → Swift).
    @objc public static func setCustomKeys(_ keys: [String: String]) {
        let crashlytics = Crashlytics.crashlytics()
        for (key, value) in keys {
            crashlytics.setCustomValue(value, forKey: key)
        }
    }

    /// Registra `name`/`reason` como excepción no fatal. `keys` se aplican
    /// antes del registro (misma limitación que Android: Crashlytics no tiene
    /// custom keys por-reporte, así que quedan como globales).
    @objc public static func recordError(name: String, reason: String, stackTrace: [String], keys: [String: String]) {
        let crashlytics = Crashlytics.crashlytics()
        for (key, value) in keys {
            crashlytics.setCustomValue(value, forKey: key)
        }

        let model = ExceptionModel(name: name, reason: reason)
        model.stackTrace = KotlinStackTraceMapper.mapFrames(stackTrace).map { symbol in
            StackFrame(symbol: symbol, file: "", line: 0)
        }

        print("🟣 [CrashlyticsKMP-Swift] recordError: \(name) - \(reason) (\(stackTrace.count) frames)")
        crashlytics.record(exceptionModel: model)
    }

    @objc public static func setCollectionEnabled(_ enabled: Bool) {
        print("🟣 [CrashlyticsKMP-Swift] setCollectionEnabled(\(enabled))")
        Crashlytics.crashlytics().setCrashlyticsCollectionEnabled(enabled)
    }

    @objc public static func sendUnsentReports() {
        Crashlytics.crashlytics().sendUnsentReports()
    }

    @objc public static func deleteUnsentReports() {
        Crashlytics.crashlytics().deleteUnsentReports()
    }

    /// Solo para verificación manual. Nunca en un flujo de usuario.
    @objc public static func forceCrash() {
        fatalError("CrashlyticsKMP test crash (iOS)")
    }
}
