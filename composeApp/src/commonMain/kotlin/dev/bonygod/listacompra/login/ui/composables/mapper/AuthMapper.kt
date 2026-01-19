package dev.bonygod.listacompra.login.ui.composables.mapper

import dev.bonygod.listacompra.login.domain.model.Usuario
import dev.bonygod.listacompra.login.ui.composables.model.AuthUI

fun Usuario.toUI(): AuthUI {
    return AuthUI(
        uid = this.uid,
        userName = this.nombre,
        email = this.email,
        listas = listas,
        password = "",
        confirmPassword = ""
    )
}