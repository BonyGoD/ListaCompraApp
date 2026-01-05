package dev.bonygod.listacompra.core.network

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.UIKit.UIApplication
import kotlin.coroutines.resume


@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
actual class GoogleAuthHelper {

    actual suspend fun signInWithGoogle(
        onSuccess: (String, String, String, String, String) -> Unit,
        onError: (String) -> Unit,
    ) {
        suspendCancellableCoroutine<Unit> { cont ->

            val rootViewController =
                UIApplication.sharedApplication.keyWindow?.rootViewController

            if (rootViewController == null) {
                onError("RootViewController not found")
                cont.resume(Unit)
                return@suspendCancellableCoroutine
            }

            // Registrar los callbacks con datos completos del usuario
            GoogleSignInCallback.registerWithUserData(
                onSuccess = { idToken, accessToken, email, displayName, photoURL ->
                    // Autenticar con Firebase usando el token de Google
                    kotlinx.coroutines.GlobalScope.launch {
                        try {
                            val credential = GoogleAuthProvider.credential(idToken, accessToken)
                            val result = Firebase.auth.signInWithCredential(credential)
                            val user = result.user

                            if (user != null) {
                                onSuccess(
                                    displayName,
                                    user.uid,
                                    idToken,
                                    email,
                                    photoURL
                                )
                            } else {
                                onError("Usuario de Firebase es null")
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        cont.resume(Unit)
                    }
                },
                onError = { error ->
                    onError(error)
                    cont.resume(Unit)
                }
            )
            // Notificar que se requiere sign-in
            // Swift escuchará esta notificación y llamará a GoogleSignInBridge
            platform.Foundation.NSNotificationCenter.defaultCenter.postNotificationName(
                "GoogleSignInRequested",
                `object` = null
            )
        }
    }
}
