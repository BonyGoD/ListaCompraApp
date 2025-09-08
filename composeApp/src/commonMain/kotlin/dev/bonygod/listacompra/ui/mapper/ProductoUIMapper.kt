package dev.bonygod.listacompra.ui.mapper

import dev.bonygod.listacompra.domain.model.Producto
import dev.bonygod.listacompra.ui.model.ListaCompraUI
import dev.bonygod.listacompra.ui.model.ProductoUI

fun Producto.toUI(): ProductoUI {
    return ProductoUI(
        id = this.id,
        nombre = this.nombre,
        fecha = this.fecha
    )
}

fun List<Producto>.toListaCompraUI(): ListaCompraUI {
    return ListaCompraUI(
        productos = this.map { it.toUI() }
    )
}