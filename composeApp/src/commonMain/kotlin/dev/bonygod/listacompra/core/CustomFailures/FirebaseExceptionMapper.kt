package dev.bonygod.listacompra.core.CustomFailures

/**
 * Mapea excepciones de Firebase a LoginFailure específicos
 * Examina tanto el mensaje de la excepción como su causa (cause) para determinar el tipo de error
 */
fun Exception.toUserFailure(): LoginFailure {
    val errorMessage = this.message.orEmpty()

    return when {
        errorMessage.contains(
            "The supplied auth credential is incorrect, malformed or has expired."
        ) -> LoginFailure.UserNotFound()
        else -> LoginFailure.UnknownError()
    }
}

