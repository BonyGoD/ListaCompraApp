package dev.bonygod.listacompra.login.domain.model

data class Notifications(
    val items: List<NotificationItem>
)

data class NotificationItem(
    val nombre: String,
    val email: String,
    val listaId: String
)