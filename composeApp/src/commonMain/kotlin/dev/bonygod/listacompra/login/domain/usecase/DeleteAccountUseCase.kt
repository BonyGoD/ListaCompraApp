package dev.bonygod.listacompra.login.domain.usecase

import dev.bonygod.listacompra.login.data.repository.UserRepository

class DeleteAccountUseCase(
    private val userRepo: UserRepository
) {
    suspend operator fun invoke() {
        userRepo.deleteAccount()
    }
}