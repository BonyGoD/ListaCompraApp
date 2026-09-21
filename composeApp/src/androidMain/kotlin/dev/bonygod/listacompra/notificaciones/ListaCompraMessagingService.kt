package dev.bonygod.listacompra.notificaciones

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dev.bonygod.listacompra.MainActivity
import dev.bonygod.listacompra.R
import dev.bonygod.listacompra.getPlatform
import dev.bonygod.listacompra.login.domain.usecase.GuardarTokenPushUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

class ListaCompraMessagingService : FirebaseMessagingService(), KoinComponent {

    private val guardarTokenPushUseCase: GuardarTokenPushUseCase by inject()
    private val appScope: CoroutineScope by inject(named("appScope"))

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        if (getPlatform().isDebugBuild) {
            Log.d(TAG, "Nuevo token FCM: $token")
        }
        appScope.launch {
            guardarTokenPushUseCase(token)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val notification = remoteMessage.notification ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            remoteMessage.data.forEach { (key, value) -> putExtra(key, value) }
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, PushNotifications.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_notification)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
            .setColor(ContextCompat.getColor(this, R.color.notification_accent_blue))
            .setContentTitle(notification.title)
            .setContentText(notification.body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationId = remoteMessage.messageId?.hashCode() ?: System.currentTimeMillis().toInt()
        NotificationManagerCompat.from(this).notify(notificationId, notificationBuilder.build())
    }

    private companion object {
        const val TAG = "PushNotifications"
    }
}
