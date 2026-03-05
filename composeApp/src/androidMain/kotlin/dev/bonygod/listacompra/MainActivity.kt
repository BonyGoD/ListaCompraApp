package dev.bonygod.listacompra

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize
import com.google.firebase.crashlytics.FirebaseCrashlytics

class MainActivity : ComponentActivity() {
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
        setContent {
            App()
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
}
