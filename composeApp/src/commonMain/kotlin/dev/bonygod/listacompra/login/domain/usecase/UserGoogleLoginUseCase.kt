package dev.bonygod.listacompra.login.domain.usecase

import dev.bonygod.listacompra.login.data.repository.UserRepository

class UserGoogleLoginUseCase(
    private val userRepo: UserRepository
) {
    suspend operator fun invoke(idToken: String) {
        return userRepo.loginWithGoogle(idToken)
    }
}