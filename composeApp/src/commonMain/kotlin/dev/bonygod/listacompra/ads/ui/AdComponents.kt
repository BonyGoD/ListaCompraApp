package dev.bonygod.listacompra.ads.ui
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
@Composable
expect fun BannerAd(
    modifier: Modifier = Modifier,
    adUnitId: String,
    onAdLoaded: () -> Unit = {},
    onAdFailedToLoad: (String) -> Unit = {}
)
@Composable
expect fun InterstitialAdTrigger(
    adUnitId: String,
    onAdShown: () -> Unit = {},
    onAdDismissed: () -> Unit = {},
    onAdFailedToShow: (String) -> Unit = {},
    content: @Composable (showAd: () -> Unit) -> Unit
)
