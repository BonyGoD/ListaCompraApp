package dev.bonygod.listacompra.login.domain.usecase

import dev.bonygod.crashlytics.kmp.core.CrashReporter
import dev.bonygod.listacompra.login.data.repository.UserRepository

class LogOutUseCase(
    private val userRepo: UserRepository,
    private val crashReporter: CrashReporter
) {
    suspend operator fun invoke() {
        userRepo.logOut()
        crashReporter.setUserId(null)
    }
}
