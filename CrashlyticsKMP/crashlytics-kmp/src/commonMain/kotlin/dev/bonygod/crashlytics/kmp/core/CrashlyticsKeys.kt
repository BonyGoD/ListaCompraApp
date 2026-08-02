package dev.bonygod.crashlytics.kmp.core

/**
 * Nombres de custom keys reservados para uso común de la app.
 */
public object CrashlyticsKeys {
    /** `"android"` o `"ios"`. Aplicada automáticamente por [CrashlyticsKMP.initialize]. */
    public const val PLATFORM: String = "platform"

    /** `"debug"` o `"release"`. Aplicada automáticamente por [CrashlyticsKMP.initialize]. */
    public const val BUILD_TYPE: String = "build_type"

    /** Versión de la app. Se pasa vía `defaultCustomKeys` de [CrashlyticsConfig]. */
    public const val APP_VERSION: String = "app_version"

    /** Última pantalla navegada. */
    public const val SCREEN: String = "screen"

    /** Última acción de usuario relevante antes de un posible crash. */
    public const val LAST_ACTION: String = "last_action"
}
