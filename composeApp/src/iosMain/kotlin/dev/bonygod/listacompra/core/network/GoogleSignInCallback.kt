package dev.bonygod.listacompra.core.network

/**
 * Callback para manejar el resultado del Google Sign-In desde Swift.
 *
 * Esta clase debe ser llamada desde el código Swift de iosApp después
 * de que GoogleSignInBridge complete el proceso de autenticación.
 */
object GoogleSignInCallback {

    private var onSuccessCallback: ((String) -> Unit)? = null
    private var onSuccessWithDataCallback: ((String, String, String, String) -> Unit)? = null
    private var onErrorCallback: ((String) -> Unit)? = null

    /**
     * Registra los callbacks que serán invocados cuando Swift complete el sign-in.
     */
    fun register(
        onSuccess: (idToken: String) -> Unit,
        onError: (error: String) -> Unit
    ) {
        onSuccessCallback = onSuccess
        onErrorCallback = onError
    }

    /**
     * Registra los callbacks con datos completos del usuario.
     */
    fun registerWithUserData(
        onSuccess: (displayName: String, uid: String, email: String, photoURL: String) -> Unit,
        onError: (error: String) -> Unit
    ) {
        onSuccessWithDataCallback = onSuccess
        onErrorCallback = onError
    }

    /**
     * Método que Swift debe llamar cuando el sign-in es exitoso (solo token).
     */
    fun onSignInSuccess(idToken: String) {
        onSuccessCallback?.invoke(idToken)
        clear()
    }

    /**
     * Método que Swift debe llamar cuando el sign-in es exitoso con datos completos.
     */
    fun onSignInSuccessWithUserData(displayName: String, uid: String, email: String, photoURL: String) {
        onSuccessWithDataCallback?.invoke(displayName, uid, email, photoURL)
        clear()
    }

    /**
     * Método que Swift debe llamar cuando hay un error.
     */
    fun onSignInError(error: String) {
        onErrorCallback?.invoke(error)
        clear()
    }

    private fun clear() {
        onSuccessCallback = null
        onSuccessWithDataCallback = null
        onErrorCallback = null
    }
}
