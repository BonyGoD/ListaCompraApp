package dev.bonygod.listacompra.home.ui.model

data class UserUI(
    val uid: String = "",
    val email: String = "",
    val listas: List<String> = emptyList()
) {
    val listaId: String get() = listas.firstOrNull() ?: ""
}