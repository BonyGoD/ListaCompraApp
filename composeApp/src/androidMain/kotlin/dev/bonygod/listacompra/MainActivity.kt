package dev.bonygod.listacompra

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.bonygod.listacompra.core.navigation.PendingNotificationAction
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MainActivity : ComponentActivity(), KoinComponent {

    private val pendingNotificationAction: PendingNotificationAction by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                Color.TRANSPARENT,
                Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                Color.TRANSPARENT,
                Color.TRANSPARENT
            )
        )
        super.onCreate(savedInstanceState)
        Firebase.initialize(this)

        // Configurar Crashlytics según el build type
        // En debug no se reportarán crashes, en release sí
        configureCrashlytics()

        initPlatform(this)
        handleNotificationIntent(intent)
        setContent {
            App()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent) {
        if (intent.getStringExtra(NOTIFICATION_TYPE_EXTRA) == NOTIFICATION_TYPE_LISTA_COMPARTIDA) {
            pendingNotificationAction.requestNotificaciones()
        }
    }

    private fun configureCrashlytics() {
        // Detectar si es debug basándose en si es debuggable
        val isDebug = (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
        val buildType = if (isDebug) "debug" else "release"

        FirebaseCrashlytics.getInstance().apply {
            setCustomKey("build_type", buildType)
            isCrashlyticsCollectionEnabled = !isDebug
        }
    }

    private companion object {
        const val NOTIFICATION_TYPE_EXTRA = "tipo"
        const val NOTIFICATION_TYPE_LISTA_COMPARTIDA = "lista_compartida"
    }
}
