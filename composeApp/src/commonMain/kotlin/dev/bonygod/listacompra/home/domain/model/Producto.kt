package dev.bonygod.listacompra.home.domain.model

data class Producto(
    val id: String,
    val nombre: String,
    val fecha: String,
    val isImportant: Boolean
)