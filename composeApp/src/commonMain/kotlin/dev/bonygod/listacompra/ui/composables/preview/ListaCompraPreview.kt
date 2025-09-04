package dev.bonygod.listacompra.ui.composables.preview

import dev.bonygod.listacompra.ui.model.ListaCompraUI
import dev.bonygod.listacompra.ui.model.ProductoUI

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
                nombre = "Platanos",
                fecha = "2024-07-02",
                unidades = 5
            ),
            ProductoUI(
                nombre = "Azucar",
                fecha = "2024-07-03",
                unidades = 3
            ),
        )
    )
}