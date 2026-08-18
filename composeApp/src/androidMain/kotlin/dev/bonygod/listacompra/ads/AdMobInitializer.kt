package dev.bonygod.listacompra.ads

import android.content.Context
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object AdMobInitializer {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var started = false

    fun initialize(context: Context, onComplete: () -> Unit = {}) {
        if (started) return
        started = true

        scope.launch {
            MobileAds.initialize(context.applicationContext) {
                scope.launch(Dispatchers.Main) {
                    onComplete()
                }
            }
        }
    }
}
