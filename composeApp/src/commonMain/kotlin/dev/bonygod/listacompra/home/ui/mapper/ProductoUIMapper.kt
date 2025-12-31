package dev.bonygod.listacompra.home.ui.mapper

import dev.bonygod.listacompra.home.domain.model.Producto
import dev.bonygod.listacompra.home.ui.model.ListaCompraUI
import dev.bonygod.listacompra.home.ui.model.ProductoUI

fun Producto.toUI(): ProductoUI {
    return ProductoUI(
        id = this.id,
        nombre = this.nombre,
        fecha = this.fecha,
        isImportant = this.isImportant,
    )
}

fun List<Producto>.toListaCompraUI(): ListaCompraUI {
    return ListaCompraUI(
        productos = this.map { it.toUI() }
    )
}