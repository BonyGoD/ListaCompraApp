package dev.bonygod.listacompra.login.domain.usecase

import dev.bonygod.listacompra.core.CustomFailures.LoginFailure
import dev.bonygod.listacompra.login.data.repository.UserRepository
import dev.bonygod.listacompra.login.domain.model.UserRegister
import dev.bonygod.listacompra.login.domain.model.Usuario
import dev.bonygod.listacompra.util.isValidEmail

class UserRegisterUseCase (
    private val userRepo: UserRepository
) {
    suspend operator fun invoke(userData: UserRegister): Result<Usuario> {
        if (userData.userName.isEmpty()) {
            return Result.failure(LoginFailure.BlankUserName())
        }
        if (userData.password.isEmpty() || userData.confirmPassword.isEmpty()) {
            return Result.failure(LoginFailure.BlankPassword())
        }
        if(!userData.email.isValidEmail()) {
            return Result.failure(LoginFailure.IncorrectEmail())
        }
        if (userData.password != userData.confirmPassword) {
            return Result.failure(LoginFailure.PasswordsNotEqual())
        }
        return userRepo.userRegister(userData.email, userData.password)
    }
}