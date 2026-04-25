package dev.bonygod.listacompra.mislistas.domain.usecase

import dev.bonygod.listacompra.login.data.repository.UserRepository

class SetDefaultListaUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(listaId: String): Result<Unit> =
        userRepository.setDefaultLista(listaId)
}
