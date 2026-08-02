package dev.bonygod.crashlytics.kmp

import dev.bonygod.crashlytics.kmp.core.CrashlyticsConfig
import dev.bonygod.crashlytics.kmp.core.CrashlyticsKMP
import dev.bonygod.crashlytics.kmp.core.NoOpCrashReporter
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertSame

class CrashlyticsKMPTest {

    /**
     * Un único test para las dos primeras comprobaciones: [CrashlyticsKMP] es un
     * singleton compartido por toda la suite, así que el estado "antes de
     * initialize()" solo se puede observar de forma fiable si se comprueba
     * justo antes de llamar a initialize() dentro del mismo test.
     *
     * No se asume que `initialize()` tenga éxito: en un unit test de JVM
     * `FirebaseCrashlytics.getInstance()` puede fallar por no haber `FirebaseApp`
     * inicializado, y el contrato solo exige que la llamada no lance y que una
     * segunda llamada no cambie la instancia devuelta por `reporter`.
     */
    @Test
    fun reporterEsNoOpAntesDeInitializeYSegundaLlamadaNoCambiaElReporter() {
        assertSame(NoOpCrashReporter, CrashlyticsKMP.reporter)
        assertFalse(CrashlyticsKMP.isInitialized)

        val config = CrashlyticsConfig(isDebugBuild = true)

        // No debe lanzar, tenga éxito o no la creación del reporter real.
        CrashlyticsKMP.initialize(config)
        val reporterTrasPrimeraLlamada = CrashlyticsKMP.reporter

        // Segunda llamada: no-op, el reporter no cambia.
        CrashlyticsKMP.initialize(config)
        assertSame(reporterTrasPrimeraLlamada, CrashlyticsKMP.reporter)
    }

    @Test
    fun ningunMetodoDeNoOpCrashReporterLanza() {
        NoOpCrashReporter.log("mensaje de prueba")
        NoOpCrashReporter.setUserId(null)
        NoOpCrashReporter.setUserId("uid-de-prueba")
        NoOpCrashReporter.setCustomKey("clave", "valor")
        NoOpCrashReporter.setCustomKey("clave", true)
        NoOpCrashReporter.setCustomKey("clave", 1)
        NoOpCrashReporter.setCustomKey("clave", 1L)
        NoOpCrashReporter.setCustomKey("clave", 1.0)
        NoOpCrashReporter.setCustomKeys(mapOf("a" to "b"))
        NoOpCrashReporter.recordException(RuntimeException("boom"), "mensaje", emptyMap())
        NoOpCrashReporter.setCollectionEnabled(true)
        NoOpCrashReporter.sendUnsentReports()
        NoOpCrashReporter.deleteUnsentReports()
        NoOpCrashReporter.forceCrash()
    }
}
