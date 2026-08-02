package dev.bonygod.crashlytics.kmp.internal

import dev.bonygod.crashlytics.kmp.core.CrashReporter
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.setUnhandledExceptionHook
import kotlin.native.terminateWithUnhandledException

/**
 * Instala un hook global para excepciones Kotlin no capturadas en iOS.
 *
 * ⚠️ **Limitación conocida, aceptada para la v1:** el registro del non-fatal
 * es *best-effort*, no una garantía. `Crashlytics.record(exceptionModel:)`
 * persiste de forma asíncrona, y `terminateWithUnhandledException` mata el
 * proceso justo después de que retorne la notificación — el non-fatal puede
 * no llegar a escribirse a tiempo. Cuando sí llega, Crashlytics además
 * captura la señal del crash como *fatal* (sin stack Kotlin legible), y en la
 * consola de Firebase aparecen dos entradas correlacionadas para el mismo
 * crash; cuando no llega, solo queda el fatal sin stack Kotlin. Unificarlas
 * de forma fiable requeriría lanzar una `NSException` con los frames Kotlin
 * (enfoque tipo CrashKiOS); se deja para una v2.
 */
@OptIn(ExperimentalNativeApi::class)
internal fun installKotlinExceptionHook(reporter: CrashReporter) {
    setUnhandledExceptionHook { throwable ->
        reporter.recordException(throwable, message = "FATAL: unhandled Kotlin exception")
        terminateWithUnhandledException(throwable)
    }
}
