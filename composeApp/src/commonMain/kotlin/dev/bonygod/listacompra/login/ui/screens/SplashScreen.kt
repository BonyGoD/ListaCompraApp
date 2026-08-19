package dev.bonygod.listacompra.login.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.bonygod.listacompra.common.ui.theme.PrimaryBlue
import dev.bonygod.listacompra.login.ui.SplashViewModel
import dev.bonygod.listacompra.login.ui.composables.interactions.SplashEvent
import listacompra.composeapp.generated.resources.Res
import listacompra.composeapp.generated.resources.app_icon
import listacompra.composeapp.generated.resources.splash_screen_error_message
import listacompra.composeapp.generated.resources.splash_screen_login_button
import listacompra.composeapp.generated.resources.splash_screen_retry_button
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SplashScreen() {
    val viewModel: SplashViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        if (state.isLoading) {
            SplashLoading()
        } else {
            SplashError(onEvent = viewModel::onEvent)
        }
    }
}

@Composable
private fun SplashLoading() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(Res.drawable.app_icon),
            contentDescription = null,
            modifier = Modifier.size(120.dp)
        )
        CircularProgressIndicator(
            modifier = Modifier
                .padding(top = 32.dp)
                .size(48.dp),
            color = PrimaryBlue
        )
    }
}

@Composable
private fun SplashError(onEvent: (SplashEvent) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(Res.string.splash_screen_error_message),
            textAlign = TextAlign.Center
        )
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            onClick = { onEvent(SplashEvent.Retry) }
        ) {
            Text(text = stringResource(Res.string.splash_screen_retry_button))
        }
        TextButton(
            modifier = Modifier.padding(top = 8.dp),
            onClick = { onEvent(SplashEvent.GoToLogin) }
        ) {
            Text(text = stringResource(Res.string.splash_screen_login_button), color = PrimaryBlue)
        }
    }
}
