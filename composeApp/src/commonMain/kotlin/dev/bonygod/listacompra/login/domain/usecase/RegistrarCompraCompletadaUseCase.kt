package dev.bonygod.listacompra.login.domain.usecase

import dev.bonygod.listacompra.login.data.repository.UserRepository
import dev.bonygod.listacompra.login.domain.model.ContadoresResena

class RegistrarCompraCompletadaUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(): Result<ContadoresResena> =
        userRepository.registrarCompraCompletada()
}
