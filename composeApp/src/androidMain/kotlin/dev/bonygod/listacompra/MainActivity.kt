package dev.bonygod.listacompra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.bonygod.listacompra.ui.screens.HomeScreen
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        Firebase.initialize(this)
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