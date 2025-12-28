package dev.bonygod.listacompra.core.analytics

import dev.gitlive.firebase.analytics.FirebaseAnalytics

class AnalyticsService(private val analytics: FirebaseAnalytics) {

    fun logEvent(eventName: String, params: Map<String, Any>? = null) {
        try {
            if (params != null) {
                analytics.logEvent(eventName, params)
            } else {
                analytics.logEvent(eventName)
            }
            println("Analytics: Event logged - $eventName with params: $params")
        } catch (e: Exception) {
            println("Analytics: Error logging event $eventName - ${e.message}")
            e.printStackTrace()
        }
    }

    fun setUserId(userId: String) {
        try {
            analytics.setUserId(userId)
            println("Analytics: User ID set - $userId")
        } catch (e: Exception) {
            println("Analytics: Error setting user ID - ${e.message}")
            e.printStackTrace()
        }
    }

    fun setUserProperty(name: String, value: String) {
        try {
            analytics.setUserProperty(name, value)
            println("Analytics: User property set - $name: $value")
        } catch (e: Exception) {
            println("Analytics: Error setting user property - ${e.message}")
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

