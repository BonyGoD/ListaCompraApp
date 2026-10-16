package dev.bonygod.listacompra.login.domain.model

data class ContadoresResena(
    val comprasCompletadas: Int = 0,
    val resenaPedidaVersion: String = "",
    val resenaPedidaCompras: Int = 0
) {
    // La cuota de Apple ronda las 3 al año: se pide a la tercera compra y después solo
    // con versión nueva y 10 compras más, para no gastarla en quien aún no conoce la app.
    // Los 10 de separación valen entre peticiones, no antes de la primera: sin petición
    // previa resenaPedidaCompras es 0 y exigirlos retrasaría la primera a la décima
    // compra. Por eso se distingue con resenaPedidaVersion vacío. No "simplificar".
    fun debePedirResena(appVersion: String): Boolean {
        if (comprasCompletadas < COMPRAS_MINIMAS) return false
        val nuncaPedida = resenaPedidaVersion.isEmpty()
        if (nuncaPedida) return true
        return appVersion != resenaPedidaVersion &&
            comprasCompletadas - resenaPedidaCompras >= COMPRAS_ENTRE_PETICIONES
    }

    private companion object {
        const val COMPRAS_MINIMAS = 3
        const val COMPRAS_ENTRE_PETICIONES = 10
    }
}
