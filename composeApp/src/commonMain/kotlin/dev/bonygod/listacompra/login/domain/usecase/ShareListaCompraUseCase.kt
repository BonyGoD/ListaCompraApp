package dev.bonygod.listacompra.login.domain.usecase

import dev.bonygod.listacompra.login.data.repository.UserRepository

class ShareListaCompraUseCase(
    private val userRepo: UserRepository
) {
    suspend operator fun invoke(nombre: String, email: String, listaId: String): Result<Unit> {
        return userRepo.shareListaCompra(nombre, email, listaId)
    }
}