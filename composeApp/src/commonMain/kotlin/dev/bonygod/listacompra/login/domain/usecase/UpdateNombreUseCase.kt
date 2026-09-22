package dev.bonygod.listacompra.login.domain.usecase

import dev.bonygod.listacompra.login.data.repository.UserRepository

class UpdateNombreUseCase(private val userRepo: UserRepository) {
    suspend operator fun invoke(nombre: String): Result<Unit> =
        userRepo.updateNombre(nombre)
}
