package dev.bonygod.listacompra.notificaciones

import dev.bonygod.listacompra.core.navigation.PendingNotificationAction
import dev.bonygod.listacompra.login.domain.usecase.GuardarTokenPushUseCase
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import platform.Foundation.NSNotification
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusEphemeral
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.seconds

private const val TOKEN_REQUESTED_NOTIFICATION = "FCMTokenRequested"
private const val TOKEN_RESPONSE_NOTIFICATION = "FCMTokenResponse"
private const val TOKEN_REFRESHED_NOTIFICATION = "FCMTokenRefreshed"
private const val TOKEN_USER_INFO_KEY = "token"
private const val TAP_NOTIFICATION = "PushNotificationTapped"
private const val TAP_BRIDGE_READY_NOTIFICATION = "PushNotificationTapBridgeReady"

@OptIn(ExperimentalForeignApi::class)
actual object PushNotifications : KoinComponent {
    private val guardarTokenPushUseCase: GuardarTokenPushUseCase by inject()
    private val appScope: CoroutineScope by inject(named("appScope"))
    private val pendingNotificationAction: PendingNotificationAction by inject()

    actual fun initialize() {
        NSNotificationCenter.defaultCenter.addObserverForName(
            name = TOKEN_REFRESHED_NOTIFICATION,
            `object` = null,
            queue = NSOperationQueue.mainQueue
        ) { notification: NSNotification? ->
            val token = notification?.userInfo?.get(TOKEN_USER_INFO_KEY) as? String
            if (token != null) {
                appScope.launch { guardarTokenPushUseCase(token) }
            }
        }

        NSNotificationCenter.defaultCenter.addObserverForName(
            name = TAP_NOTIFICATION,
            `object` = null,
            queue = NSOperationQueue.mainQueue
        ) { _: NSNotification? ->
            pendingNotificationAction.requestNotificaciones()
        }

        NSNotificationCenter.defaultCenter.postNotificationName(
            TAP_BRIDGE_READY_NOTIFICATION,
            `object` = null
        )
    }

    actual suspend fun getToken(): String? = withTimeoutOrNull(10.seconds) {
        suspendCancellableCoroutine { continuation ->
            var observer: Any? = null
            observer = NSNotificationCenter.defaultCenter.addObserverForName(
                name = TOKEN_RESPONSE_NOTIFICATION,
                `object` = null,
                queue = NSOperationQueue.mainQueue,
                usingBlock = { notification: NSNotification? ->
                    val token = notification?.userInfo?.get(TOKEN_USER_INFO_KEY) as? String
                    observer?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
                    if (continuation.isActive) continuation.resume(token)
                }
            )

            continuation.invokeOnCancellation {
                observer?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
            }

            NSNotificationCenter.defaultCenter.postNotificationName(
                TOKEN_REQUESTED_NOTIFICATION,
                `object` = null
            )
        }
    }

    actual suspend fun hasPermission(): Boolean = suspendCancellableCoroutine { continuation ->
        UNUserNotificationCenter.currentNotificationCenter().getNotificationSettingsWithCompletionHandler { settings ->
            val granted = when (settings?.authorizationStatus) {
                UNAuthorizationStatusAuthorized,
                UNAuthorizationStatusProvisional,
                UNAuthorizationStatusEphemeral -> true
                else -> false
            }
            if (continuation.isActive) continuation.resume(granted)
        }
    }
}
