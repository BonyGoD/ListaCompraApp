package dev.bonygod.listacompra.core.review

import platform.Foundation.NSNotificationCenter

private const val REVIEW_REQUESTED_NOTIFICATION = "AppReviewRequested"

// La petición con escena (requestReview(in:)) la hace Swift: ver iOSApp.swift.
actual suspend fun requestAppReview() {
    NSNotificationCenter.defaultCenter.postNotificationName(
        REVIEW_REQUESTED_NOTIFICATION,
        `object` = null
    )
}
