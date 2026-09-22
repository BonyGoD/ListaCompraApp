package dev.bonygod.listacompra.login.domain.usecase

import dev.bonygod.listacompra.login.data.repository.UserRepository

class GuardarTokenPushUseCase(private val userRepo: UserRepository) {
    suspend operator fun invoke(token: String): Result<Unit> = userRepo.guardarTokenPush(token)
}
