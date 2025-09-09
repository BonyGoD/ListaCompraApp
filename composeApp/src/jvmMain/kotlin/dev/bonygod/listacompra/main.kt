package dev.bonygod.listacompra

import android.app.Application
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.google.firebase.FirebasePlatform
import dev.bonygod.listacompra.core.di.appModule
import dev.bonygod.listacompra.core.di.dataModule
import dev.bonygod.listacompra.core.di.initKoin
import dev.bonygod.listacompra.core.di.viewModelsModule
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.initialize
import java.io.FileInputStream
import java.util.Properties

fun main() = application {

    FirebasePlatform.initializeFirebasePlatform(object: FirebasePlatform() {
        val storage = mutableMapOf<String, String>()
        override fun clear(key: String){
            storage.remove(key)
        }

        override fun log(msg: String) = println(msg)

        override fun retrieve(key: String) = storage[key]

        override fun store(key: String, value: String) = storage.set(key, value)

    })

    val properties = Properties().apply {
        val projectRoot = System.getProperty("user.dir")
        val rootDir = java.io.File(projectRoot).parentFile ?: java.io.File(projectRoot)
        val localPropsFile = java.io.File(rootDir, "local.properties")
        if (localPropsFile.exists()) {
            load(FileInputStream(localPropsFile))
        } else {
            println("Warning: local.properties not found at ${localPropsFile.absolutePath}")
        }
    }

    val options = FirebaseOptions(
        projectId = properties.getProperty("FIREBASE_PROJECT_ID"),
        applicationId = properties.getProperty("FIREBASE_APP_ID"),
        apiKey = properties.getProperty("FIREBASE_API_KEY")
    )

    Firebase.initialize(Application(), options)

    initKoin {
        modules(appModule, viewModelsModule, dataModule)
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Lista de Compra",
    ) {
        App()
    }
}