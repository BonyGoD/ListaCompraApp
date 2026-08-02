package dev.bonygod.crashlytics.kmp.core

/**
 * Contrato común de reporte de errores de CrashlyticsKMP.
 *
 * Ninguna implementación puede lanzar: cualquier fallo del SDK nativo se absorbe
 * internamente para que nunca interrumpa el flujo de la app. Cualquier llamada
 * realizada antes de [CrashlyticsKMP.initialize] es un no-op silencioso, servido
 * por [NoOpCrashReporter].
 */
public interface CrashReporter {

    /** Registra un mensaje de log asociado al próximo reporte de crash. */
    public fun log(message: String)

    /** Asocia el reporte al usuario indicado. `null` limpia el id actual. */
    public fun setUserId(userId: String?)

    public fun setCustomKey(key: String, value: String)
    public fun setCustomKey(key: String, value: Boolean)
    public fun setCustomKey(key: String, value: Int)
    public fun setCustomKey(key: String, value: Long)
    public fun setCustomKey(key: String, value: Double)

    /** Aplica varias custom keys de una vez. */
    public fun setCustomKeys(keys: Map<String, Any>)

    /**
     * Reporta [throwable] como no fatal.
     *
     * [keys] se aplican solo a este reporte cuando la plataforma lo permite. En
     * Android, Crashlytics no soporta keys por-reporte, así que se aplican como
     * custom keys globales (documentado en la implementación de Android).
     */
    public fun recordException(
        throwable: Throwable,
        message: String? = null,
        keys: Map<String, Any> = emptyMap()
    )

    /** Activa o desactiva la recolección de crashes en tiempo de ejecución. */
    public fun setCollectionEnabled(enabled: Boolean)

    /** Fuerza el envío inmediato de los reportes pendientes. */
    public fun sendUnsentReports()

    /** Descarta los reportes pendientes sin enviarlos. */
    public fun deleteUnsentReports()

    /** Provoca un crash real. Solo para verificación manual, nunca en un flujo de usuario. */
    public fun forceCrash()
}
