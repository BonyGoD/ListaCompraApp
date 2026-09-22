package dev.bonygod.listacompra.notificaciones

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import dev.bonygod.listacompra.R
import dev.bonygod.listacompra.appContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual object PushNotifications {
    const val CHANNEL_ID = "push_notifications_channel"

    actual fun initialize() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return
            val channel = NotificationChannel(
                CHANNEL_ID,
                appContext.getString(R.string.push_notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }
    }

    actual suspend fun getToken(): String? = suspendCancellableCoroutine { continuation ->
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                if (continuation.isActive) continuation.resume(token)
            }
            .addOnFailureListener {
                if (continuation.isActive) continuation.resume(null)
            }
    }

    actual suspend fun hasPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(appContext).areNotificationsEnabled()
        }
    }
}
