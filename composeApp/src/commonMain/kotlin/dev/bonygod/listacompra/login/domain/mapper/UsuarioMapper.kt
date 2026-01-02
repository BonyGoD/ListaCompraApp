package dev.bonygod.listacompra.login.domain.mapper

import dev.bonygod.listacompra.login.data.model.UserResponse
import dev.bonygod.listacompra.login.domain.model.Usuario

fun UserResponse.toDomain(): Usuario {
    return Usuario(
        uid = this.uid,
        nombre = this.nombre,
        email = this.email,
        apiKey = this.apiKey,
        listas = this.listas
    )
}