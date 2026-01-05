//
//  GoogleUserData.swift
//  iosApp
//
//  Created by Ivan Boniquet Rodriguez on 4/1/26.
//  Copyright © 2026 BonyGoD. All rights reserved.
//

import Foundation

@objc public class GoogleUserData: NSObject {
    @objc public let idToken: String
    @objc public let accessToken: String
    @objc public let email: String
    @objc public let displayName: String
    @objc public let photoURL: String

    public init(
        idToken: String,
        accessToken: String,
        email: String,
        displayName: String,
        photoURL: String
    ) {
        self.idToken = idToken
        self.accessToken = accessToken
        self.email = email
        self.displayName = displayName
        self.photoURL = photoURL
        super.init()
    }
}

