package dev.bonygod.listacompra.login.domain.mapper

import dev.bonygod.listacompra.home.data.model.entity.ProductoResponse
import dev.bonygod.listacompra.home.domain.model.Producto
import dev.bonygod.listacompra.login.data.model.NotificationsReponse
import dev.bonygod.listacompra.login.data.model.UserResponse
import dev.bonygod.listacompra.login.domain.model.NotificationItem
import dev.bonygod.listacompra.login.domain.model.Notifications
import dev.bonygod.listacompra.login.domain.model.UserRegister
import dev.bonygod.listacompra.login.domain.model.Usuario
import dev.bonygod.listacompra.login.ui.composables.model.AuthUI

fun UserResponse.toDomain(): Usuario {
    return Usuario(
        uid = this.uid,
        nombre = this.nombre,
        email = this.email,
        apiKey = this.apiKey,
        listas = this.listas
    )
}

fun AuthUI.toDomain(): UserRegister {
    return UserRegister(
        userName = this.userName,
        email = this.email,
        password = this.password,
        confirmPassword = this.confirmPassword
    )
}

fun NotificationsReponse.toDomain(): NotificationItem {
    return NotificationItem(
        nombre = nombre,
        email = email,
        listaId = listaId
    )
}
