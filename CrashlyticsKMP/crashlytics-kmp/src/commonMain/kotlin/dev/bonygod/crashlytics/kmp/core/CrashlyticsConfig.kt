package dev.bonygod.crashlytics.kmp.core

/**
 * Configuración de arranque de [CrashlyticsKMP.initialize].
 */
public data class CrashlyticsConfig(
    /** `true` en builds debug. Determina, junto a [collectionEnabledInDebug], la recolección inicial. */
    public val isDebugBuild: Boolean,
    /** Si la recolección de crashes debe quedar activa en debug. Por defecto, desactivada. */
    public val collectionEnabledInDebug: Boolean = false,
    /** iOS: instala `setUnhandledExceptionHook` para reportar excepciones Kotlin no capturadas. */
    public val installKotlinExceptionHook: Boolean = true,
    /** Custom keys aplicadas automáticamente durante `initialize()`. */
    public val defaultCustomKeys: Map<String, String> = emptyMap(),
    /** Escribe logs de diagnóstico del propio wrapper (`println`). */
    public val verboseLogging: Boolean = false
)
