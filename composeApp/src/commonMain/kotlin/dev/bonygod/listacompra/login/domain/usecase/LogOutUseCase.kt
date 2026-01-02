package dev.bonygod.listacompra.login.domain.usecase

import dev.bonygod.listacompra.login.data.repository.UserRepository

class LogOutUseCase(
    private val userRepo: UserRepository
) {
    suspend operator fun invoke() {
        userRepo.logOut()
    }
}