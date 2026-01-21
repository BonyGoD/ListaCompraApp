package dev.bonygod.listacompra.login.domain.usecase

import dev.bonygod.listacompra.login.data.repository.UserRepository
import dev.bonygod.listacompra.login.ui.composables.mapper.toUI
import dev.bonygod.listacompra.login.ui.composables.model.NotificationsUI
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetNotificationsUseCase(
private val userRepo: UserRepository
) {
    operator fun invoke(): Flow<List<NotificationsUI>> {
        return userRepo.getNotifications()
            .map { notifications ->
                notifications?.items?.map { it.toUI() } ?: emptyList()
            }
    }
}