package dev.bonygod.crashlytics.kmp.internal

import dev.bonygod.crashlytics.kmp.core.CrashReporter
import dev.bonygod.crashlytics.kmp.core.CrashlyticsConfig

internal actual val platformName: String = "ios"

internal actual fun createPlatformCrashReporter(config: CrashlyticsConfig): CrashReporter {
    val reporter = IosCrashReporter(config)
    if (config.installKotlinExceptionHook) {
        installKotlinExceptionHook(reporter)
    }
    return reporter
}
