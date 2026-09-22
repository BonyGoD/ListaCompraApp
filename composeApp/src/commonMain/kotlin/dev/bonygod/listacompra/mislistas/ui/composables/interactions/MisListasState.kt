package dev.bonygod.listacompra.mislistas.ui.composables.interactions

import dev.bonygod.listacompra.mislistas.ui.model.ListaInfoUI

data class MisListasState(
    val listas: List<ListaInfoUI> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val renameDialogListaId: String? = null,
    val renameDialogCurrentNombre: String = "",
    val showCreateDialog: Boolean = false,
    val deleteDialogListaId: String? = null,
    val deleteDialogNombre: String = "",
    val deleteDialogEsPropia: Boolean = false
)
