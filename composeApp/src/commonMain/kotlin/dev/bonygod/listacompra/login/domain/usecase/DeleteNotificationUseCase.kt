package dev.bonygod.listacompra.login.domain.usecase

import dev.bonygod.listacompra.login.data.repository.UserRepository

class DeleteNotificationUseCase(
    private val userRepo: UserRepository
) {
    suspend operator fun invoke(listaId: String) {
        userRepo.deleteNotification(listaId)
    }
}