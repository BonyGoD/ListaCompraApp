package dev.bonygod.listacompra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            navigationBarStyle = SystemBarStyle.light(
                scrim = android.graphics.Color.WHITE,
                darkScrim = android.graphics.Color.WHITE
            )
        )
        super.onCreate(savedInstanceState)
        Firebase.initialize(this)
        initPlatform(this)
        setContent {
            App()
        }
    }
}

//@Preview(showSystemUi = true, showBackground = true)
//@Composable
//fun AppAndroidPreview() {
//    ScreenWrapper {
//        HomeScreen(firestoreRepository)
//    }
//}
