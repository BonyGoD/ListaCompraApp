package dev.bonygod.listacompra.login.domain.usecase

import dev.bonygod.listacompra.login.data.repository.UserRepository
import dev.bonygod.listacompra.login.domain.model.Usuario

class AddSharedListUseCase(
    private val userRepo: UserRepository
) {
    suspend operator fun invoke(listaId: String): Result<Usuario> {
        return userRepo.addSharedList(listaId)
    }
}