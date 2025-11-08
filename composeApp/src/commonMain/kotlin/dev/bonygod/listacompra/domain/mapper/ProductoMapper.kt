package dev.bonygod.listacompra.domain.mapper

import dev.bonygod.listacompra.data.datasource.model.entity.ProductoResponse
import dev.bonygod.listacompra.domain.model.Producto

fun ProductoResponse.toDomain(): Producto {
    return Producto(
        id = this.id,
        nombre = this.producto,
        fecha = this.fecha,
        isImportant = this.isImportant
    )
}
