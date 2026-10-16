package dev.bonygod.listacompra.login.domain.usecase

import dev.bonygod.listacompra.login.data.repository.UserRepository

class MarcarResenaPedidaUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(version: String, compras: Int): Result<Unit> =
        userRepository.marcarResenaPedida(version, compras)
}
