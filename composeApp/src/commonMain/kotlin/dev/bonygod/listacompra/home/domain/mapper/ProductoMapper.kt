package dev.bonygod.listacompra.home.domain.mapper

fun dev.bonygod.listacompra.home.data.datasource.model.entity.ProductoResponse.toDomain(): dev.bonygod.listacompra.home.domain.model.Producto {
    return _root_ide_package_.dev.bonygod.listacompra.home.domain.model.Producto(
        id = this.id,
        nombre = this.producto,
        fecha = this.fecha,
        isImportant = this.isImportant
    )
}
