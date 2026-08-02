package dev.bonygod.crashlytics.kmp.internal

import dev.bonygod.crashlytics.kmp.core.CrashReporter
import dev.bonygod.crashlytics.kmp.core.CrashlyticsConfig
import kotlin.experimental.ExperimentalNativeApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSNotificationCenter

/**
 * [CrashReporter] real de iOS.
 *
 * No llama a ningún SDK nativo directamente: postea notificaciones que
 * `CrashlyticsCallbackHelper` (Swift Package `CrashlyticsKMPSwift`) escucha y
 * traduce en llamadas a `Crashlytics.crashlytics()`.
 *
 * ⚠️ **Orden de arranque, crítico:** `NSNotificationCenter` no encola nada.
 * Todo lo que se postee antes de que el `AppDelegate` haya ejecutado
 * `_ = CrashlyticsCallbackHelper.shared` se pierde en silencio, sin ningún
 * aviso. Por eso [dev.bonygod.crashlytics.kmp.core.CrashlyticsKMP.initialize]
 * debe llamarse **después** de esa línea: si se llama antes, el
 * `setCollectionEnabled` inicial y las custom keys base de `initialize()` no
 * llegan, y Crashlytics se queda con su configuración por defecto (misma
 * advertencia documentada en el README de `CrashlyticsKMPSwift`).
 *
 * ⚠️ Crashlytics iOS no soporta custom keys por-reporte: las `keys` de
 * [recordException] se aplican como custom keys globales, igual que en
 * `AndroidCrashReporter`.
 */
@OptIn(ExperimentalForeignApi::class)
internal class IosCrashReporter(private val config: CrashlyticsConfig) : CrashReporter {

    override fun log(message: String) {
        post(CrashlyticsNotifications.LOG, mapOf<Any?, Any?>("message" to message))
    }

    override fun setUserId(userId: String?) {
        // "" ⇒ limpiar el id, tal y como espera CrashlyticsBridge.setUserId en Swift.
        post(CrashlyticsNotifications.SET_USER_ID, mapOf<Any?, Any?>("userId" to (userId ?: "")))
    }

    override fun setCustomKey(key: String, value: String) {
        postCustomKey(key, value, type = "string")
    }

    override fun setCustomKey(key: String, value: Boolean) {
        postCustomKey(key, value.toString(), type = "bool")
    }

    override fun setCustomKey(key: String, value: Int) {
        postCustomKey(key, value.toString(), type = "int")
    }

    override fun setCustomKey(key: String, value: Long) {
        postCustomKey(key, value.toString(), type = "long")
    }

    override fun setCustomKey(key: String, value: Double) {
        postCustomKey(key, value.toString(), type = "double")
    }

    override fun setCustomKeys(keys: Map<String, Any>) {
        // Todos los valores viajan como String: Swift castea con `as? [String: String]`,
        // y un tipo no-String en el mapa rompería el cast y descartaría la notificación.
        val stringKeys = keys.mapValues { it.value.toString() }
        post(CrashlyticsNotifications.SET_CUSTOM_KEYS, mapOf<Any?, Any?>("keys" to stringKeys))
    }

    override fun recordException(throwable: Throwable, message: String?, keys: Map<String, Any>) {
        val name = throwable::class.simpleName ?: "KotlinException"
        val reason = message ?: throwable.message ?: ""
        val stackTrace = buildStackTrace(throwable)
        val stringKeys = keys.mapValues { it.value.toString() }

        if (config.verboseLogging) {
            println("🟣 [CrashlyticsKMP] recordException: $name - $reason (${stackTrace.size} frames)")
        }

        post(
            CrashlyticsNotifications.RECORD_ERROR,
            mapOf<Any?, Any?>(
                "name" to name,
                "reason" to reason,
                "stackTrace" to stackTrace,
                "keys" to stringKeys
            )
        )
    }

    override fun setCollectionEnabled(enabled: Boolean) {
        post(CrashlyticsNotifications.SET_COLLECTION_ENABLED, mapOf<Any?, Any?>("enabled" to enabled.toString()))
    }

    override fun sendUnsentReports() {
        post(CrashlyticsNotifications.SEND_UNSENT_REPORTS, userInfo = null)
    }

    override fun deleteUnsentReports() {
        post(CrashlyticsNotifications.DELETE_UNSENT_REPORTS, userInfo = null)
    }

    override fun forceCrash() {
        // El fatalError() real lo ejecuta CrashlyticsBridge en Swift.
        post(CrashlyticsNotifications.FORCE_CRASH, userInfo = null)
    }

    private fun postCustomKey(key: String, value: String, type: String) {
        post(
            CrashlyticsNotifications.SET_CUSTOM_KEY,
            mapOf<Any?, Any?>("key" to key, "value" to value, "type" to type)
        )
    }

    /**
     * [Throwable.getStackTrace] del throwable, seguido de la cadena de `cause`
     * como frames `"Caused by: <clase>: <mensaje>"` y sus propios frames.
     * Nunca devuelve `null`: en el peor caso, lista vacía.
     */
    @OptIn(ExperimentalNativeApi::class)
    private fun buildStackTrace(throwable: Throwable): List<String> {
        val frames = mutableListOf<String>()
        frames.addAll(throwable.getStackTrace())

        var cause = throwable.cause
        while (cause != null) {
            frames.add("Caused by: ${cause::class.simpleName}: ${cause.message ?: ""}")
            frames.addAll(cause.getStackTrace())
            cause = cause.cause
        }

        return frames
    }

    private fun post(name: String, userInfo: Map<Any?, Any?>?) {
        NSNotificationCenter.defaultCenter.postNotificationName(name, `object` = null, userInfo = userInfo)
        if (config.verboseLogging) {
            println("🟣 [CrashlyticsKMP] post($name)")
        }
    }
}
