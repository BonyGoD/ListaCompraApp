package dev.bonygod.crashlytics.kmp.internal

import dev.bonygod.crashlytics.kmp.core.CrashReporter
import dev.bonygod.crashlytics.kmp.core.CrashlyticsConfig

internal actual val platformName: String = "android"

internal actual fun createPlatformCrashReporter(config: CrashlyticsConfig): CrashReporter =
    AndroidCrashReporter(config)
