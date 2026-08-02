package dev.bonygod.crashlytics.kmp.internal

import dev.bonygod.crashlytics.kmp.core.CrashReporter
import dev.bonygod.crashlytics.kmp.core.CrashlyticsConfig

/** Nombre de la plataforma actual: `"android"` o `"ios"`. Se aplica como custom key en `initialize()`. */
internal expect val platformName: String

/** Crea el [CrashReporter] real de la plataforma actual a partir de [config]. */
internal expect fun createPlatformCrashReporter(config: CrashlyticsConfig): CrashReporter
