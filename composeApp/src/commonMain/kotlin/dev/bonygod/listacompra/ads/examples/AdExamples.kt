package dev.bonygod.listacompra.ads.examples

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.bonygod.listacompra.ads.AdConstants
import dev.bonygod.listacompra.ads.ui.BannerAd
import dev.bonygod.listacompra.ads.ui.InterstitialAdTrigger

/**
 * Ejemplo 1: Pantalla simple con Banner en la parte inferior
 */
@Composable
fun ExampleScreenWithBanner() {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Tu contenido principal
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text("Contenido de tu app")
        }

        // Banner publicitario en la parte inferior
        BannerAd(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            adUnitId = AdConstants.BANNER_AD_UNIT_ID_ANDROID,
            onAdLoaded = {
                println("✅ Banner cargado exitosamente")
            },
            onAdFailedToLoad = { error ->
                println("❌ Error al cargar banner: $error")
            }
        )
    }
}

/**
 * Ejemplo 2: Botón que muestra un Interstitial antes de navegar
 */
@Composable
fun ExampleScreenWithInterstitial(
    onNavigateToNextScreen: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Pantalla actual",
            style = MaterialTheme.typography.headlineMedium
        )

        // El Interstitial se carga automáticamente
        // Cuando el usuario hace clic, muestra el anuncio
        // Después de cerrar el anuncio, navega a la siguiente pantalla
        InterstitialAdTrigger(
            adUnitId = AdConstants.INTERSTITIAL_AD_UNIT_ID_ANDROID,
            onAdShown = {
                println("📺 Mostrando anuncio intersticial")
            },
            onAdDismissed = {
                println("👋 Usuario cerró el anuncio")
                // Navegar después de cerrar el anuncio
                onNavigateToNextScreen()
            },
            onAdFailedToShow = { error ->
                println("❌ Error: $error")
                // Si falla, navegar directamente
                onNavigateToNextScreen()
            }
        ) { showAd ->
            Button(
                onClick = { showAd() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continuar (Mostrar anuncio)")
            }
        }
    }
}

/**
 * Ejemplo 3: Combinación de Banner + Interstitial
 */
@Composable
fun ExampleFullAdsScreen(
    onExit: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Contenido principal
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Pantalla con anuncios", style = MaterialTheme.typography.headlineMedium)
            Text("Aquí puedes poner tu contenido normal...")

            Spacer(modifier = Modifier.weight(1f))

            // Botón con intersticial
            InterstitialAdTrigger(
                adUnitId = AdConstants.INTERSTITIAL_AD_UNIT_ID_ANDROID,
                onAdDismissed = { onExit() },
                onAdFailedToShow = { onExit() }
            ) { showAd ->
                Button(
                    onClick = { showAd() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Salir")
                }
            }
        }

        // Banner fijo al final
        BannerAd(
            modifier = Modifier.fillMaxWidth(),
            adUnitId = AdConstants.BANNER_AD_UNIT_ID_ANDROID,
            onAdLoaded = { println("Banner cargado") },
            onAdFailedToLoad = { error -> println("Error banner: $error") }
        )
    }
}
