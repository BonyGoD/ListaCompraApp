package dev.bonygod.listacompra.domain.model

data class Producto(
    val id: String,
    val nombre: String,
    val fecha: String,
    val isImportant: Boolean
)