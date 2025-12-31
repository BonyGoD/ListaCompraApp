package dev.bonygod.listacompra.home.data.datasource.model.entity

import kotlinx.serialization.Serializable

@Serializable
data class ProductoResponse(
    val id: String = "",
    val producto: String = "",
    val fecha: String = "",
    val isImportant: Boolean = false
)