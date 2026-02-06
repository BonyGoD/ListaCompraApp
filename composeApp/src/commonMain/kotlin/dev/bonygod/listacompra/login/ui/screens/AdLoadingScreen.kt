package dev.bonygod.listacompra.login.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.bonygod.listacompra.common.ui.FullScreenLoading
import dev.bonygod.listacompra.core.navigation.Navigator
import dev.bonygod.listacompra.core.navigation.Routes
import org.koin.compose.koinInject

@Composable
fun AdLoadingScreen(userId: String) {
    val navigator: Navigator = koinInject()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        FullScreenLoading()
    }

    ShowPreloadedInterstitial(
        onAdShown = {
            println("AdLoadingScreen: ✅ Ad shown")
        },
        onAdDismissed = {
            println("AdLoadingScreen: 👋 Ad dismissed, navigating to Home")
            navigator.clearAndNavigateTo(Routes.Home(userId))
        },
        onAdFailedToShow = {
            println("AdLoadingScreen: ❌ Ad failed, navigating to Home")
            navigator.clearAndNavigateTo(Routes.Home(userId))
        }
    )
}
