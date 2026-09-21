package dev.bonygod.listacompra.login.domain.usecase

import dev.bonygod.crashlytics.kmp.core.CrashReporter
import dev.bonygod.listacompra.login.data.repository.UserRepository
import dev.bonygod.listacompra.notificaciones.PushNotifications

class LogOutUseCase(
    private val userRepo: UserRepository,
    private val crashReporter: CrashReporter
) {
    suspend operator fun invoke() {
        val token = if (PushNotifications.hasPermission()) PushNotifications.getToken() else null
        userRepo.logOut(token)
        crashReporter.setUserId(null)
    }
}
