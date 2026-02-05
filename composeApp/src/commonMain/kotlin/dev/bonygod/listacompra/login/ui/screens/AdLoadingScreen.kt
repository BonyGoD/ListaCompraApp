package dev.bonygod.listacompra.login.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.bonygod.listacompra.ads.AdConstants
import dev.bonygod.listacompra.ads.getInterstitialAdUnitId
import dev.bonygod.listacompra.ads.ui.InterstitialAdTrigger
import dev.bonygod.listacompra.common.ui.FullScreenLoading
import dev.bonygod.listacompra.core.navigation.Navigator
import dev.bonygod.listacompra.core.navigation.Routes
import kotlinx.coroutines.delay
import org.koin.compose.koinInject

@Composable
fun AdLoadingScreen(userId: String) {
    val navigator: Navigator = koinInject()

    InterstitialAdTrigger(
        adUnitId = AdConstants.getInterstitialAdUnitId(),
        onAdShown = {
            println("AdLoadingScreen: ✅ Ad shown")
        },
        onAdDismissed = {
            println("AdLoadingScreen: 👋 Ad dismissed, navigating to Home")
            navigator.clearAndNavigateTo(Routes.Home(userId))
        },
        onAdFailedToShow = { error ->
            println("AdLoadingScreen: ❌ Ad failed: $error, navigating to Home")
            navigator.clearAndNavigateTo(Routes.Home(userId))
        }
    ) { showAd ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            FullScreenLoading()
        }

        LaunchedEffect(Unit) {
            delay(3000)
            showAd()
        }
    }
}
