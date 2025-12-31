package dev.bonygod.listacompra.home.ui.composables.preview

import dev.bonygod.listacompra.home.ui.model.ListaCompraUI
import dev.bonygod.listacompra.home.ui.model.ProductoUI

object ListaCompraPreview {
    val ListaCompraUI = ListaCompraUI(
        productos = listOf(
            ProductoUI(
                nombre = "Leche",
                fecha = "2024-06-30",
                unidades = 2
            ),
            ProductoUI(
                nombre = "Pan",
                fecha = "2024-07-01",
                unidades = 1
            ),
            ProductoUI(
                nombre = "Huevos",
                fecha = "2024-07-05",
                unidades = 12
            ),
            ProductoUI(
                nombre = "Plátanos",
                fecha = "2024-07-02",
                unidades = 5
            ),
            ProductoUI(
                nombre = "Azúcar",
                fecha = "2024-07-03",
                unidades = 3
            ),
        )
    )
}