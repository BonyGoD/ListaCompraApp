package dev.bonygod.listacompra.home.domain.mapper

import dev.bonygod.listacompra.home.data.model.entity.ProductoResponse
import dev.bonygod.listacompra.home.domain.model.Producto

fun ProductoResponse.toDomain(): Producto {
    return Producto(
        id = this.id,
        nombre = this.producto,
        fecha = this.fecha,
        isImportant = this.isImportant
    )
}
