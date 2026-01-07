package dev.bonygod.listacompra.core.network

expect class GoogleAuthHelper {
    suspend fun signInWithGoogle(onSuccess: (String, String, String, String) -> Unit, onError: (String) -> Unit)
}
