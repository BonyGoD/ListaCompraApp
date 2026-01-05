//
//  GoogleSignInBridge.swift
//  iosApp
//
//  Created by Ivan Boniquet Rodriguez on 4/1/26.
//  Copyright © 2026 BonyGoD. All rights reserved.
//

import Foundation
import GoogleSignIn
import UIKit

/// Servicio encargado de gestionar la autenticación con Google Sign-In
@objc public class GoogleSignInBridge: NSObject {

    /// Inicia el proceso de autenticación con Google
    /// - Parameters:
    ///   - presentingViewController: ViewController desde donde se presenta el sign-in
    ///   - completion: Callback con el resultado (GoogleUserData o Error)
    @objc public static func signIn(
        presentingViewController: UIViewController,
        completion: @escaping (GoogleUserData?, NSError?) -> Void
    ) {

        guard
            let clientID = Bundle.main.object(
                forInfoDictionaryKey: "GIDClientID"
            ) as? String
        else {
            completion(nil, NSError(domain: "Missing GIDClientID", code: -1))
            return
        }

        let config = GIDConfiguration(clientID: clientID)

        GIDSignIn.sharedInstance.configuration = config

        GIDSignIn.sharedInstance.signIn(
            withPresenting: presentingViewController
        ) { result, error in

            if let error = error {
                completion(nil, error as NSError)
                return
            }

            guard let user = result?.user,
                  let idToken = user.idToken?.tokenString
            else {
                completion(nil, NSError(domain: "No ID Token", code: -2))
                return
            }

            // accessToken es String, no opcional
            let accessToken = user.accessToken.tokenString

            // Obtener datos del usuario
            let email = user.profile?.email ?? ""
            let displayName = user.profile?.name ?? ""
            let photoURL = user.profile?.imageURL(withDimension: 200)?.absoluteString ?? ""

            let userData = GoogleUserData(
                idToken: idToken,
                accessToken: accessToken,
                email: email,
                displayName: displayName,
                photoURL: photoURL
            )

            completion(userData, nil)
        }
    }
}

