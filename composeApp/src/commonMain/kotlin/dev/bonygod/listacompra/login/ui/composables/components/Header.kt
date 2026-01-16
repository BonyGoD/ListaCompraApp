package dev.bonygod.listacompra.login.ui.composables.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import listacompra.composeapp.generated.resources.Inter_Italic
import listacompra.composeapp.generated.resources.Res
import listacompra.composeapp.generated.resources.app_icon
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource

@Composable
fun Header(
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = CenterHorizontally
    ) {
        Image(
            painter = painterResource(Res.drawable.app_icon),
            contentDescription = "App icon",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 50.dp)
                .size(100.dp)
        )
        Text(
            text = title,
            fontFamily = FontFamily(Font(Res.font.Inter_Italic)),
            fontWeight = FontWeight.Bold,
            fontSize = 25.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 16.dp)
        )
        Text(
            text = subtitle,
            fontFamily = FontFamily(Font(Res.font.Inter_Italic)),
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 8.dp)
        )
    }
}