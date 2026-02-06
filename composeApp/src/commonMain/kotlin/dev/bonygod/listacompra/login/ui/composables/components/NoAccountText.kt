package dev.bonygod.listacompra.login.ui.composables.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.bonygod.listacompra.login.ui.composables.interactions.AuthEvent
import listacompra.composeapp.generated.resources.Res
import listacompra.composeapp.generated.resources.login_screen_no_account
import listacompra.composeapp.generated.resources.login_screen_register_link
import org.jetbrains.compose.resources.stringResource

@Composable
fun NoAccountText(setEvent: (AuthEvent) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(Res.string.login_screen_no_account),
        )
        Text(
            text = stringResource(Res.string.login_screen_register_link),
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(start = 5.dp)
                .clickable {
                    setEvent(AuthEvent.OnNavigateToRegister)
                }
        )
    }
}