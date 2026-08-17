package dev.bonygod.listacompra

import android.content.Context
import android.os.Build

internal lateinit var appContext: Context
    private set

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.RELEASE}"
    override val appVersion: String?
        get() = try {
            appContext.packageManager.getPackageInfo(appContext.packageName, 0).versionName
        } catch (e: Exception) {
            "N/A"
        }
    override val isDebugBuild: Boolean
        get() = (appContext.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
}

actual fun getPlatform(): Platform = AndroidPlatform()

fun initPlatform(context: Context) {
    appContext = context.applicationContext
}
