package dev.bonygod.listacompra.home.domain.usecase

import dev.bonygod.listacompra.home.data.repository.ProductosRepository
import dev.bonygod.listacompra.home.ui.mapper.toListaCompraUI
import dev.bonygod.listacompra.home.ui.model.ListaCompraUI
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
