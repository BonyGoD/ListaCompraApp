package dev.bonygod.listacompra.mislistas.domain.usecase

import dev.bonygod.listacompra.login.data.repository.UserRepository

class RenameListaUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(listaId: String, nombre: String): Result<Unit> =
        userRepository.renameNombreLista(listaId, nombre)
}

