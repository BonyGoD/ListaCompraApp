package dev.bonygod.crashlytics.kmp.core

import dev.bonygod.crashlytics.kmp.internal.createPlatformCrashReporter
import dev.bonygod.crashlytics.kmp.internal.platformName

/**
 * Punto de entrada único de la librería.
 *
 * Antes de [initialize], [reporter] devuelve [NoOpCrashReporter]: nunca `null`,
 * nunca lanza. [initialize] es idempotente — una segunda llamada es un no-op
 * (con log si `verboseLogging` estaba activo en la configuración original).
 *
 * El estado se guarda en `var`s privadas sin sincronización explícita: basta con
 * llamar a [initialize] una sola vez, desde el arranque de la app (antes de usar
 * [reporter] desde otro punto), igual que hace el resto de wrappers del proyecto
 * (p. ej. `AnalyticsService`).
 */
public object CrashlyticsKMP {

    private var initialized: Boolean = false
    private var platformReporter: CrashReporter = NoOpCrashReporter

    /** `true` una vez que [initialize] se ha ejecutado correctamente. */
    public val isInitialized: Boolean
        get() = initialized

    /** El reportero activo. [NoOpCrashReporter] hasta que [initialize] se complete con éxito. */
    public val reporter: CrashReporter
        get() = platformReporter

    /**
     * Inicializa la librería con [config]. Una segunda llamada es un no-op.
     *
     * Aplica automáticamente, en este orden: `setCollectionEnabled`, la custom
     * key [CrashlyticsKeys.PLATFORM], la custom key [CrashlyticsKeys.BUILD_TYPE]
     * y, por último, `config.defaultCustomKeys`.
     *
     * Si algo falla durante la inicialización, [reporter] queda en
     * [NoOpCrashReporter] — nunca se propaga la excepción al llamador, y una
     * llamada posterior a [initialize] puede reintentarlo.
     */
    public fun initialize(config: CrashlyticsConfig) {
        if (initialized) {
            if (config.verboseLogging) {
                println("🟣 [CrashlyticsKMP] initialize() ignorado: ya estaba inicializado.")
            }
            return
        }

        try {
            val newReporter = createPlatformCrashReporter(config)

            newReporter.setCollectionEnabled(!config.isDebugBuild || config.collectionEnabledInDebug)
            newReporter.setCustomKey(CrashlyticsKeys.PLATFORM, platformName)
            newReporter.setCustomKey(CrashlyticsKeys.BUILD_TYPE, if (config.isDebugBuild) "debug" else "release")
            config.defaultCustomKeys.forEach { (key, value) -> newReporter.setCustomKey(key, value) }

            platformReporter = newReporter
            initialized = true

            if (config.verboseLogging) {
                println("🟣 [CrashlyticsKMP] initialize() completado. platform=$platformName")
            }
        } catch (e: Throwable) {
            println("🟣 [CrashlyticsKMP] initialize() falló, se mantiene NoOpCrashReporter: ${e.message}")
            platformReporter = NoOpCrashReporter
        }
    }
}
