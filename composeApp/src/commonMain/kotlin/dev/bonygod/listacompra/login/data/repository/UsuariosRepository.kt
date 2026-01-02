package dev.bonygod.listacompra.login.data.repository

import dev.bonygod.listacompra.login.data.datasource.UsuariosDataSource

class UsuariosRepository(
    private val usuariosDataSource: UsuariosDataSource
) {
    suspend fun registrarUsuario(email: String, password: String) {
        usuariosDataSource.registrarUsuario(email, password)
    }

    suspend fun loginConEmail(email: String, password: String) {
        usuariosDataSource.loginConEmail(email, password)
    }

    suspend fun loginConGoogle(idToken: String) {
        usuariosDataSource.loginConGoogle(idToken)
    }

    suspend fun recuperarContrasena(email: String) {
        usuariosDataSource.recuperarPass(email)
    }

    suspend fun cerrarSesion() {
        usuariosDataSource.cerrarSesion()
    }

    fun obtenerUsuarioActual() {
        usuariosDataSource.obtenerUsuarioActual()
    }
}