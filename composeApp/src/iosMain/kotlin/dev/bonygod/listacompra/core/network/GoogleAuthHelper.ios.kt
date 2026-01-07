package dev.bonygod.listacompra.core.network

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.UIKit.UIApplication
import kotlin.coroutines.resume


@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
actual class GoogleAuthHelper {

    actual suspend fun signInWithGoogle(
        onSuccess: (String, String, String, String) -> Unit,
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
                onSuccess = { displayName, uid, email, photoURL ->
                    onSuccess(displayName, uid, email, photoURL)
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
