package dev.bonygod.crashlytics.kmp.core

/**
 * Implementación no-op de [CrashReporter].
 *
 * Se usa antes de [CrashlyticsKMP.initialize] y como fallback si la
 * inicialización falla, de forma que la API nunca lanza ni devuelve `null`
 * aunque el SDK nativo no esté disponible.
 */
public object NoOpCrashReporter : CrashReporter {
    override fun log(message: String) {}

    override fun setUserId(userId: String?) {}

    override fun setCustomKey(key: String, value: String) {}
    override fun setCustomKey(key: String, value: Boolean) {}
    override fun setCustomKey(key: String, value: Int) {}
    override fun setCustomKey(key: String, value: Long) {}
    override fun setCustomKey(key: String, value: Double) {}

    override fun setCustomKeys(keys: Map<String, Any>) {}

    override fun recordException(throwable: Throwable, message: String?, keys: Map<String, Any>) {}

    override fun setCollectionEnabled(enabled: Boolean) {}

    override fun sendUnsentReports() {}

    override fun deleteUnsentReports() {}

    override fun forceCrash() {}
}
