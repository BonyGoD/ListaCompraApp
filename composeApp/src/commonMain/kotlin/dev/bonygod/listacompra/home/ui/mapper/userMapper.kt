package dev.bonygod.listacompra.home.ui.mapper

import dev.bonygod.listacompra.home.ui.model.UserUI
import dev.bonygod.listacompra.login.domain.model.Usuario

fun Usuario.toUI(): UserUI {
    return UserUI(
        uid = this.uid,
        nombre = this.nombre,
        email = this.email,
        listas = this.listas
    )
}
