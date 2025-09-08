package dev.bonygod.listacompra.domain.usecase

import dev.bonygod.listacompra.data.repository.ProductosRepository
import dev.bonygod.listacompra.ui.model.ListaCompraUI
import dev.bonygod.listacompra.ui.mapper.toListaCompraUI
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetProductosUseCase(
    private val productosRepository: ProductosRepository
) {
    operator fun invoke(): Flow<ListaCompraUI> {
        return productosRepository.getProductos()
            .map { productos -> productos.toListaCompraUI() }
    }
}
