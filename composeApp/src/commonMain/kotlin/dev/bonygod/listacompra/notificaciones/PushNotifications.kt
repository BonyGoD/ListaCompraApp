package dev.bonygod.listacompra.notificaciones

expect object PushNotifications {
    fun initialize()

    suspend fun getToken(): String?

    suspend fun hasPermission(): Boolean
}
