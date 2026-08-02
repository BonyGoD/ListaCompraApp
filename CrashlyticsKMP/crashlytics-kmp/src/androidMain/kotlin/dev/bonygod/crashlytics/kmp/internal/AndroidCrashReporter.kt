package dev.bonygod.crashlytics.kmp.internal

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dev.bonygod.crashlytics.kmp.core.CrashReporter
import dev.bonygod.crashlytics.kmp.core.CrashlyticsConfig

private const val TAG = "CrashlyticsKMP"

/**
 * [CrashReporter] real de Android sobre `FirebaseCrashlytics`.
 *
 * ⚠️ Crashlytics Android no soporta custom keys por-reporte: las `keys` pasadas
 * a [recordException] se aplican como custom keys globales y permanecen hasta
 * el siguiente `setCustomKey` (no se limpian tras el reporte).
 *
 * [initialize][dev.bonygod.crashlytics.kmp.core.CrashlyticsKMP.initialize] debe
 * llamarse desde `Application.onCreate()` o después, para garantizar que
 * `FirebaseApp` ya está inicializado.
 */
internal class AndroidCrashReporter(private val config: CrashlyticsConfig) : CrashReporter {

    // Getter y no `by lazy`/constructor: si FirebaseApp aún no está listo,
    // construir este reporter no debe fallar; el fallo se pospone a cada llamada.
    private val crashlytics get() = FirebaseCrashlytics.getInstance()

    override fun log(message: String) {
        try {
            crashlytics.log(message)
        } catch (e: Throwable) {
            Log.e(TAG, "log() falló: ${e.message}", e)
        }
    }

    override fun setUserId(userId: String?) {
        try {
            crashlytics.setUserId(userId ?: "")
        } catch (e: Throwable) {
            Log.e(TAG, "setUserId() falló: ${e.message}", e)
        }
    }

    override fun setCustomKey(key: String, value: String) {
        try {
            crashlytics.setCustomKey(key, value)
        } catch (e: Throwable) {
            Log.e(TAG, "setCustomKey(String) falló: ${e.message}", e)
        }
    }

    override fun setCustomKey(key: String, value: Boolean) {
        try {
            crashlytics.setCustomKey(key, value)
        } catch (e: Throwable) {
            Log.e(TAG, "setCustomKey(Boolean) falló: ${e.message}", e)
        }
    }

    override fun setCustomKey(key: String, value: Int) {
        try {
            crashlytics.setCustomKey(key, value)
        } catch (e: Throwable) {
            Log.e(TAG, "setCustomKey(Int) falló: ${e.message}", e)
        }
    }

    override fun setCustomKey(key: String, value: Long) {
        try {
            crashlytics.setCustomKey(key, value)
        } catch (e: Throwable) {
            Log.e(TAG, "setCustomKey(Long) falló: ${e.message}", e)
        }
    }

    override fun setCustomKey(key: String, value: Double) {
        try {
            crashlytics.setCustomKey(key, value)
        } catch (e: Throwable) {
            Log.e(TAG, "setCustomKey(Double) falló: ${e.message}", e)
        }
    }

    override fun setCustomKeys(keys: Map<String, Any>) {
        // Cada key en su propio try/catch: un fallo en una no debe descartar el resto.
        keys.forEach { (key, value) ->
            try {
                setCustomKeyByType(key, value)
            } catch (e: Throwable) {
                Log.e(TAG, "setCustomKeys() falló en la key \"$key\": ${e.message}", e)
            }
        }
    }

    override fun recordException(throwable: Throwable, message: String?, keys: Map<String, Any>) {
        // Cada paso en su propio try/catch, conservando el orden log → keys → recordException:
        // Crashlytics adjunta al reporte los logs y custom keys existentes en el momento de
        // recordException(), así que un fallo en un paso anterior no debe impedir el registro
        // de la excepción, que es lo único que este método tiene que conseguir siempre.
        if (message != null) {
            try {
                crashlytics.log(message)
            } catch (e: Throwable) {
                Log.e(TAG, "recordException(): log() falló: ${e.message}", e)
            }
        }

        keys.forEach { (key, value) ->
            try {
                setCustomKeyByType(key, value)
            } catch (e: Throwable) {
                Log.e(TAG, "recordException(): setCustomKey falló en la key \"$key\": ${e.message}", e)
            }
        }

        try {
            if (config.verboseLogging) {
                Log.d(TAG, "recordException(): ${throwable::class.simpleName} - ${message ?: throwable.message}")
            }
            crashlytics.recordException(throwable)
        } catch (e: Throwable) {
            Log.e(TAG, "recordException(): recordException() falló: ${e.message}", e)
        }
    }

    override fun setCollectionEnabled(enabled: Boolean) {
        try {
            crashlytics.isCrashlyticsCollectionEnabled = enabled
        } catch (e: Throwable) {
            Log.e(TAG, "setCollectionEnabled() falló: ${e.message}", e)
        }
    }

    override fun sendUnsentReports() {
        try {
            crashlytics.sendUnsentReports()
        } catch (e: Throwable) {
            Log.e(TAG, "sendUnsentReports() falló: ${e.message}", e)
        }
    }

    override fun deleteUnsentReports() {
        try {
            crashlytics.deleteUnsentReports()
        } catch (e: Throwable) {
            Log.e(TAG, "deleteUnsentReports() falló: ${e.message}", e)
        }
    }

    override fun forceCrash() {
        // Sin try/catch: tiene que propagar, es su función.
        throw RuntimeException("CrashlyticsKMP test crash (Android)")
    }

    /** Despacha [value] a la sobrecarga nativa de `setCustomKey` según su tipo real. */
    private fun setCustomKeyByType(key: String, value: Any) {
        when (value) {
            is String -> crashlytics.setCustomKey(key, value)
            is Boolean -> crashlytics.setCustomKey(key, value)
            is Int -> crashlytics.setCustomKey(key, value)
            is Long -> crashlytics.setCustomKey(key, value)
            is Double -> crashlytics.setCustomKey(key, value)
            is Float -> crashlytics.setCustomKey(key, value)
            else -> crashlytics.setCustomKey(key, value.toString())
        }
    }
}
