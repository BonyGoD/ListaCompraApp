//
//  GoogleAuthCallbackHelper.swift
//  iosApp
//
//  Helper para conectar GoogleSignInBridge con el código Kotlin
//

import Foundation
import UIKit
import ComposeApp

/// Servicio que coordina la comunicación entre Kotlin y GoogleSignInBridge
/// Escucha notificaciones de Kotlin y gestiona el flujo de autenticación
public class GoogleAuthCallbackHelper {

    public static let shared = GoogleAuthCallbackHelper()

    private init() {
        // Escuchar la notificación de Kotlin
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handleSignInRequest),
            name: NSNotification.Name("GoogleSignInRequested"),
            object: nil
        )
    }

    @objc private func handleSignInRequest() {
        // Obtener el root view controller
        guard let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
              let rootVC = windowScene.windows.first?.rootViewController else {
            // Llamar al callback de error en Kotlin
            GoogleSignInCallback.shared.onSignInError(error: "No se pudo obtener el view controller")
            return
        }

        // Llamar a GoogleSignInBridge
        GoogleSignInBridge.signIn(presentingViewController: rootVC) { userData, error in
            if let error = error {
                // Notificar error a Kotlin
                GoogleSignInCallback.shared.onSignInError(error: error.localizedDescription)
            } else if let userData = userData {
                // Notificar éxito a Kotlin con todos los datos
                GoogleSignInCallback.shared.onSignInSuccessWithUserData(
                    displayName: userData.displayName,
                    uid: userData.uid,
                    email: userData.email,
                    photoURL: userData.photoURL
                )
            } else {
                // Error desconocido
                GoogleSignInCallback.shared.onSignInError(error: "Unknown error")
            }
        }
    }

    deinit {
        NotificationCenter.default.removeObserver(self)
    }
}

