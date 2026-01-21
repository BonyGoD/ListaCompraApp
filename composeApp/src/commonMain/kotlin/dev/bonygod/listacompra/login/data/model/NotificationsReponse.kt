package dev.bonygod.listacompra.login.data.model

import kotlinx.serialization.Serializable

@Serializable
data class NotificationsReponse(
    val nombre: String = "",
    val email: String = "",
    val listaId: String = ""
)