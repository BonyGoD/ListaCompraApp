package dev.bonygod.listacompra.mislistas.domain.usecase

import dev.bonygod.listacompra.login.data.repository.UserRepository

class DeleteListaUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(listaId: String, esPropia: Boolean): Result<Unit> =
        userRepository.deleteLista(listaId, esPropia)
}
