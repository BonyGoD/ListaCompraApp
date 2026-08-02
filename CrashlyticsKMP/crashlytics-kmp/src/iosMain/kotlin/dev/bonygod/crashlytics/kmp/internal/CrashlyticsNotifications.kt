package dev.bonygod.crashlytics.kmp.internal

/**
 * Nombres de las notificaciones del protocolo Kotlin → Swift.
 *
 * ⚠️ Deben coincidir carácter a carácter con el enum `CrashlyticsNotification`
 * de `CrashlyticsKMPSwift/Sources/CrashlyticsKMPSwift/CrashlyticsCallbackHelper.swift`.
 * Un typo aquí no da error de compilación en ningún lado: simplemente no
 * llega nada a Firebase.
 */
internal object CrashlyticsNotifications {
    const val LOG = "CrashlyticsKMPLog"
    const val SET_USER_ID = "CrashlyticsKMPSetUserId"
    const val SET_CUSTOM_KEY = "CrashlyticsKMPSetCustomKey"
    const val SET_CUSTOM_KEYS = "CrashlyticsKMPSetCustomKeys"
    const val RECORD_ERROR = "CrashlyticsKMPRecordError"
    const val SET_COLLECTION_ENABLED = "CrashlyticsKMPSetCollectionEnabled"
    const val SEND_UNSENT_REPORTS = "CrashlyticsKMPSendUnsentReports"
    const val DELETE_UNSENT_REPORTS = "CrashlyticsKMPDeleteUnsentReports"
    const val FORCE_CRASH = "CrashlyticsKMPForceCrash"
}
