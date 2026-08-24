package dev.bonygod.listacompra.core.analytics

import dev.bonygod.listacompra.getPlatform
import dev.gitlive.firebase.analytics.FirebaseAnalytics

class AnalyticsService(private val analytics: FirebaseAnalytics) {

    init {
        // Los builds de depuracion NO envian analitica. Crashlytics ya se apagaba en
        // debug (MainActivity.configureCrashlytics), pero Analytics no, y eso ensuciaba
        // las metricas de produccion: cada instalacion limpia durante el desarrollo
        // —borrar datos, reinstalar, un emulador nuevo— crea un identificador de
        // instancia nuevo que Firebase cuenta como una persona distinta.
        //
        // Se vio el 23 ago 2026: Firebase daba 78 usuarios activos de 30 dias y Play
        // Console, que solo cuenta instalaciones venidas de la tienda, daba entre 7 y 10.
        //
        // El valor se guarda en el dispositivo y sobrevive a los reinicios, asi que en
        // release se pone a true explicitamente para revertir cualquier false anterior.
        try {
            analytics.setAnalyticsCollectionEnabled(!getPlatform().isDebugBuild)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun logEvent(eventName: String, params: Map<String, Any>? = null) {
        try {
            if (params != null) {
                analytics.logEvent(eventName, params)
            } else {
                analytics.logEvent(eventName)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setUserId(userId: String) {
        try {
            analytics.setUserId(userId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setUserProperty(name: String, value: String) {
        try {
            analytics.setUserProperty(name, value)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Eventos específicos de la app
    fun logProductoAdded(nombre: String) {
        logEvent(
            "producto_added",
            mapOf("producto_nombre" to nombre)
        )
    }

    fun logProductoDeleted(nombre: String) {
        logEvent(
            "producto_deleted",
            mapOf("producto_nombre" to nombre)
        )
    }

    fun logProductoUpdated(nombre: String, comprado: Boolean) {
        logEvent(
            "producto_updated",
            mapOf(
                "producto_nombre" to nombre,
                "comprado" to comprado
            )
        )
    }

    fun logListaCleared(totalProductos: Int) {
        logEvent(
            "lista_cleared",
            mapOf("total_productos" to totalProductos)
        )
    }

    fun logScreenView(screenName: String) {
        logEvent(
            "screen_view",
            mapOf("screen_name" to screenName)
        )
    }
}

